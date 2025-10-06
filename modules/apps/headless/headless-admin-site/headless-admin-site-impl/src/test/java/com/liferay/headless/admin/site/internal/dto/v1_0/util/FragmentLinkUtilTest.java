/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.site.internal.dto.v1_0.util;

import com.liferay.headless.admin.site.dto.v1_0.FragmentLink;
import com.liferay.headless.admin.site.dto.v1_0.FragmentLinkInlineValue;
import com.liferay.headless.admin.site.dto.v1_0.FragmentLinkValue;
import com.liferay.info.item.InfoItemServiceRegistry;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.service.GroupLocalServiceUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * @author Mikel Lorza
 */
public class FragmentLinkUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@AfterClass
	public static void tearDownClass() {
		_localizedValueUtilMockedStatic.close();
	}

	private static final MockedStatic<LocalizedValueUtil> _localizedValueUtilMockedStatic =
		Mockito.mockStatic(LocalizedValueUtil.class);


	@Test
	public void testToFragmentLink() {
		InfoItemServiceRegistry infoItemServiceRegistry = Mockito.mock(
			InfoItemServiceRegistry.class);

		long scopeGroupId = RandomTestUtil.randomLong();

		_localizedValueUtilMockedStatic.when(
			() -> LocalizedValueUtil.toLocalizedValues(Mockito.any(JSONObject.class))
		).thenReturn(
			HashMapBuilder.put(
				LocaleUtil.SPAIN.toString(),
				"www.liferay.es"
			).put(
				LocaleUtil.US.toString(), "www.liferay.com"
			).build()
		);

		Assert.assertEquals(
			_getFragmentLinkInlineValue(),
			FragmentLinkUtil.toFragmentLink(
				infoItemServiceRegistry,
				JSONUtil.put(
					"href",
					JSONUtil.put(
						LocaleUtil.SPAIN.toString(), "www.liferay.es"
					).put(
						LocaleUtil.US.toString(), "www.liferay.com"
					)
				).put(
					"target", "_blank"
				),
				scopeGroupId));
	}

	private FragmentLink _getFragmentLinkInlineValue() {

		return new FragmentLink() {
			{
				setTarget(Target.BLANK);
				setValue(
					() -> new FragmentLinkInlineValue() {
						{
							setValue_i18n(
								HashMapBuilder.put(
									LocaleUtil.SPAIN.toString(),
									"www.liferay.es"
								).put(
									LocaleUtil.US.toString(), "www.liferay.com"
								).build());
						}
					});
			}
		};
	}

}