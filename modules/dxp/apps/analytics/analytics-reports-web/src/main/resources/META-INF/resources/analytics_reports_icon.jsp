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
String portletNamespace = PortalUtil.getPortletNamespace(AnalyticsReportsPortletKeys.ANALYTICS_REPORTS);

String activePanel = "";

if ((Boolean)request.getAttribute("isPanelStateOpen")) {
	activePanel = "active";
}
%>

<li class="control-menu-nav-item">
	<clay:button
		aria-label='<%= LanguageUtil.get(request, "content-performance") %>'
		cssClass='<%= activePanel + "lfr-portal-tooltip product-menu-toggle sidenav-toggler" %>'
		data-content="body"
		data-open-class="lfr-has-analytics-reports-panel open-admin-panel"
		data-target='<%= "#" + portletNamespace + "analyticsReportsPanelId" %>'
		data-title='<%= LanguageUtil.get(request, "content-performance") %>'
		data-toggle="liferay-sidenav"
		data-type="fixed-push"
		data-type-mobile="fixed"
		displayType="unstyled"
		icon="analytics"
		id='<%= portletNamespace + "analyticsReportsPanelToggleId" %>'
		monospaced="<%= true %>"
		small="<%= true %>"
	/>
</li>

<script>
	Liferay.SideNavigation.initialize(
		document.getElementById('${portletNamespace}analyticsReportsPanelToggleId')
	);
</script>