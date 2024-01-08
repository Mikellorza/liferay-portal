<%--
/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%@ include file="/init.jsp" %>

<p class="mt-4 sheet-subtitle text-secondary">
	<liferay-ui:message key="url-separator" />
</p>

<clay:alert
	cssClass="mb-4"
	displayType="info"
	message="friendly-url-separator-info-message"
/>

if (response)
<span>error</span>

<%
FriendlyURLSeparatorCompanyConfigurationDisplayContext friendlyURLSeparatorCompanyConfigurationDisplayContext = (FriendlyURLSeparatorCompanyConfigurationDisplayContext)request.getAttribute(FriendlyURLSeparatorCompanyConfigurationDisplayContext.class.getName());

for (FriendlyURLSeparatorCompanyConfigurationDisplayContext.FriendlyURLSeparator friendlyURLSeparator : friendlyURLSeparatorCompanyConfigurationDisplayContext.getConfigurableFriendlyURLSeparators()) {
%>

<div class="form-group">
	<label class="mb-0" for="<%= friendlyURLSeparator.getName() %>">
		<%= friendlyURLSeparator.getLabel() %>
	</label>
	<p class="mb-1 text-secondary">
		<%= themeDisplay.getPortalURL() %>
	</p>
	<div class="input-group">
		<div class="input-group-item input-group-item-shrink input-group-prepend">
			<div class="input-group-text">
				/
			</div>
		</div>
		<div class="input-group-item input-group-append">
			<input id="<%= friendlyURLSeparator.getName() %>" name="<%= friendlyURLSeparator.getName() %>" class="form-control" value="<%= friendlyURLSeparator.getValue() %>" />
		</div>
	</div>
</div>

<%
}
%>