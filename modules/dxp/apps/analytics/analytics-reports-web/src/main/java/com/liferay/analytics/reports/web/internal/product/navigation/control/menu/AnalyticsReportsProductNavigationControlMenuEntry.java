/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */

package com.liferay.analytics.reports.web.internal.product.navigation.control.menu;

import com.liferay.analytics.reports.constants.AnalyticsReportsWebKeys;
import com.liferay.analytics.reports.info.item.AnalyticsReportsInfoItem;
import com.liferay.analytics.reports.info.item.AnalyticsReportsInfoItemRegistry;
import com.liferay.analytics.reports.info.item.ClassNameClassPKInfoItemIdentifier;
import com.liferay.analytics.reports.info.item.provider.AnalyticsReportsInfoItemObjectProvider;
import com.liferay.analytics.reports.web.internal.constants.AnalyticsReportsPortletKeys;
import com.liferay.analytics.reports.web.internal.info.item.provider.AnalyticsReportsInfoItemObjectProviderRegistry;
import com.liferay.analytics.reports.web.internal.util.AnalyticsReportsUtil;
import com.liferay.analytics.settings.rest.manager.AnalyticsSettingsManager;
import com.liferay.frontend.js.loader.modules.extender.npm.NPMResolver;
import com.liferay.info.item.ClassPKInfoItemIdentifier;
import com.liferay.info.item.InfoItemReference;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.portlet.PortletURLFactory;
import com.liferay.portal.kernel.portlet.url.builder.PortletURLBuilder;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.Html;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.SessionClicks;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.template.react.renderer.ReactRenderer;
import com.liferay.product.navigation.control.menu.BaseJSPProductNavigationControlMenuEntry;
import com.liferay.product.navigation.control.menu.ProductNavigationControlMenuEntry;
import com.liferay.product.navigation.control.menu.constants.ProductNavigationControlMenuCategoryKeys;

import java.io.IOException;

import java.util.Collections;
import java.util.Locale;
import java.util.Objects;

import javax.portlet.PortletRequest;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Sarai Díaz
 */
@Component(
	property = {
		"product.navigation.control.menu.category.key=" + ProductNavigationControlMenuCategoryKeys.USER,
		"product.navigation.control.menu.entry.order:Integer=400"
	},
	service = {
		AnalyticsReportsProductNavigationControlMenuEntry.class,
		ProductNavigationControlMenuEntry.class
	}
)
public class AnalyticsReportsProductNavigationControlMenuEntry
	extends BaseJSPProductNavigationControlMenuEntry {

	@Override
	public String getBodyJspPath() {
		return "/analytics_reports_body.jsp";
	}

	@Override
	public String getIconJspPath() {
		return "/analytics_reports_icon.jsp";
	}

	@Override
	public String getLabel(Locale locale) {
		return null;
	}

	@Override
	public String getURL(HttpServletRequest httpServletRequest) {
		return null;
	}

	@Override
	public boolean includeBody(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse)
		throws IOException {

		httpServletRequest.setAttribute(
			"data",
			HashMapBuilder.<String, Object>put(
				"context",
				Collections.singletonMap(
					"analyticsReportsDataURL",
					_getAnalyticsReportsURL(httpServletRequest))
			).put(
				"portletNamespace", _portletNamespace
			).build());

		httpServletRequest.setAttribute(
			"isPanelStateOpen", isPanelStateOpen(httpServletRequest));

		return include(
			httpServletRequest, httpServletResponse, getBodyJspPath());
	}

	@Override
	public boolean includeIcon(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse)
		throws IOException {

		httpServletRequest.setAttribute(
			"isPanelStateOpen", isPanelStateOpen(httpServletRequest));

		return include(
			httpServletRequest, httpServletResponse, getIconJspPath());
	}

	public boolean isPanelStateOpen(HttpServletRequest httpServletRequest) {
		String analyticsReportsPanelState = SessionClicks.get(
			httpServletRequest, _SESSION_CLICKS_KEY, "closed");

		if (Objects.equals(analyticsReportsPanelState, "open")) {
			return true;
		}

		return false;
	}

	@Override
	public boolean isShow(HttpServletRequest httpServletRequest)
		throws PortalException {

		InfoItemReference infoItemReference = _getInfoItemReference(
			httpServletRequest);

		AnalyticsReportsInfoItemObjectProvider<Object>
			analyticsReportsInfoItemObjectProvider =
				(AnalyticsReportsInfoItemObjectProvider<Object>)
					_analyticsReportsInfoItemObjectProviderRegistry.
						getAnalyticsReportsInfoItemObjectProvider(
							infoItemReference.getClassName());

		if (analyticsReportsInfoItemObjectProvider == null) {
			return false;
		}

		Object analyticsReportsInfoItemObject =
			analyticsReportsInfoItemObjectProvider.
				getAnalyticsReportsInfoItemObject(infoItemReference);

		if (analyticsReportsInfoItemObject == null) {
			return false;
		}

		AnalyticsReportsInfoItem<Object> analyticsReportsInfoItem =
			(AnalyticsReportsInfoItem<Object>)
				_analyticsReportsInfoItemRegistry.getAnalyticsReportsInfoItem(
					infoItemReference.getClassName());

		if ((analyticsReportsInfoItem == null) ||
			!analyticsReportsInfoItem.isShow(analyticsReportsInfoItemObject)) {

			return false;
		}

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		try {
			if (!AnalyticsReportsUtil.isShowAnalyticsReportsPanel(
					_analyticsSettingsManager, themeDisplay.getCompanyId(),
					httpServletRequest)) {

				return false;
			}
		}
		catch (PortalException portalException) {
			throw portalException;
		}
		catch (Exception exception) {
			_log.error(exception);

			return false;
		}

		return super.isShow(httpServletRequest);
	}

	public void setPanelState(
		HttpServletRequest httpServletRequest, String panelState) {

		SessionClicks.put(httpServletRequest, _SESSION_CLICKS_KEY, panelState);
	}

	@Activate
	protected void activate() {
		_portletNamespace = _portal.getPortletNamespace(
			AnalyticsReportsPortletKeys.ANALYTICS_REPORTS);
	}

	@Override
	protected ServletContext getServletContext() {
		return _servletContext;
	}

	private String _getAnalyticsReportsURL(
		HttpServletRequest httpServletRequest) {

		InfoItemReference infoItemReference = _getInfoItemReference(
			httpServletRequest);

		if (infoItemReference.getInfoItemIdentifier() instanceof
				ClassNameClassPKInfoItemIdentifier) {

			ClassNameClassPKInfoItemIdentifier
				classNameClassPKInfoItemIdentifier =
					(ClassNameClassPKInfoItemIdentifier)
						infoItemReference.getInfoItemIdentifier();

			return PortletURLBuilder.create(
				_portletURLFactory.create(
					httpServletRequest,
					AnalyticsReportsPortletKeys.ANALYTICS_REPORTS,
					PortletRequest.RESOURCE_PHASE)
			).setParameter(
				"className", infoItemReference.getClassName()
			).setParameter(
				"classPK", classNameClassPKInfoItemIdentifier.getClassPK()
			).setParameter(
				"classTypeName",
				classNameClassPKInfoItemIdentifier.getClassName()
			).setParameter(
				"p_p_resource_id", "/analytics_reports/get_data"
			).buildString();
		}
		else if (infoItemReference.getInfoItemIdentifier() instanceof
					ClassPKInfoItemIdentifier) {

			return PortletURLBuilder.create(
				_portletURLFactory.create(
					httpServletRequest,
					AnalyticsReportsPortletKeys.ANALYTICS_REPORTS,
					PortletRequest.RESOURCE_PHASE)
			).setParameter(
				"className", infoItemReference.getClassName()
			).setParameter(
				"classPK", infoItemReference.getClassPK()
			).setParameter(
				"p_p_resource_id", "/analytics_reports/get_data"
			).buildString();
		}

		return StringPool.BLANK;
	}

	private InfoItemReference _getInfoItemReference(
		HttpServletRequest httpServletRequest) {

		InfoItemReference infoItemReference =
			(InfoItemReference)httpServletRequest.getAttribute(
				AnalyticsReportsWebKeys.ANALYTICS_INFO_ITEM_REFERENCE);

		if (infoItemReference == null) {
			ThemeDisplay themeDisplay =
				(ThemeDisplay)httpServletRequest.getAttribute(
					WebKeys.THEME_DISPLAY);

			return new InfoItemReference(
				Layout.class.getName(), themeDisplay.getPlid());
		}

		return infoItemReference;
	}

	private static final String _SESSION_CLICKS_KEY =
		"com.liferay.analytics.reports.web_panelState";

	private static final Log _log = LogFactoryUtil.getLog(
		AnalyticsReportsProductNavigationControlMenuEntry.class);

	@Reference
	private AnalyticsReportsInfoItemObjectProviderRegistry
		_analyticsReportsInfoItemObjectProviderRegistry;

	@Reference
	private AnalyticsReportsInfoItemRegistry _analyticsReportsInfoItemRegistry;

	@Reference
	private AnalyticsSettingsManager _analyticsSettingsManager;

	@Reference
	private Html _html;

	@Reference
	private Language _language;

	@Reference
	private NPMResolver _npmResolver;

	@Reference
	private Portal _portal;

	private String _portletNamespace;

	@Reference
	private PortletURLFactory _portletURLFactory;

	@Reference
	private ReactRenderer _reactRenderer;

	@Reference(
		target = "(osgi.web.symbolicname=com.liferay.analytics.reports.web)"
	)
	private ServletContext _servletContext;

}