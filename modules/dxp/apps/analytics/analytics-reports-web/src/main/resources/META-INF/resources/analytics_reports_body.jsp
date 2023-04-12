<%--
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
--%>

<%@ include file="/init.jsp" %>

<%
String panelState = "";

if ((Boolean)request.getAttribute("isPanelStateOpen"))
	panelState = "lfr-has-analytics-reports-panel open-admin-panel ";
%>

<liferay-util:body-bottom
	outputKey="analyticsReportsPanel"
>
	<div class="<%= panelState %> cadmin d-print-none lfr-admin-panel lfr-analytics-reports-panel lfr-product-menu-panel sidenav-fixed sidenav-menu-slider sidenav-right" id="<%= PortalUtil.getPortletNamespace(AnalyticsReportsPortletKeys.ANALYTICS_REPORTS) %>analyticsReportsPanelId">
		<div class="sidebar sidebar-light sidebar-sm sidenav-menu">
			<div class="lfr-analytics-reports-sidebar" id="analyticsReportsSidebar">
				<div class="d-flex justify-content-between p-3 sidebar-header">
					<h1 class="sr-only"><liferay-ui:message key="content-performance-panel" /></h1>

					<span class="font-weight-bold"><liferay-ui:message key="content-performance" /></span>

					<clay:button
						aria-label='<%= LanguageUtil.get(request, "close") %>'
						cssClass="btn btn-monospaced btn-unstyled component-action sidenav-close text-secondary"
						displayType="unstyled"
						icon="times"
						small="<%= true %>"
					/>
				</div>

				<div class="sidebar-body">
					<span aria-hidden="true" className="loading-animation loading-animation-sm" />

					<react:component
						module="js/AnalyticsReportsApp"
						props='<%= (Map<String, Object>)request.getAttribute("data") %>'
					/>
				</div>
			</div>
		</div>
	</div>
</liferay-util:body-bottom>