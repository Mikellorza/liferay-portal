/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.layout.reports.web.internal.struts;

import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.servlet.ServletResponseUtil;
import com.liferay.portal.kernel.struts.StrutsAction;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HttpComponentsUtil;
import com.liferay.portal.kernel.util.WebKeys;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Mikel Lorza
 */
@Component(
	property = "path=/layout_reports/get_layout_reports_tabs",
	service = StrutsAction.class
)
public class GetLayoutReportTabsStrutsAction implements StrutsAction {

	@Override
	public String execute(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse)
		throws Exception {

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		ServletResponseUtil.write(
			httpServletResponse,
			JSONUtil.putAll(
				JSONUtil.put(
					"id", "render-times"
				).put(
					"name",
					_language.get(themeDisplay.getLocale(), "render-times")
				).put(
					"url",
					() -> HttpComponentsUtil.addParameters(
						themeDisplay.getPortalURL() +
							themeDisplay.getPathMain() +
								"/layout_reports/get_render_times_data",
						"p_l_id", themeDisplay.getPlid())
				),
				JSONUtil.put(
					"id", "page-speed-insights"
				).put(
					"name",
					_language.get(
						themeDisplay.getLocale(), "page-speed-insights")
				).put(
					"url",
					HttpComponentsUtil.addParameters(
						themeDisplay.getPortalURL() +
							themeDisplay.getPathMain() +
								"/layout_reports/get_layout_reports_data",
						"p_l_id", themeDisplay.getPlid())
				)
			).toString());

		return null;
	}

	@Reference
	private Language _language;

}