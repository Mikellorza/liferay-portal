/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.test.rule;

import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.PortalUtil;

import org.junit.runner.Description;

/**
 * @author Mikel Lorza
 */
public class DeletableSystemGroupTestRule extends AbstractTestRule<Void, Void> {

	public static final DeletableSystemGroupTestRule INSTANCE =
		new DeletableSystemGroupTestRule();

	@Override
	protected void afterClass(Description description, Void previousValue) {
		if (_safeCloseable != null) {
			_safeCloseable.close();

			_safeCloseable = null;
		}
	}

	@Override
	protected void afterMethod(
		Description description, Void previousValue, Object target) {
	}

	@Override
	protected Void beforeClass(Description description) {
		DeletableSystemGroup deletableSystemGroup = description.getAnnotation(
			DeletableSystemGroup.class);

		if (deletableSystemGroup != null) {
			Portal portal = PortalUtil.getPortal();

			String[] originalSortedSystemGroups =
				ReflectionTestUtil.getFieldValue(portal, "_sortedSystemGroups");

			String[] newSortedSystemGroups = originalSortedSystemGroups;

			for (String groupKey : deletableSystemGroup.groupKeys()) {
				newSortedSystemGroups = ArrayUtil.remove(
					newSortedSystemGroups, groupKey);
			}

			ReflectionTestUtil.setFieldValue(
				portal, "_sortedSystemGroups", newSortedSystemGroups);

			_safeCloseable = () -> ReflectionTestUtil.setFieldValue(
				portal, "_sortedSystemGroups", originalSortedSystemGroups);
		}

		return null;
	}

	@Override
	protected Void beforeMethod(Description description, Object target) {
		return null;
	}

	private SafeCloseable _safeCloseable;

}