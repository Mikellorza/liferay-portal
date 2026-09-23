/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.invitation.invite.members.web.internal.portlet.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.invitation.invite.members.constants.InviteMembersConstants;
import com.liferay.invitation.invite.members.constants.InviteMembersPortletKeys;
import com.liferay.invitation.invite.members.model.MemberRequest;
import com.liferay.invitation.invite.members.service.MemberRequestLocalService;
import com.liferay.layout.test.util.ContentLayoutTestUtil;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.Team;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.TeamLocalService;
import com.liferay.portal.kernel.test.context.ContextUserReplace;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletActionRequest;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletActionResponse;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.RoleTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.log.LogCapture;
import com.liferay.portal.test.log.LogEntry;
import com.liferay.portal.test.log.LoggerTestUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import jakarta.portlet.ActionRequest;
import jakarta.portlet.Portlet;

import java.util.List;

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
public class InviteMembersPortletTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();
	}

	@Test
	public void testProcessAction() throws Exception {
		User user = UserTestUtil.addUser(_group.getGroupId());

		User receiverUser = UserTestUtil.addUser();

		_processAction(0, 0, receiverUser, user);

		Assert.assertTrue(
			_memberRequestLocalService.hasPendingMemberRequest(
				_group.getGroupId(), receiverUser.getUserId()));

		Role siteAdministratorRole = _roleLocalService.getRole(
			_group.getCompanyId(), RoleConstants.SITE_ADMINISTRATOR);

		_assertProcessActionFails(
			PrincipalException.class, siteAdministratorRole.getRoleId(), 0,
			user);

		Role role = RoleTestUtil.addRole(RoleConstants.TYPE_SITE);

		_assertProcessActionFails(
			PrincipalException.class, role.getRoleId(), 0, user);

		Team team = _teamLocalService.addTeam(
			TestPropsValues.getUserId(), _group.getGroupId(),
			RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			ServiceContextTestUtil.getServiceContext(_group.getGroupId()));

		_assertProcessActionFails(
			PrincipalException.MustHavePermission.class, 0, team.getTeamId(),
			user);

		user = UserTestUtil.addGroupAdminUser(_group);

		receiverUser = UserTestUtil.addUser();

		_processAction(role.getRoleId(), 0, receiverUser, user);

		MemberRequest memberRequest =
			_memberRequestLocalService.getMemberRequest(
				_group.getGroupId(), receiverUser.getUserId(),
				InviteMembersConstants.STATUS_PENDING);

		Assert.assertEquals(role.getRoleId(), memberRequest.getInvitedRoleId());

		receiverUser = UserTestUtil.addUser();

		_processAction(0, team.getTeamId(), receiverUser, user);

		memberRequest = _memberRequestLocalService.getMemberRequest(
			_group.getGroupId(), receiverUser.getUserId(),
			InviteMembersConstants.STATUS_PENDING);

		Assert.assertEquals(team.getTeamId(), memberRequest.getInvitedTeamId());

		_otherGroup = GroupTestUtil.addGroup();

		team = _teamLocalService.addTeam(
			TestPropsValues.getUserId(), _otherGroup.getGroupId(),
			RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			ServiceContextTestUtil.getServiceContext(_otherGroup.getGroupId()));

		_assertProcessActionFails(
			PrincipalException.class, 0, team.getTeamId(), user);
	}

	private void _assertProcessActionFails(
			Class<? extends PrincipalException> exceptionClass,
			long invitedRoleId, long invitedTeamId, User user)
		throws Exception {

		User receiverUser = UserTestUtil.addUser();

		try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				"com.liferay.invitation.invite.members.web.internal.portlet." +
					"InviteMembersPortlet",
				LoggerTestUtil.WARN)) {

			_processAction(invitedRoleId, invitedTeamId, receiverUser, user);

			List<LogEntry> logEntries = logCapture.getLogEntries();

			Assert.assertEquals(logEntries.toString(), 1, logEntries.size());

			LogEntry logEntry = logEntries.get(0);

			Throwable throwable = logEntry.getThrowable();

			Assert.assertEquals(exceptionClass, throwable.getClass());
		}

		Assert.assertFalse(
			_memberRequestLocalService.hasPendingMemberRequest(
				_group.getGroupId(), receiverUser.getUserId()));
	}

	private ThemeDisplay _getThemeDisplay(
			PermissionChecker permissionChecker, User user)
		throws Exception {

		ThemeDisplay themeDisplay = ContentLayoutTestUtil.getThemeDisplay(
			_companyLocalService.getCompany(_group.getCompanyId()), _group,
			LayoutTestUtil.addTypePortletLayout(_group));

		themeDisplay.setPermissionChecker(permissionChecker);
		themeDisplay.setRealUser(user);
		themeDisplay.setSignedIn(true);
		themeDisplay.setUser(user);

		return themeDisplay;
	}

	private void _processAction(
			long invitedRoleId, long invitedTeamId, User receiverUser,
			User user)
		throws Exception {

		PermissionChecker permissionChecker =
			PermissionCheckerFactoryUtil.create(user);

		try (ContextUserReplace contextUserReplace = new ContextUserReplace(
				user, permissionChecker)) {

			MockLiferayPortletActionRequest mockLiferayPortletActionRequest =
				new MockLiferayPortletActionRequest();

			mockLiferayPortletActionRequest.setAttribute(
				WebKeys.THEME_DISPLAY,
				_getThemeDisplay(permissionChecker, user));
			mockLiferayPortletActionRequest.setParameter(
				ActionRequest.ACTION_NAME, "sendInvites");
			mockLiferayPortletActionRequest.setParameter(
				"groupId", String.valueOf(_group.getGroupId()));
			mockLiferayPortletActionRequest.setParameter(
				"invitedRoleId", String.valueOf(invitedRoleId));
			mockLiferayPortletActionRequest.setParameter(
				"invitedTeamId", String.valueOf(invitedTeamId));
			mockLiferayPortletActionRequest.setParameter(
				"receiverEmailAddresses", "");
			mockLiferayPortletActionRequest.setParameter(
				"receiverUserIds", String.valueOf(receiverUser.getUserId()));

			_portlet.processAction(
				mockLiferayPortletActionRequest,
				new MockLiferayPortletActionResponse());
		}
	}

	@Inject
	private CompanyLocalService _companyLocalService;

	@DeleteAfterTestRun
	private Group _group;

	@Inject
	private MemberRequestLocalService _memberRequestLocalService;

	@DeleteAfterTestRun
	private Group _otherGroup;

	@Inject(
		filter = "jakarta.portlet.name=" + InviteMembersPortletKeys.INVITE_MEMBERS
	)
	private Portlet _portlet;

	@Inject
	private RoleLocalService _roleLocalService;

	@Inject
	private TeamLocalService _teamLocalService;

}