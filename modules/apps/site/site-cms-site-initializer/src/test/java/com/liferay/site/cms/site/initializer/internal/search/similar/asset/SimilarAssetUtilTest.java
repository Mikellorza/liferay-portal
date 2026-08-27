/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.search.similar.asset;

import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Mikel Lorza
 */
public class SimilarAssetUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testBlankTextYieldsNoSimilarAssets() {
		Assert.assertEquals(0, SimilarAssetUtil.getSimilarAssets(null).length);
		Assert.assertEquals(0, SimilarAssetUtil.getSimilarAssets("").length);
		Assert.assertEquals(0, SimilarAssetUtil.getSimilarAssets("   ").length);
	}

	@Test
	public void testDeterministic() {
		Assert.assertArrayEquals(
			SimilarAssetUtil.getSimilarAssets(_A),
			SimilarAssetUtil.getSimilarAssets(_A));
	}

	@Test
	public void testDeterministicAcrossDefaultLocales() {
		Locale defaultLocale = LocaleUtil.getDefault();

		try {
			LocaleUtil.setDefault("en", "US", null);

			String[] similarAssets = SimilarAssetUtil.getSimilarAssets(
				_TURKISH);

			LocaleUtil.setDefault("tr", "TR", null);

			Assert.assertArrayEquals(
				similarAssets, SimilarAssetUtil.getSimilarAssets(_TURKISH));
		}
		finally {
			LocaleUtil.setDefault(
				defaultLocale.getLanguage(), defaultLocale.getCountry(),
				defaultLocale.getVariant());
		}
	}

	@Test
	public void testDistinctTextSharesNoSimilarAssets() {
		Assert.assertEquals(0, _getSharedSimilarAssetCount(_A, _DISTINCT));
	}

	@Test
	public void testSimilarTextSharesMoreThanDistinct() {
		Assert.assertTrue(
			_getSharedSimilarAssetCount(_A, _A + " 2") >
				_getSharedSimilarAssetCount(_A, _DISTINCT));
	}

	@Test
	public void testSimilarTextSharesMostSimilarAssets() {
		Assert.assertTrue(_getSharedSimilarAssetCount(_A, _A + " 2") >= 20);
	}

	private int _getSharedSimilarAssetCount(String text1, String text2) {
		Set<String> similarAssets = new HashSet<>();

		for (String similarAsset : SimilarAssetUtil.getSimilarAssets(text1)) {
			similarAssets.add(similarAsset);
		}

		int count = 0;

		for (String similarAsset : SimilarAssetUtil.getSimilarAssets(text2)) {
			if (similarAssets.contains(similarAsset)) {
				count++;
			}
		}

		return count;
	}

	private static final String _A =
		"If you forgot your password, go to the login page and click the " +
			"forgot password link. Enter your email address and you will " +
				"receive an email with instructions to create a new password.";

	private static final String _DISTINCT =
		"The quarterly sales report shows strong revenue growth across the " +
			"European market. Product categories in retail and wholesale " +
				"increased during the last fiscal period.";

	private static final String _TURKISH =
		"KULLANICI ADINIZI VE \u0130NTERNET \u015E\u0130FREN\u0130Z\u0130 " +
			"G\u0130R\u0130N VE OTURUM A\u00c7MAK \u0130\u00c7\u0130N " +
				"\u0130LER\u0130 D\u00dc\u011eMES\u0130NE BASIN";

}