/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.page.template.internal.upgrade.v5_4_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.layout.page.template.constants.LayoutPageTemplateEntryTypeConstants;
import com.liferay.layout.page.template.model.LayoutPageTemplateEntry;
import com.liferay.layout.page.template.service.LayoutPageTemplateEntryLocalService;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.ResourcePermission;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.portal.upgrade.registry.UpgradeStepRegistrator;
import com.liferay.portal.upgrade.test.util.UpgradeTestUtil;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Mikel Lorza
 */
@RunWith(Arquillian.class)
public class ResourcePermissionUpgradeProcessTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_company = _companyLocalService.getCompany(
			TestPropsValues.getCompanyId());

		_upgradeProcess = UpgradeTestUtil.getUpgradeStep(
			_upgradeStepRegistrator, _CLASS_NAME);
	}

	@Test
	public void testUpgradeProcessOfLayoutPageTemplateEntryWithoutResourcePermissions()
		throws Exception {

		LayoutPageTemplateEntry layoutPageTemplateEntry =
			_layoutPageTemplateEntryLocalService.addLayoutPageTemplateEntry(
				TestPropsValues.getUserId(), _company.getGroupId(), 0, 0, 0,
				RandomTestUtil.randomString(),
				LayoutPageTemplateEntryTypeConstants.TYPE_WIDGET_PAGE, 0, true,
				0, 0, 0, WorkflowConstants.STATUS_APPROVED,
				new ServiceContext());

		_resourcePermissionLocalService.deleteResourcePermissions(
			_company.getCompanyId(), LayoutPageTemplateEntry.class.getName(),
			ResourceConstants.SCOPE_INDIVIDUAL,
			layoutPageTemplateEntry.getLayoutPageTemplateEntryId());

		_upgradeProcess.upgrade();

		Role role = _roleLocalService.getRole(
			_company.getCompanyId(), RoleConstants.GUEST);

		ResourcePermission resourcePermission =
			_resourcePermissionLocalService.fetchResourcePermission(
				_company.getCompanyId(),
				LayoutPageTemplateEntry.class.getName(),
				ResourceConstants.SCOPE_INDIVIDUAL,
				String.valueOf(
					layoutPageTemplateEntry.getLayoutPageTemplateEntryId()),
				role.getRoleId());

		Assert.assertNotNull(resourcePermission);
		Assert.assertEquals(1, resourcePermission.getActionIds());

		role = _roleLocalService.getRole(
			_company.getCompanyId(), RoleConstants.OWNER);

		resourcePermission =
			_resourcePermissionLocalService.fetchResourcePermission(
				_company.getCompanyId(),
				LayoutPageTemplateEntry.class.getName(),
				ResourceConstants.SCOPE_INDIVIDUAL,
				String.valueOf(
					layoutPageTemplateEntry.getLayoutPageTemplateEntryId()),
				role.getRoleId());

		Assert.assertNotNull(resourcePermission);
		Assert.assertEquals(15, resourcePermission.getActionIds());

		role = _roleLocalService.getRole(
			_company.getCompanyId(), RoleConstants.USER);

		resourcePermission =
			_resourcePermissionLocalService.fetchResourcePermission(
				_company.getCompanyId(),
				LayoutPageTemplateEntry.class.getName(),
				ResourceConstants.SCOPE_INDIVIDUAL,
				String.valueOf(
					layoutPageTemplateEntry.getLayoutPageTemplateEntryId()),
				role.getRoleId());

		Assert.assertNotNull(resourcePermission);
		Assert.assertEquals(1, resourcePermission.getActionIds());
	}

	private static final String _CLASS_NAME =
		"com.liferay.layout.page.template.internal.upgrade.v5_4_0." +
			"ResourcePermissionUpgradeProcess";

	@Inject
	private static CompanyLocalService _companyLocalService;

	@Inject(
		filter = "(&(component.name=com.liferay.layout.page.template.internal.upgrade.registry.LayoutPageTemplateServiceUpgradeStepRegistrator))"
	)
	private static UpgradeStepRegistrator _upgradeStepRegistrator;

	private Company _company;

	@Inject
	private LayoutPageTemplateEntryLocalService
		_layoutPageTemplateEntryLocalService;

	@Inject
	private ResourcePermissionLocalService _resourcePermissionLocalService;

	@Inject
	private RoleLocalService _roleLocalService;

	private UpgradeProcess _upgradeProcess;

}