/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.friendly.url.web.internal.portlet.action;

import com.liferay.configuration.admin.constants.ConfigurationAdminPortletKeys;
import com.liferay.friendly.url.configuration.manager.FriendlyURLSeparatorConfigurationManager;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.portlet.FriendlyURLResolver;
import com.liferay.portal.kernel.portlet.FriendlyURLResolverRegistryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Mikel Lorza
 */
@Component(
	property = {
		"javax.portlet.name=" + ConfigurationAdminPortletKeys.INSTANCE_SETTINGS,
		"mvc.command.name=/instance_settings/friendly_url_separator_save_company_configuration"
	},
	service = MVCActionCommand.class
)
public class FriendlyURLSeparatorSaveCompanyConfigurationMVCActionCommand
	extends BaseMVCActionCommand {

	@Override
	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		PermissionChecker permissionChecker =
			themeDisplay.getPermissionChecker();

		if (!permissionChecker.isCompanyAdmin(themeDisplay.getCompanyId())) {
			PrincipalException principalException =
				new PrincipalException.MustBeCompanyAdmin(
					permissionChecker.getUserId());

			throw new PortletException(principalException);
		}

		_friendlyURLSeparatorConfigurationManager.
			updateFriendlyURLSeparatorCompanyConfiguration(
				themeDisplay.getCompanyId(),
				_getFriendlyURLSeparators(actionRequest));

		SessionMessages.add(
			actionRequest, "requestProcessed",
			_language.get(
				themeDisplay.getLocale(),
				"your-request-completed-successfully"));

		sendRedirect(actionRequest, actionResponse);
	}

	private String _getFriendlyURLSeparators(ActionRequest actionRequest) {
		JSONArray jsonArray = _jsonFactory.createJSONArray();

		for (FriendlyURLResolver friendlyURLResolver :
				FriendlyURLResolverRegistryUtil.
					getFriendlyURLResolversAsCollection()) {

			if (!friendlyURLResolver.isURLSeparatorConfigurable()) {
				continue;
			}

			jsonArray.put(
				() -> {
					String urlSeparator = ParamUtil.getString(
						actionRequest, friendlyURLResolver.getType());

					if (Validator.isBlank(urlSeparator)) {
						return null;
					}

					if (!urlSeparator.startsWith(StringPool.SLASH)) {
						urlSeparator = StringPool.SLASH + urlSeparator;
					}

					if (!urlSeparator.endsWith(StringPool.SLASH)) {
						urlSeparator = urlSeparator + StringPool.SLASH;
					}

					return JSONUtil.put(
						"type", friendlyURLResolver.getType()
					).put(
						"urlSeparator", urlSeparator
					);
				});
		}

		return jsonArray.toString();
	}

	@Reference
	private FriendlyURLSeparatorConfigurationManager
		_friendlyURLSeparatorConfigurationManager;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private Language _language;

}