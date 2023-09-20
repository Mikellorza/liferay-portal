/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.locked.layouts.web.internal.scheduler;

import com.liferay.layout.configuration.LockedLayoutsGroupConfiguration;
import com.liferay.layout.locked.layouts.web.internal.configuration.LockedLayoutsCompanyConfiguration;
import com.liferay.layout.manager.LayoutLockManager;
import com.liferay.petra.function.UnsafeConsumer;
import com.liferay.petra.function.UnsafeRunnable;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.scheduler.SchedulerJobConfiguration;
import com.liferay.portal.kernel.scheduler.TimeUnit;
import com.liferay.portal.kernel.scheduler.TriggerConfiguration;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.util.GetterUtil;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Lourdes Fernández Besada
 */
@Component(
	configurationPid = "com.liferay.layout.locked.layouts.web.internal.configuration.LockedLayoutsCompanyConfiguration",
	service = SchedulerJobConfiguration.class
)
public class UnlockLayoutsSchedulerJobConfiguration
	implements SchedulerJobConfiguration {

	@Override
	public UnsafeConsumer<Long, Exception>
		getCompanyJobExecutorUnsafeConsumer() {

		return _getCompanyJobExecutorUnsafeConsumer();
	}

	@Override
	public UnsafeRunnable<Exception> getJobExecutorUnsafeRunnable() {
		return () -> _companyLocalService.forEachCompanyId(
			_getCompanyJobExecutorUnsafeConsumer());
	}

	@Override
	public TriggerConfiguration getTriggerConfiguration() {
		return TriggerConfiguration.createTriggerConfiguration(
			15, TimeUnit.MINUTE);
	}

	private UnsafeConsumer<Long, Exception>
		_getCompanyJobExecutorUnsafeConsumer() {

		return companyId -> {
			if (!FeatureFlagManagerUtil.isEnabled("LPS-180328")) {
				return;
			}

			LockedLayoutsCompanyConfiguration
				lockedLayoutsCompanyConfiguration =
					_configurationProvider.getCompanyConfiguration(
						LockedLayoutsCompanyConfiguration.class, companyId);

			if (!lockedLayoutsCompanyConfiguration.
					allowAutomaticUnlockingProcess()) {

				return;
			}

			_layoutLockManager.unlockLayouts(
				companyId, _getLockedLayoutsGroupConfigurations(companyId),
				lockedLayoutsCompanyConfiguration.timeWithoutAutosave());
		};
	}

	private String _getLockedLayoutsGroupConfigurationFilterString(
		long companyId) {

		String filterString = StringBundler.concat(
			"(&(", ConfigurationAdmin.SERVICE_FACTORYPID, StringPool.EQUAL,
			LockedLayoutsGroupConfiguration.class.getName(), ".scoped)(|");

		for (Group group :
				_groupLocalService.getGroups(
					companyId, GroupConstants.ANY_PARENT_GROUP_ID, true)) {

			filterString = filterString.concat(
				"(groupId=" + group.getGroupId() + ")");
		}

		return filterString.concat("))");
	}

	private Map<Long, LockedLayoutsGroupConfiguration>
			_getLockedLayoutsGroupConfigurations(long companyId)
		throws Exception {

		Map<Long, LockedLayoutsGroupConfiguration>
			lockedLayoutsGroupConfigurations = new HashMap<>();

		Configuration[] configurations = _configurationAdmin.listConfigurations(
			_getLockedLayoutsGroupConfigurationFilterString(companyId));

		if ((configurations == null) || (configurations.length == 0)) {
			return lockedLayoutsGroupConfigurations;
		}

		for (Configuration configuration : configurations) {
			Dictionary<String, Object> dictionary =
				configuration.getProperties();

			long groupId = GetterUtil.getLong(dictionary.get("groupId"));

			if (groupId > 0) {
				lockedLayoutsGroupConfigurations.put(
					groupId,
					_configurationProvider.getGroupConfiguration(
						LockedLayoutsGroupConfiguration.class, groupId));
			}
		}

		return lockedLayoutsGroupConfigurations;
	}

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference
	private ConfigurationAdmin _configurationAdmin;

	@Reference
	private ConfigurationProvider _configurationProvider;

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private LayoutLockManager _layoutLockManager;

}