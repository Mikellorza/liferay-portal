/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.site.internal.dto.v1_0.util;

import com.liferay.headless.admin.site.dto.v1_0.Scope;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.GroupLocalServiceUtil;
import com.liferay.portal.kernel.util.Validator;

/**
 * @author Mikel Lorza
 */
public class GroupUtil {

	public static String getExternalReferenceCode(
			String externalReferenceCode, long scopeGroupId)
		throws PortalException {

		if (Validator.isNotNull(externalReferenceCode)) {
			return externalReferenceCode;
		}

		Group group = GroupLocalServiceUtil.getGroup(scopeGroupId);

		return group.getExternalReferenceCode();
	}

	public static Long getGroupId(Scope scope, long scopeGroupId) {
		if ((scope == null) || (scope.getExternalReferenceCode() == null)) {
			return scopeGroupId;
		}

		Long companyId = CompanyUtil.getCompanyId(scopeGroupId);

		if (companyId == null) {
			return null;
		}

		Group group = GroupLocalServiceUtil.fetchGroupByExternalReferenceCode(
			scope.getExternalReferenceCode(), companyId);

		if (group == null) {
			return null;
		}

		return group.getGroupId();
	}

}