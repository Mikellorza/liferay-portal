/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.page.template.internal.upgrade.v5_4_0;

import com.liferay.layout.page.template.model.LayoutPageTemplateEntry;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.ResourceLocalService;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Mikel Lorza
 */
public class ResourcePermissionUpgradeProcess extends UpgradeProcess {

	public ResourcePermissionUpgradeProcess(
		CompanyLocalService companyLocalService,
		ResourceLocalService resourceLocalService) {

		_companyLocalService = companyLocalService;
		_resourceLocalService = resourceLocalService;
	}

	@Override
	protected void doUpgrade() throws Exception {
		_companyLocalService.forEachCompany(
			company -> {
				try (PreparedStatement preparedStatement =
						connection.prepareStatement(
							StringBundler.concat(
								"select layoutPageTemplateEntryId, userId ",
								"from LayoutPageTemplateEntry where groupId =",
								"? and layoutPrototypeId > 0"))) {

					preparedStatement.setLong(1, company.getGroupId());

					try (ResultSet resultSet =
							preparedStatement.executeQuery()) {

						while (resultSet.next()) {
							_resourceLocalService.addResources(
								company.getCompanyId(), company.getGroupId(),
								resultSet.getLong("userId"),
								LayoutPageTemplateEntry.class.getName(),
								resultSet.getLong("layoutPageTemplateEntryId"),
								false, true, true);
						}
					}
				}
				catch (Exception exception) {
					if (_log.isDebugEnabled()) {
						_log.debug(exception);
					}
				}
			});
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ResourcePermissionUpgradeProcess.class);

	private final CompanyLocalService _companyLocalService;
	private final ResourceLocalService _resourceLocalService;

}