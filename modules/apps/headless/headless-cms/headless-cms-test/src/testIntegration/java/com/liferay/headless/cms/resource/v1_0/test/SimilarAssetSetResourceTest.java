/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.cms.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.depot.constants.DepotConstants;
import com.liferay.depot.constants.DepotRolesConstants;
import com.liferay.depot.model.DepotEntry;
import com.liferay.depot.service.DepotEntryLocalService;
import com.liferay.headless.cms.client.dto.v1_0.SimilarAsset;
import com.liferay.headless.cms.client.dto.v1_0.SimilarAssetSet;
import com.liferay.headless.cms.client.pagination.Page;
import com.liferay.headless.cms.client.pagination.Pagination;
import com.liferay.headless.cms.client.resource.v1_0.SimilarAssetSetResource;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectEntryFolder;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryFolderLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.RoleConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Mikel Lorza
 */
@FeatureFlag("LPD-82226")
@RunWith(Arquillian.class)
public class SimilarAssetSetResourceTest
	extends BaseSimilarAssetSetResourceTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Override
	@Test
	public void testGetSimilarAssetSetsPage() throws Exception {
		DepotEntry depotEntry = _addSpaceDepotEntry(
			ServiceContextTestUtil.getServiceContext());

		long groupId = depotEntry.getGroupId();

		ObjectDefinition objectDefinition =
			_getBasicWebContentObjectDefinition();

		Page<SimilarAssetSet> similarAssetSetsPage = _getSimilarAssetSetsPage(
			groupId, null, null, null);

		Assert.assertEquals(0, similarAssetSetsPage.getTotalCount());

		List<SimilarAssetSet> similarAssetSets =
			(List<SimilarAssetSet>)similarAssetSetsPage.getItems();

		Assert.assertEquals(
			similarAssetSets.toString(), 0, similarAssetSets.size());

		ObjectEntry duplicateObjectEntry1 = _addObjectEntry(
			depotEntry, objectDefinition, _SIMILAR_TITLE, _SIMILAR_CONTENT);
		ObjectEntry duplicateObjectEntry2 = _addObjectEntry(
			depotEntry, objectDefinition, _SIMILAR_TITLE,
			_SIMILAR_CONTENT + " You can also contact support for help.");

		_addObjectEntry(
			depotEntry, objectDefinition, RandomTestUtil.randomString(),
			_DISTINCT_CONTENT);

		similarAssetSetsPage = _getSimilarAssetSetsPage(
			groupId, null, null, null);

		Assert.assertEquals(2, similarAssetSetsPage.getTotalCount());

		similarAssetSets =
			(List<SimilarAssetSet>)similarAssetSetsPage.getItems();

		Assert.assertEquals(
			similarAssetSets.toString(), 1, similarAssetSets.size());

		SimilarAssetSet similarAssetSet = similarAssetSets.get(0);

		Assert.assertEquals(_SIMILAR_TITLE, similarAssetSet.getTitle());
		Assert.assertEquals(
			2, GetterUtil.getInteger(similarAssetSet.getSize()));

		SimilarAsset[] similarAssets = similarAssetSet.getSimilarAssets();

		Assert.assertEquals(
			Arrays.toString(similarAssets), 2, similarAssets.length);

		List<Long> objectEntryIds = new ArrayList<>();

		for (SimilarAsset similarAssetSetAsset : similarAssets) {
			objectEntryIds.add(similarAssetSetAsset.getId());

			Assert.assertEquals(
				_SIMILAR_TITLE, similarAssetSetAsset.getTitle());
			Assert.assertNotNull(similarAssetSetAsset.getContentType());
			Assert.assertNotNull(similarAssetSetAsset.getDateModified());

			String itemURL = similarAssetSetAsset.getItemURL();

			Assert.assertTrue(
				itemURL,
				StringUtil.endsWith(
					itemURL,
					"/cms/edit_content_item?objectEntryId=" +
						similarAssetSetAsset.getId()));
		}

		Assert.assertTrue(
			objectEntryIds.contains(duplicateObjectEntry1.getObjectEntryId()));
		Assert.assertTrue(
			objectEntryIds.contains(duplicateObjectEntry2.getObjectEntryId()));

		_depotEntryLocalService.deleteDepotEntry(depotEntry.getDepotEntryId());

		_testGetSimilarAssetSetsPageGraphQL();
		_testGetSimilarAssetSetsPagePermissions();
		_testGetSimilarAssetSetsPageSearch();
		_testGetSimilarAssetSetsPageTranslation();
	}

	@Override
	@Test
	public void testGetSimilarAssetSetsPageWithPagination() throws Exception {
		DepotEntry depotEntry = _addSpaceDepotEntry(
			ServiceContextTestUtil.getServiceContext());

		long groupId = depotEntry.getGroupId();

		_addObjectEntries(depotEntry);

		Page<SimilarAssetSet> similarAssetSetsPage = _getSimilarAssetSetsPage(
			groupId, Pagination.of(1, 2), null, null);

		Assert.assertEquals(5, similarAssetSetsPage.getTotalCount());

		List<SimilarAssetSet> similarAssetSets =
			(List<SimilarAssetSet>)similarAssetSetsPage.getItems();

		Assert.assertEquals(
			similarAssetSets.toString(), 1, similarAssetSets.size());

		_assertSimilarAssetSet(
			similarAssetSets.get(0), 3, "Summer Sale",
			new String[] {"Big Summer Sale", "Summer Sale 2026"});

		similarAssetSetsPage = _getSimilarAssetSetsPage(
			groupId, Pagination.of(2, 2), null, null);

		Assert.assertEquals(5, similarAssetSetsPage.getTotalCount());

		similarAssetSets =
			(List<SimilarAssetSet>)similarAssetSetsPage.getItems();

		Assert.assertEquals(
			similarAssetSets.toString(), 2, similarAssetSets.size());

		_assertSimilarAssetSet(
			similarAssetSets.get(0), 3, "Summer Sale",
			new String[] {"Summer Sale Highlights"});
		_assertSimilarAssetSet(
			similarAssetSets.get(1), 2, _PRODUCT_LAUNCH_TITLE,
			new String[] {_PRODUCT_LAUNCH_TITLE});

		similarAssetSetsPage = _getSimilarAssetSetsPage(
			groupId, Pagination.of(3, 2), null, null);

		Assert.assertEquals(5, similarAssetSetsPage.getTotalCount());

		similarAssetSets =
			(List<SimilarAssetSet>)similarAssetSetsPage.getItems();

		Assert.assertEquals(
			similarAssetSets.toString(), 1, similarAssetSets.size());

		_assertSimilarAssetSet(
			similarAssetSets.get(0), 2, _PRODUCT_LAUNCH_TITLE,
			new String[] {_PRODUCT_LAUNCH_TITLE});

		_depotEntryLocalService.deleteDepotEntry(depotEntry.getDepotEntryId());
	}

	@Override
	@Test
	public void testGetSimilarAssetSetsPageWithSortDateTime() throws Exception {
		DepotEntry depotEntry = _addSpaceDepotEntry(
			ServiceContextTestUtil.getServiceContext());

		_addObjectEntries(depotEntry);

		for (String sortString :
				new String[] {"dateModified:asc", "dateModified:desc"}) {

			Page<SimilarAssetSet> similarAssetSetsPage =
				_getSimilarAssetSetsPage(
					depotEntry.getGroupId(), null, null, sortString);

			List<SimilarAssetSet> similarAssetSets =
				(List<SimilarAssetSet>)similarAssetSetsPage.getItems();

			Assert.assertEquals(
				similarAssetSets.toString(), 2, similarAssetSets.size());

			_assertSortedByDateModified(
				similarAssetSets, sortString.endsWith(":asc"));
		}

		_depotEntryLocalService.deleteDepotEntry(depotEntry.getDepotEntryId());
	}

	@Override
	@Test
	public void testGetSimilarAssetSetsPageWithSortString() throws Exception {
		DepotEntry depotEntry = _addSpaceDepotEntry(
			ServiceContextTestUtil.getServiceContext());

		long groupId = depotEntry.getGroupId();

		_addObjectEntries(depotEntry);

		// The largest set comes first when no sort is requested

		_assertSimilarAssetSetTitles(
			_getSimilarAssetSetsPage(groupId, null, null, null), "Summer Sale",
			_PRODUCT_LAUNCH_TITLE);

		_assertSimilarAssetSetTitles(
			_getSimilarAssetSetsPage(groupId, null, null, "title:asc"),
			_PRODUCT_LAUNCH_TITLE, "Summer Sale");
		_assertSimilarAssetSetTitles(
			_getSimilarAssetSetsPage(groupId, null, null, "title:desc"),
			"Summer Sale", _PRODUCT_LAUNCH_TITLE);

		_depotEntryLocalService.deleteDepotEntry(depotEntry.getDepotEntryId());
	}

	private void _addObjectEntries(DepotEntry depotEntry) throws Exception {
		ObjectDefinition objectDefinition =
			_getBasicWebContentObjectDefinition();

		_addObjectEntry(
			depotEntry, objectDefinition, "Big Summer Sale",
			_SUMMER_SALE_CONTENT);
		_addObjectEntry(
			depotEntry, objectDefinition, "Summer Sale 2026",
			_SUMMER_SALE_CONTENT + " The offer ends this Sunday.");
		_addObjectEntry(
			depotEntry, objectDefinition, "Summer Sale Highlights",
			_SUMMER_SALE_CONTENT +
				" The offer ends this Sunday and stock is limited.");

		_addObjectEntry(
			depotEntry, objectDefinition, _PRODUCT_LAUNCH_TITLE,
			_PRODUCT_LAUNCH_CONTENT);
		_addObjectEntry(
			depotEntry, objectDefinition, _PRODUCT_LAUNCH_TITLE,
			_PRODUCT_LAUNCH_CONTENT +
				" Contact the press office for further details.");

		_addObjectEntry(
			depotEntry, objectDefinition, RandomTestUtil.randomString(),
			_DISTINCT_CONTENT);
	}

	private ObjectEntry _addObjectEntry(
			DepotEntry depotEntry, ObjectDefinition objectDefinition,
			String title, String content)
		throws Exception {

		return _addObjectEntry(
			depotEntry, objectDefinition, title, content,
			ServiceContextTestUtil.getServiceContext());
	}

	private ObjectEntry _addObjectEntry(
			DepotEntry depotEntry, ObjectDefinition objectDefinition,
			String title, String content, ServiceContext serviceContext)
		throws Exception {

		ObjectEntryFolder objectEntryFolder =
			_objectEntryFolderLocalService.
				getObjectEntryFolderByExternalReferenceCode(
					"L_CONTENTS", depotEntry.getGroupId(),
					depotEntry.getCompanyId());

		return _objectEntryLocalService.addObjectEntry(
			depotEntry.getGroupId(), depotEntry.getUserId(),
			objectDefinition.getObjectDefinitionId(),
			objectEntryFolder.getObjectEntryFolderId(), "en_US",
			HashMapBuilder.<String, Serializable>put(
				"content_i18n",
				HashMapBuilder.put(
					"en_US", (Serializable)content
				).build()
			).put(
				"title_i18n",
				HashMapBuilder.put(
					"en_US", (Serializable)title
				).build()
			).build(),
			serviceContext);
	}

	private DepotEntry _addSpaceDepotEntry(ServiceContext serviceContext)
		throws Exception {

		return _depotEntryLocalService.addDepotEntry(
			HashMapBuilder.put(
				LocaleUtil.getDefault(), StringUtil.randomString()
			).build(),
			HashMapBuilder.put(
				LocaleUtil.getDefault(), StringUtil.randomString()
			).build(),
			DepotConstants.TYPE_SPACE, serviceContext);
	}

	private ObjectEntry _addTranslatedObjectEntry(
			DepotEntry depotEntry, ObjectDefinition objectDefinition,
			String title, String content, String spanishTitle,
			String spanishContent)
		throws Exception {

		ObjectEntryFolder objectEntryFolder =
			_objectEntryFolderLocalService.
				getObjectEntryFolderByExternalReferenceCode(
					"L_CONTENTS", depotEntry.getGroupId(),
					depotEntry.getCompanyId());

		return _objectEntryLocalService.addObjectEntry(
			depotEntry.getGroupId(), depotEntry.getUserId(),
			objectDefinition.getObjectDefinitionId(),
			objectEntryFolder.getObjectEntryFolderId(), "en_US",
			HashMapBuilder.<String, Serializable>put(
				"content_i18n",
				HashMapBuilder.put(
					"en_US", content
				).put(
					"es_ES", spanishContent
				).build()
			).put(
				"title_i18n",
				HashMapBuilder.put(
					"en_US", title
				).put(
					"es_ES", spanishTitle
				).build()
			).build(),
			ServiceContextTestUtil.getServiceContext());
	}

	private void _assertSimilarAssetSet(
		SimilarAssetSet similarAssetSet, int size, String title,
		String[] titles) {

		Assert.assertEquals(title, similarAssetSet.getTitle());
		Assert.assertEquals(
			size, GetterUtil.getInteger(similarAssetSet.getSize()));

		SimilarAsset[] similarAssets = similarAssetSet.getSimilarAssets();

		Assert.assertEquals(
			Arrays.toString(similarAssets), titles.length,
			similarAssets.length);

		for (int i = 0; i < titles.length; i++) {
			SimilarAsset similarAssetSetAsset = similarAssets[i];

			Assert.assertEquals(titles[i], similarAssetSetAsset.getTitle());
		}
	}

	private void _assertSimilarAssetSetTitles(
		Page<SimilarAssetSet> similarAssetSetsPage, String... titles) {

		List<SimilarAssetSet> similarAssetSets =
			(List<SimilarAssetSet>)similarAssetSetsPage.getItems();

		Assert.assertEquals(
			similarAssetSets.toString(), titles.length,
			similarAssetSets.size());

		for (int i = 0; i < titles.length; i++) {
			SimilarAssetSet similarAssetSet = similarAssetSets.get(i);

			Assert.assertEquals(titles[i], similarAssetSet.getTitle());
		}
	}

	private void _assertSortedByDateModified(
		List<SimilarAssetSet> similarAssetSets, boolean ascending) {

		Date previousDateModified = null;

		for (SimilarAssetSet similarAssetSet : similarAssetSets) {
			Date dateModified = _getMaxDateModified(similarAssetSet);

			Assert.assertNotNull(dateModified);

			if (previousDateModified != null) {
				if (ascending) {
					Assert.assertFalse(
						dateModified.before(previousDateModified));
				}
				else {
					Assert.assertFalse(
						dateModified.after(previousDateModified));
				}
			}

			previousDateModified = dateModified;
		}
	}

	private String _getAdminUserEmailAddress() throws Exception {
		User user = UserTestUtil.getAdminUser(testCompany.getCompanyId());

		return user.getEmailAddress();
	}

	private ObjectDefinition _getBasicWebContentObjectDefinition()
		throws Exception {

		Group cmsGroup = _groupLocalService.getGroup(
			TestPropsValues.getCompanyId(), GroupConstants.CMS);

		return _objectDefinitionLocalService.
			getObjectDefinitionByExternalReferenceCode(
				"L_CMS_BASIC_WEB_CONTENT", cmsGroup.getCompanyId());
	}

	private Date _getMaxDateModified(SimilarAssetSet similarAssetSet) {
		Date maxDateModified = null;

		for (SimilarAsset similarAsset : similarAssetSet.getSimilarAssets()) {
			Date dateModified = similarAsset.getDateModified();

			if ((maxDateModified == null) ||
				dateModified.after(maxDateModified)) {

				maxDateModified = dateModified;
			}
		}

		return maxDateModified;
	}

	private Page<SimilarAssetSet> _getSimilarAssetSetsPage(
			long groupId, Pagination pagination, String search,
			String sortString)
		throws Exception {

		return similarAssetSetResource.getSimilarAssetSetsPage(
			groupId, search, pagination, sortString);
	}

	private void _testGetSimilarAssetSetsPageGraphQL() throws Exception {
		DepotEntry depotEntry = _addSpaceDepotEntry(
			ServiceContextTestUtil.getServiceContext());

		ObjectDefinition objectDefinition =
			_getBasicWebContentObjectDefinition();

		_addObjectEntry(
			depotEntry, objectDefinition, _SIMILAR_TITLE, _SIMILAR_CONTENT);
		_addObjectEntry(
			depotEntry, objectDefinition, _SIMILAR_TITLE,
			_SIMILAR_CONTENT + " You can also contact support for help.");

		GraphQLField graphQLField = new GraphQLField(
			"similarAssetSets",
			HashMapBuilder.<String, Object>put(
				"assetLibraryId", "\"" + depotEntry.getGroupId() + "\""
			).build(),
			new GraphQLField(
				"items", new GraphQLField("size"), new GraphQLField("title")),
			new GraphQLField("totalCount"));

		JSONObject similarAssetSetsPageJSONObject =
			JSONUtil.getValueAsJSONObject(
				invokeGraphQLQuery(graphQLField), "JSONObject/data",
				"JSONObject/similarAssetSets");

		Assert.assertEquals(
			2, similarAssetSetsPageJSONObject.getLong("totalCount"));

		JSONArray similarAssetSetsJSONArray =
			similarAssetSetsPageJSONObject.getJSONArray("items");

		Assert.assertEquals(
			similarAssetSetsJSONArray.toString(), 1,
			similarAssetSetsJSONArray.length());

		JSONObject similarAssetSetJSONObject =
			similarAssetSetsJSONArray.getJSONObject(0);

		Assert.assertEquals(2, similarAssetSetJSONObject.getInt("size"));
		Assert.assertEquals(
			_SIMILAR_TITLE, similarAssetSetJSONObject.getString("title"));

		_depotEntryLocalService.deleteDepotEntry(depotEntry.getDepotEntryId());
	}

	private void _testGetSimilarAssetSetsPagePermissions() throws Exception {
		DepotEntry depotEntry = _addSpaceDepotEntry(
			ServiceContextTestUtil.getServiceContext());

		long groupId = depotEntry.getGroupId();

		ObjectDefinition objectDefinition =
			_getBasicWebContentObjectDefinition();

		ObjectEntry objectEntry = _addObjectEntry(
			depotEntry, objectDefinition, _SIMILAR_TITLE, _SIMILAR_CONTENT);

		_addObjectEntry(
			depotEntry, objectDefinition, _SIMILAR_TITLE,
			_SIMILAR_CONTENT + " You can also contact support for help.");

		Page<SimilarAssetSet> similarAssetSetsPage = _getSimilarAssetSetsPage(
			groupId, null, null, null);

		Assert.assertEquals(2, similarAssetSetsPage.getTotalCount());

		List<SimilarAssetSet> similarAssetSets =
			(List<SimilarAssetSet>)similarAssetSetsPage.getItems();

		_assertSimilarAssetSet(
			similarAssetSets.get(0), 2, _SIMILAR_TITLE,
			new String[] {_SIMILAR_TITLE, _SIMILAR_TITLE});

		// The CMS grants VIEW on every content to the user role and to the
		// space member role, so hiding one asset takes revoking both

		for (String name :
				new String[] {
					DepotRolesConstants.ASSET_LIBRARY_MEMBER, RoleConstants.USER
				}) {

			Role role = _roleLocalService.getRole(
				testCompany.getCompanyId(), name);

			_resourcePermissionLocalService.setResourcePermissions(
				testCompany.getCompanyId(), objectDefinition.getClassName(),
				ResourceConstants.SCOPE_INDIVIDUAL,
				String.valueOf(objectEntry.getObjectEntryId()),
				role.getRoleId(), new String[0]);
		}

		String password = RandomTestUtil.randomString();

		User user = UserTestUtil.addUser(testCompany, password);

		_userLocalService.addGroupUser(groupId, user.getUserId());

		SimilarAssetSetResource userSimilarAssetSetResource =
			SimilarAssetSetResource.builder(
			).authentication(
				user.getEmailAddress(), password
			).endpoint(
				testCompany.getVirtualHostname(),
				PortalUtil.getPortalServerPort(false), "http"
			).locale(
				LocaleUtil.getDefault()
			).build();

		similarAssetSetsPage =
			userSimilarAssetSetResource.getSimilarAssetSetsPage(
				groupId, null, null, null);

		Assert.assertEquals(0, similarAssetSetsPage.getTotalCount());

		similarAssetSets =
			(List<SimilarAssetSet>)similarAssetSetsPage.getItems();

		Assert.assertEquals(
			similarAssetSets.toString(), 0, similarAssetSets.size());

		_userLocalService.deleteUser(user);

		_depotEntryLocalService.deleteDepotEntry(depotEntry.getDepotEntryId());
	}

	private void _testGetSimilarAssetSetsPageSearch() throws Exception {
		DepotEntry depotEntry = _addSpaceDepotEntry(
			ServiceContextTestUtil.getServiceContext());

		long groupId = depotEntry.getGroupId();

		_addObjectEntries(depotEntry);

		Page<SimilarAssetSet> similarAssetSetsPage = _getSimilarAssetSetsPage(
			groupId, null, "press", null);

		Assert.assertEquals(2, similarAssetSetsPage.getTotalCount());

		List<SimilarAssetSet> similarAssetSets =
			(List<SimilarAssetSet>)similarAssetSetsPage.getItems();

		Assert.assertEquals(
			similarAssetSets.toString(), 1, similarAssetSets.size());

		_assertSimilarAssetSet(
			similarAssetSets.get(0), 2, _PRODUCT_LAUNCH_TITLE,
			new String[] {_PRODUCT_LAUNCH_TITLE, _PRODUCT_LAUNCH_TITLE});

		// A set narrowed down to one asset keeps its full size and its name

		similarAssetSetsPage = _getSimilarAssetSetsPage(
			groupId, null, "summer highlights", null);

		Assert.assertEquals(1, similarAssetSetsPage.getTotalCount());

		similarAssetSets =
			(List<SimilarAssetSet>)similarAssetSetsPage.getItems();

		Assert.assertEquals(
			similarAssetSets.toString(), 1, similarAssetSets.size());

		_assertSimilarAssetSet(
			similarAssetSets.get(0), 3, "Summer Sale",
			new String[] {"Summer Sale Highlights"});

		_depotEntryLocalService.deleteDepotEntry(depotEntry.getDepotEntryId());
	}

	private void _testGetSimilarAssetSetsPageTranslation() throws Exception {
		DepotEntry depotEntry = _addSpaceDepotEntry(
			ServiceContextTestUtil.getServiceContext());

		long groupId = depotEntry.getGroupId();

		ObjectDefinition objectDefinition =
			_getBasicWebContentObjectDefinition();

		_addTranslatedObjectEntry(
			depotEntry, objectDefinition, _SIMILAR_TITLE, _SIMILAR_CONTENT,
			"Oferta de Verano Grande", _SPANISH_SUMMER_SALE_CONTENT);
		_addTranslatedObjectEntry(
			depotEntry, objectDefinition, RandomTestUtil.randomString(),
			_DISTINCT_CONTENT, "Oferta de Verano 2026",
			_SPANISH_SUMMER_SALE_CONTENT + " La oferta acaba el domingo.");

		SimilarAssetSetResource spanishSimilarAssetSetResource =
			SimilarAssetSetResource.builder(
			).authentication(
				_getAdminUserEmailAddress(), PropsValues.DEFAULT_ADMIN_PASSWORD
			).endpoint(
				testCompany.getVirtualHostname(),
				PortalUtil.getPortalServerPort(false), "http"
			).locale(
				LocaleUtil.SPAIN
			).build();

		Page<SimilarAssetSet> similarAssetSetsPage =
			spanishSimilarAssetSetResource.getSimilarAssetSetsPage(
				groupId, null, null, null);

		Assert.assertEquals(2, similarAssetSetsPage.getTotalCount());

		List<SimilarAssetSet> similarAssetSets =
			(List<SimilarAssetSet>)similarAssetSetsPage.getItems();

		Assert.assertEquals(
			similarAssetSets.toString(), 1, similarAssetSets.size());

		_assertSimilarAssetSet(
			similarAssetSets.get(0), 2, "Oferta de Verano",
			new String[] {"Oferta de Verano Grande", "Oferta de Verano 2026"});

		similarAssetSetsPage = _getSimilarAssetSetsPage(
			groupId, null, null, null);

		Assert.assertEquals(0, similarAssetSetsPage.getTotalCount());

		similarAssetSets =
			(List<SimilarAssetSet>)similarAssetSetsPage.getItems();

		Assert.assertEquals(
			similarAssetSets.toString(), 0, similarAssetSets.size());

		_depotEntryLocalService.deleteDepotEntry(depotEntry.getDepotEntryId());
	}

	private static final String _DISTINCT_CONTENT =
		"The quarterly sales report shows strong revenue growth across the " +
			"European market with product categories in retail and wholesale " +
				"increasing during the last fiscal period.";

	private static final String _PRODUCT_LAUNCH_CONTENT =
		"The new generation of our platform is available today with a " +
			"redesigned workspace faster search and a set of integrations " +
				"that our customers have been asking for during this year.";

	private static final String _PRODUCT_LAUNCH_TITLE =
		"Product Launch Press Release";

	private static final String _SIMILAR_CONTENT =
		"If you forgot your password go to the login page and click the " +
			"forgot password link enter your email address and you will " +
				"receive an email with instructions to create a new password.";

	private static final String _SIMILAR_TITLE = "Reset Your Password";

	private static final String _SPANISH_SUMMER_SALE_CONTENT =
		"Nuestras rebajas de verano traen descuentos de hasta el cincuenta " +
			"por ciento en toda la coleccion de exterior incluidas tiendas " +
				"mochilas y botas de montana mientras queden existencias.";

	private static final String _SUMMER_SALE_CONTENT =
		"Our summer sale brings discounts of up to fifty percent on every " +
			"outdoor collection including tents backpacks and hiking boots " +
				"while stock lasts in all of our stores.";

	@Inject
	private DepotEntryLocalService _depotEntryLocalService;

	@Inject
	private GroupLocalService _groupLocalService;

	@Inject
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Inject
	private ObjectEntryFolderLocalService _objectEntryFolderLocalService;

	@Inject
	private ObjectEntryLocalService _objectEntryLocalService;

	@Inject
	private ResourcePermissionLocalService _resourcePermissionLocalService;

	@Inject
	private RoleLocalService _roleLocalService;

	@Inject
	private UserLocalService _userLocalService;

}