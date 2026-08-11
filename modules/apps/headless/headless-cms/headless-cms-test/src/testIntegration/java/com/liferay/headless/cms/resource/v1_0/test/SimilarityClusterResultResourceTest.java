/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.cms.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.depot.constants.DepotConstants;
import com.liferay.depot.model.DepotEntry;
import com.liferay.depot.service.DepotEntryLocalService;
import com.liferay.headless.cms.client.dto.v1_0.SimilarityCluster;
import com.liferay.headless.cms.client.dto.v1_0.SimilarityClusterAsset;
import com.liferay.headless.cms.client.dto.v1_0.SimilarityClusterResult;
import com.liferay.headless.cms.client.pagination.Pagination;
import com.liferay.headless.cms.client.problem.Problem;
import com.liferay.headless.cms.client.resource.v1_0.SimilarityClusterResultResource;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectEntryFolder;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryFolderLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.log.LogCapture;
import com.liferay.portal.test.log.LoggerTestUtil;
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
@RunWith(Arquillian.class)
public class SimilarityClusterResultResourceTest
	extends BaseSimilarityClusterResultResourceTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Override
	@Test
	public void testGetSimilarityCluster() throws Exception {
		DepotEntry depotEntry = _addSpaceDepotEntry(
			ServiceContextTestUtil.getServiceContext());

		long groupId = depotEntry.getGroupId();

		ObjectDefinition objectDefinition =
			_getBasicWebContentObjectDefinition();

		// No content yet

		SimilarityClusterResult similarityClusterResult = _getSimilarityCluster(
			groupId, null, null, null);

		Assert.assertEquals(
			0, GetterUtil.getLong(similarityClusterResult.getTotalCount()));

		SimilarityCluster[] similarityClusters =
			similarityClusterResult.getSimilarityClusters();

		Assert.assertEquals(
			Arrays.toString(similarityClusters), 0, similarityClusters.length);

		// Two near-duplicate assets and one distinct asset

		ObjectEntry nearDuplicateObjectEntry1 = _addObjectEntry(
			depotEntry, objectDefinition, "Reset Your Password",
			_NEAR_DUPLICATE_CONTENT);
		ObjectEntry nearDuplicateObjectEntry2 = _addObjectEntry(
			depotEntry, objectDefinition, "Reset Your Password",
			_NEAR_DUPLICATE_CONTENT +
				" You can also contact support for help.");

		_addObjectEntry(
			depotEntry, objectDefinition, "Quarterly Sales Report",
			_DISTINCT_CONTENT);

		similarityClusterResult = _getSimilarityCluster(
			groupId, null, null, null);

		Assert.assertEquals(
			2, GetterUtil.getLong(similarityClusterResult.getTotalCount()));

		similarityClusters = similarityClusterResult.getSimilarityClusters();

		Assert.assertEquals(
			Arrays.toString(similarityClusters), 1, similarityClusters.length);

		SimilarityCluster similarityCluster = similarityClusters[0];

		Assert.assertEquals(
			"Reset Your Password", similarityCluster.getTitle());
		Assert.assertEquals(
			2, GetterUtil.getInteger(similarityCluster.getSize()));

		SimilarityClusterAsset[] similarityClusterAssets =
			similarityCluster.getSimilarityClusterAssets();

		Assert.assertEquals(
			Arrays.toString(similarityClusterAssets), 2,
			similarityClusterAssets.length);

		List<Long> objectEntryIds = new ArrayList<>();

		int topAssetCount = 0;

		for (SimilarityClusterAsset similarityClusterAsset :
				similarityClusterAssets) {

			objectEntryIds.add(similarityClusterAsset.getId());

			Assert.assertEquals(
				"Reset Your Password", similarityClusterAsset.getTitle());
			Assert.assertNotNull(similarityClusterAsset.getContentType());
			Assert.assertNotNull(similarityClusterAsset.getDateModified());

			// The edit URL the listing links its action to

			String itemURL = similarityClusterAsset.getItemURL();

			Assert.assertTrue(
				itemURL,
				itemURL.endsWith(
					"/cms/edit_content_item?objectEntryId=" +
						similarityClusterAsset.getId()));

			if (GetterUtil.getBoolean(similarityClusterAsset.getTopAsset())) {
				topAssetCount++;

				Assert.assertNull(
					similarityClusterAsset.getSimilarityPercent());
			}
			else {
				double similarityPercent = GetterUtil.getDouble(
					similarityClusterAsset.getSimilarityPercent());

				Assert.assertTrue(
					"Similarity percent must be in (0, 100], was " +
						similarityPercent,
					(similarityPercent > 0) && (similarityPercent <= 100));
			}
		}

		Assert.assertEquals(1, topAssetCount);
		Assert.assertTrue(
			objectEntryIds.contains(
				nearDuplicateObjectEntry1.getObjectEntryId()));
		Assert.assertTrue(
			objectEntryIds.contains(
				nearDuplicateObjectEntry2.getObjectEntryId()));

		// A dimension that names nothing is rejected, so that a client typo is
		// not indistinguishable from "no duplicates". The exception mapper logs
		// every web application exception at error level, 4xx included, so the
		// log has to be captured for the log assertion test rule to pass.

		try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				"com.liferay.portal.vulcan.internal.jaxrs.exception.mapper." +
					"WebApplicationExceptionMapper",
				LoggerTestUtil.ERROR)) {

			similarityClusterResultResource.getSimilarityCluster(
				groupId, "TITLE", null, null, null);

			Assert.fail();
		}
		catch (Problem.ProblemException problemException) {
			Problem problem = problemException.getProblem();

			Assert.assertEquals("BAD_REQUEST", problem.getStatus());
		}

		// The dimension is matched exactly

		try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				"com.liferay.portal.vulcan.internal.jaxrs.exception.mapper." +
					"WebApplicationExceptionMapper",
				LoggerTestUtil.ERROR)) {

			similarityClusterResultResource.getSimilarityCluster(
				groupId, "text", null, null, null);

			Assert.fail();
		}
		catch (Problem.ProblemException problemException) {
			Problem problem = problemException.getProblem();

			Assert.assertEquals("BAD_REQUEST", problem.getStatus());
		}

		_depotEntryLocalService.deleteDepotEntry(depotEntry.getDepotEntryId());

		_testGetSimilarityClusterPage();
		_testGetSimilarityClusterSearch();
		_testGetSimilarityClusterSort();
		_testGetSimilarityClusterTranslation();
	}

	@Override
	@Test
	public void testGraphQLGetSimilarityCluster() throws Exception {
	}

	private ObjectEntry _addObjectEntry(
			DepotEntry depotEntry, ObjectDefinition objectDefinition,
			String title, String content)
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
			ServiceContextTestUtil.getServiceContext());
	}

	private void _addSimilarityClusters(DepotEntry depotEntry)
		throws Exception {

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
			depotEntry, objectDefinition, "Quarterly Sales Report",
			_DISTINCT_CONTENT);
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

	private void _assertSimilarityCluster(
		SimilarityCluster similarityCluster, String title, int size,
		String[] titles) {

		Assert.assertEquals(title, similarityCluster.getTitle());
		Assert.assertEquals(
			size, GetterUtil.getInteger(similarityCluster.getSize()));

		SimilarityClusterAsset[] similarityClusterAssets =
			similarityCluster.getSimilarityClusterAssets();

		Assert.assertEquals(
			Arrays.toString(similarityClusterAssets), titles.length,
			similarityClusterAssets.length);

		for (int i = 0; i < titles.length; i++) {
			SimilarityClusterAsset similarityClusterAsset =
				similarityClusterAssets[i];

			Assert.assertEquals(titles[i], similarityClusterAsset.getTitle());
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

	private SimilarityClusterResult _getSimilarityCluster(
			long groupId, String search, Pagination pagination,
			String sortString)
		throws Exception {

		return similarityClusterResultResource.getSimilarityCluster(
			groupId, "TEXT", search, pagination, sortString);
	}

	private void _testGetSimilarityClusterPage() throws Exception {
		DepotEntry depotEntry = _addSpaceDepotEntry(
			ServiceContextTestUtil.getServiceContext());

		long groupId = depotEntry.getGroupId();

		_addSimilarityClusters(depotEntry);

		// The first page carries only the window's assets, while the cluster
		// still reports its full size

		SimilarityClusterResult similarityClusterResult = _getSimilarityCluster(
			groupId, null, Pagination.of(1, 2), null);

		Assert.assertEquals(
			5, GetterUtil.getLong(similarityClusterResult.getTotalCount()));

		SimilarityCluster[] similarityClusters =
			similarityClusterResult.getSimilarityClusters();

		Assert.assertEquals(
			Arrays.toString(similarityClusters), 1, similarityClusters.length);

		_assertSimilarityCluster(
			similarityClusters[0], "Summer Sale", 3,
			new String[] {"Big Summer Sale", "Summer Sale 2026"});

		// A cluster split across pages is repeated in the next page with the
		// remaining assets

		similarityClusterResult = _getSimilarityCluster(
			groupId, null, Pagination.of(2, 2), null);

		Assert.assertEquals(
			5, GetterUtil.getLong(similarityClusterResult.getTotalCount()));

		similarityClusters = similarityClusterResult.getSimilarityClusters();

		Assert.assertEquals(
			Arrays.toString(similarityClusters), 2, similarityClusters.length);

		_assertSimilarityCluster(
			similarityClusters[0], "Summer Sale", 3,
			new String[] {"Summer Sale Highlights"});
		_assertSimilarityCluster(
			similarityClusters[1], _PRODUCT_LAUNCH_TITLE, 2,
			new String[] {_PRODUCT_LAUNCH_TITLE});

		similarityClusterResult = _getSimilarityCluster(
			groupId, null, Pagination.of(3, 2), null);

		Assert.assertEquals(
			5, GetterUtil.getLong(similarityClusterResult.getTotalCount()));

		similarityClusters = similarityClusterResult.getSimilarityClusters();

		Assert.assertEquals(
			Arrays.toString(similarityClusters), 1, similarityClusters.length);

		_assertSimilarityCluster(
			similarityClusters[0], _PRODUCT_LAUNCH_TITLE, 2,
			new String[] {_PRODUCT_LAUNCH_TITLE});

		_depotEntryLocalService.deleteDepotEntry(depotEntry.getDepotEntryId());
	}

	private void _testGetSimilarityClusterSearch() throws Exception {
		DepotEntry depotEntry = _addSpaceDepotEntry(
			ServiceContextTestUtil.getServiceContext());

		long groupId = depotEntry.getGroupId();

		_addSimilarityClusters(depotEntry);

		// A cluster without matching assets is left out

		SimilarityClusterResult similarityClusterResult = _getSimilarityCluster(
			groupId, "press", null, null);

		Assert.assertEquals(
			2, GetterUtil.getLong(similarityClusterResult.getTotalCount()));

		SimilarityCluster[] similarityClusters =
			similarityClusterResult.getSimilarityClusters();

		Assert.assertEquals(
			Arrays.toString(similarityClusters), 1, similarityClusters.length);

		_assertSimilarityCluster(
			similarityClusters[0], _PRODUCT_LAUNCH_TITLE, 2,
			new String[] {_PRODUCT_LAUNCH_TITLE, _PRODUCT_LAUNCH_TITLE});

		// A surviving cluster keeps its name and its full size

		similarityClusterResult = _getSimilarityCluster(
			groupId, "summer highlights", null, null);

		Assert.assertEquals(
			1, GetterUtil.getLong(similarityClusterResult.getTotalCount()));

		similarityClusters = similarityClusterResult.getSimilarityClusters();

		Assert.assertEquals(
			Arrays.toString(similarityClusters), 1, similarityClusters.length);

		_assertSimilarityCluster(
			similarityClusters[0], "Summer Sale", 3,
			new String[] {"Summer Sale Highlights"});

		_depotEntryLocalService.deleteDepotEntry(depotEntry.getDepotEntryId());
	}

	private void _testGetSimilarityClusterSort() throws Exception {
		DepotEntry depotEntry = _addSpaceDepotEntry(
			ServiceContextTestUtil.getServiceContext());

		long groupId = depotEntry.getGroupId();

		_addSimilarityClusters(depotEntry);

		// The biggest cluster comes first by default

		SimilarityClusterResult similarityClusterResult = _getSimilarityCluster(
			groupId, null, null, null);

		Assert.assertEquals(
			5, GetterUtil.getLong(similarityClusterResult.getTotalCount()));

		SimilarityCluster[] similarityClusters =
			similarityClusterResult.getSimilarityClusters();

		Assert.assertEquals(
			Arrays.toString(similarityClusters), 2, similarityClusters.length);

		_assertSimilarityCluster(
			similarityClusters[0], "Summer Sale", 3,
			new String[] {
				"Big Summer Sale", "Summer Sale 2026", "Summer Sale Highlights"
			});
		_assertSimilarityCluster(
			similarityClusters[1], _PRODUCT_LAUNCH_TITLE, 2,
			new String[] {_PRODUCT_LAUNCH_TITLE, _PRODUCT_LAUNCH_TITLE});

		// Sorting by title orders both the clusters and their assets

		similarityClusterResult = _getSimilarityCluster(
			groupId, null, null, "title:asc");

		similarityClusters = similarityClusterResult.getSimilarityClusters();

		Assert.assertEquals(
			Arrays.toString(similarityClusters), 2, similarityClusters.length);

		_assertSimilarityCluster(
			similarityClusters[0], _PRODUCT_LAUNCH_TITLE, 2,
			new String[] {_PRODUCT_LAUNCH_TITLE, _PRODUCT_LAUNCH_TITLE});
		_assertSimilarityCluster(
			similarityClusters[1], "Summer Sale", 3,
			new String[] {
				"Big Summer Sale", "Summer Sale 2026", "Summer Sale Highlights"
			});

		similarityClusterResult = _getSimilarityCluster(
			groupId, null, null, "title:desc");

		similarityClusters = similarityClusterResult.getSimilarityClusters();

		Assert.assertEquals(
			Arrays.toString(similarityClusters), 2, similarityClusters.length);

		_assertSimilarityCluster(
			similarityClusters[0], "Summer Sale", 3,
			new String[] {
				"Summer Sale Highlights", "Summer Sale 2026", "Big Summer Sale"
			});
		_assertSimilarityCluster(
			similarityClusters[1], _PRODUCT_LAUNCH_TITLE, 2,
			new String[] {_PRODUCT_LAUNCH_TITLE, _PRODUCT_LAUNCH_TITLE});

		// Sorting by modification date walks the assets from the most recently
		// modified one

		similarityClusterResult = _getSimilarityCluster(
			groupId, null, null, "dateModified:desc");

		Date previousDate = null;

		for (SimilarityCluster similarityCluster :
				similarityClusterResult.getSimilarityClusters()) {

			for (SimilarityClusterAsset similarityClusterAsset :
					similarityCluster.getSimilarityClusterAssets()) {

				Date dateModified = similarityClusterAsset.getDateModified();

				Assert.assertNotNull(dateModified);

				if (previousDate != null) {
					Assert.assertFalse(
						dateModified + " must not be after " + previousDate,
						dateModified.after(previousDate));
				}

				previousDate = dateModified;
			}
		}

		_depotEntryLocalService.deleteDepotEntry(depotEntry.getDepotEntryId());
	}

	private void _testGetSimilarityClusterTranslation() throws Exception {
		DepotEntry depotEntry = _addSpaceDepotEntry(
			ServiceContextTestUtil.getServiceContext());

		long groupId = depotEntry.getGroupId();

		ObjectDefinition objectDefinition =
			_getBasicWebContentObjectDefinition();

		// Two contents that duplicate each other only in their Spanish
		// translation

		_addTranslatedObjectEntry(
			depotEntry, objectDefinition, "Reset Your Password",
			_NEAR_DUPLICATE_CONTENT, "Oferta de Verano Grande",
			_SPANISH_SUMMER_SALE_CONTENT);
		_addTranslatedObjectEntry(
			depotEntry, objectDefinition, "Quarterly Sales Report",
			_DISTINCT_CONTENT, "Oferta de Verano 2026",
			_SPANISH_SUMMER_SALE_CONTENT + " La oferta acaba el domingo.");

		// The Spanish reader sees the Spanish duplication

		SimilarityClusterResultResource spanishSimilarityClusterResultResource =
			SimilarityClusterResultResource.builder(
			).authentication(
				_getAdminUserEmailAddress(), PropsValues.DEFAULT_ADMIN_PASSWORD
			).endpoint(
				testCompany.getVirtualHostname(),
				PortalUtil.getPortalServerPort(false), "http"
			).locale(
				LocaleUtil.SPAIN
			).build();

		SimilarityClusterResult similarityClusterResult =
			spanishSimilarityClusterResultResource.getSimilarityCluster(
				groupId, "TEXT", null, null, null);

		Assert.assertEquals(
			2, GetterUtil.getLong(similarityClusterResult.getTotalCount()));

		SimilarityCluster[] similarityClusters =
			similarityClusterResult.getSimilarityClusters();

		Assert.assertEquals(
			Arrays.toString(similarityClusters), 1, similarityClusters.length);

		_assertSimilarityCluster(
			similarityClusters[0], "Oferta de Verano", 2,
			new String[] {"Oferta de Verano 2026", "Oferta de Verano Grande"});

		// The English reader sees no duplication, because the English texts
		// differ

		similarityClusterResult = _getSimilarityCluster(
			groupId, null, null, null);

		Assert.assertEquals(
			0, GetterUtil.getLong(similarityClusterResult.getTotalCount()));

		similarityClusters = similarityClusterResult.getSimilarityClusters();

		Assert.assertEquals(
			Arrays.toString(similarityClusters), 0, similarityClusters.length);

		_depotEntryLocalService.deleteDepotEntry(depotEntry.getDepotEntryId());
	}

	private static final String _DISTINCT_CONTENT =
		"The quarterly sales report shows strong revenue growth across the " +
			"European market with product categories in retail and wholesale " +
				"increasing during the last fiscal period.";

	private static final String _NEAR_DUPLICATE_CONTENT =
		"If you forgot your password go to the login page and click the " +
			"forgot password link enter your email address and you will " +
				"receive an email with instructions to create a new password.";

	private static final String _PRODUCT_LAUNCH_CONTENT =
		"The new generation of our platform is available today with a " +
			"redesigned workspace faster search and a set of integrations " +
				"that our customers have been asking for during this year.";

	private static final String _PRODUCT_LAUNCH_TITLE =
		"Product Launch Press Release";

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

}