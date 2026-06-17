/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.bulk.selection.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.bulk.selection.BulkSelectionAction;
import com.liferay.depot.constants.DepotConstants;
import com.liferay.depot.model.DepotEntry;
import com.liferay.depot.service.DepotEntryLocalService;
import com.liferay.object.constants.ObjectDefinitionConstants;
import com.liferay.object.constants.ObjectEntryFolderConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectEntryFolder;
import com.liferay.object.rest.filter.factory.FilterFactory;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryFolderLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.petra.sql.dsl.expression.Predicate;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.ResourcePermission;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.site.cms.site.initializer.test.util.CMSTestUtil;
import com.liferay.site.cms.site.initializer.util.CMSDefaultPermissionUtil;
import com.liferay.site.cms.site.initializer.util.RoleUtil;

import java.io.Serializable;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Mikel Lorza
 */
@FeatureFlag("LPD-17564")
@RunWith(Arquillian.class)
public class DefaultPermissionObjectBulkSelectionActionTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		CMSTestUtil.getOrAddGroup(
			DefaultPermissionObjectBulkSelectionActionTest.class);

		_cmsAdministratorRole = RoleUtil.getOrAddCMSAdministratorRole(
			TestPropsValues.getCompanyId(), TestPropsValues.getUserId());

		DepotEntry depotEntry = _depotEntryLocalService.addDepotEntry(
			HashMapBuilder.put(
				LocaleUtil.getDefault(), StringUtil.randomString()
			).build(),
			HashMapBuilder.put(
				LocaleUtil.getDefault(), StringUtil.randomString()
			).build(),
			DepotConstants.TYPE_SPACE,
			ServiceContextTestUtil.getServiceContext());

		_group = depotEntry.getGroup();

		ServiceContextThreadLocal.pushServiceContext(
			ServiceContextTestUtil.getServiceContext());
	}

	@After
	public void tearDown() throws Exception {
		ServiceContextThreadLocal.popServiceContext();
	}

	@Test
	public void testDoExecutePropagatesResourcePermissions() throws Exception {
		ObjectEntryFolder contentsObjectEntryFolder =
			_objectEntryFolderLocalService.
				getObjectEntryFolderByExternalReferenceCode(
					ObjectEntryFolderConstants.EXTERNAL_REFERENCE_CODE_CONTENTS,
					_group.getGroupId(), _group.getCompanyId());

		ObjectEntryFolder objectEntryFolder = _addObjectEntryFolder(
			contentsObjectEntryFolder.getObjectEntryFolderId());

		ObjectEntry objectEntry = _addBasicWebContentObjectEntry(
			objectEntryFolder);

		String actionId = RandomTestUtil.randomString();

		ObjectEntry defaultPermissionObjectEntry =
			CMSDefaultPermissionUtil.fetchObjectEntry(
				objectEntryFolder.getCompanyId(), objectEntryFolder.getUserId(),
				objectEntryFolder.getExternalReferenceCode(),
				objectEntryFolder.getModelClassName(), _filterFactory);

		Assert.assertNotNull(defaultPermissionObjectEntry);

		ReflectionTestUtil.invoke(
			_defaultPermissionObjectBulkSelectionAction, "doExecute",
			new Class<?>[] {User.class, Map.class, Object.class},
			TestPropsValues.getUser(),
			HashMapBuilder.<String, Serializable>put(
				"defaultPermissions",
				() -> JSONUtil.put(
					ObjectEntryFolderConstants.EXTERNAL_REFERENCE_CODE_CONTENTS,
					JSONUtil.put(
						RoleConstants.CMS_ADMINISTRATOR,
						JSONUtil.putAll(
							actionId, ActionKeys.UPDATE, ActionKeys.VIEW))
				).put(
					"OBJECT_ENTRY_FOLDERS",
					JSONUtil.put(
						RoleConstants.CMS_ADMINISTRATOR,
						JSONUtil.putAll(
							actionId, ActionKeys.UPDATE, ActionKeys.VIEW))
				).toString()
			).build(),
			defaultPermissionObjectEntry);

		ResourcePermission objectEntryFolderResourcePermission =
			_resourcePermissionLocalService.getResourcePermission(
				objectEntryFolder.getCompanyId(),
				ObjectEntryFolder.class.getName(),
				ResourceConstants.SCOPE_INDIVIDUAL,
				String.valueOf(objectEntryFolder.getObjectEntryFolderId()),
				_cmsAdministratorRole.getRoleId());

		Assert.assertFalse(
			objectEntryFolderResourcePermission.hasActionId(ActionKeys.DELETE));
		Assert.assertTrue(
			objectEntryFolderResourcePermission.hasActionId(ActionKeys.UPDATE));
		Assert.assertTrue(
			objectEntryFolderResourcePermission.hasActionId(ActionKeys.VIEW));
		Assert.assertFalse(
			objectEntryFolderResourcePermission.hasActionId(actionId));

		ResourcePermission objectEntryResourcePermission =
			_resourcePermissionLocalService.getResourcePermission(
				objectEntry.getCompanyId(), objectEntry.getModelClassName(),
				ResourceConstants.SCOPE_INDIVIDUAL,
				String.valueOf(objectEntry.getObjectEntryId()),
				_cmsAdministratorRole.getRoleId());

		Assert.assertFalse(
			objectEntryResourcePermission.hasActionId(ActionKeys.DELETE));
		Assert.assertTrue(
			objectEntryResourcePermission.hasActionId(ActionKeys.UPDATE));
		Assert.assertTrue(
			objectEntryResourcePermission.hasActionId(ActionKeys.VIEW));
		Assert.assertFalse(objectEntryResourcePermission.hasActionId(actionId));
	}

	private ObjectEntry _addBasicWebContentObjectEntry(
			ObjectEntryFolder objectEntryFolder)
		throws Exception {

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.
				getObjectDefinitionByExternalReferenceCode(
					"L_CMS_BASIC_WEB_CONTENT",
					objectEntryFolder.getCompanyId());

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext();

		serviceContext.setAttribute(
			"friendlyUrlMap", new HashMap<String, String>());

		return _objectEntryLocalService.addObjectEntry(
			objectEntryFolder.getGroupId(), objectEntryFolder.getUserId(),
			objectDefinition.getObjectDefinitionId(),
			objectEntryFolder.getObjectEntryFolderId(), "en_US",
			HashMapBuilder.<String, Serializable>put(
				"title_i18n",
				HashMapBuilder.put(
					"en_US", RandomTestUtil.randomString()
				).build()
			).build(),
			serviceContext);
	}

	private ObjectEntryFolder _addObjectEntryFolder(
			long parentObjectEntryFolderId)
		throws Exception {

		return _objectEntryFolderLocalService.addObjectEntryFolder(
			RandomTestUtil.randomString(), _group.getGroupId(),
			_group.getCreatorUserId(), parentObjectEntryFolderId, "",
			HashMapBuilder.put(
				LocaleUtil.ENGLISH, RandomTestUtil.randomString()
			).build(),
			RandomTestUtil.randomString(),
			ServiceContextTestUtil.getServiceContext());
	}

	private Role _cmsAdministratorRole;

	@Inject(
		filter = "component.name=com.liferay.site.cms.site.initializer.internal.bulk.selection.DefaultPermissionObjectBulkSelectionAction"
	)
	private BulkSelectionAction<Object>
		_defaultPermissionObjectBulkSelectionAction;

	@Inject
	private DepotEntryLocalService _depotEntryLocalService;

	@Inject(
		filter = "filter.factory.key=" + ObjectDefinitionConstants.STORAGE_TYPE_DEFAULT
	)
	private FilterFactory<Predicate> _filterFactory;

	@DeleteAfterTestRun
	private Group _group;

	@Inject
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Inject
	private ObjectEntryFolderLocalService _objectEntryFolderLocalService;

	@Inject
	private ObjectEntryLocalService _objectEntryLocalService;

	@Inject
	private ResourcePermissionLocalService _resourcePermissionLocalService;

}