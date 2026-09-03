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
public class SimilarAssetHashUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testGetHashesIsDeterministic() {
		Assert.assertArrayEquals(
			SimilarAssetHashUtil.getHashes(_A),
			SimilarAssetHashUtil.getHashes(_A));
	}

	@Test
	public void testGetHashesIsDeterministicAcrossDefaultLocales() {
		Locale defaultLocale = LocaleUtil.getDefault();

		try {
			LocaleUtil.setDefault("en", "US", null);

			String[] hashes = SimilarAssetHashUtil.getHashes(_TURKISH);

			LocaleUtil.setDefault("tr", "TR", null);

			Assert.assertArrayEquals(
				hashes, SimilarAssetHashUtil.getHashes(_TURKISH));
		}
		finally {
			LocaleUtil.setDefault(
				defaultLocale.getLanguage(), defaultLocale.getCountry(),
				defaultLocale.getVariant());
		}
	}

	@Test
	public void testGetHashesIsEmptyForBlankText() {
		Assert.assertEquals(0, SimilarAssetHashUtil.getHashes(null).length);
		Assert.assertEquals(0, SimilarAssetHashUtil.getHashes("").length);
		Assert.assertEquals(0, SimilarAssetHashUtil.getHashes("   ").length);
	}

	@Test
	public void testGetHashesSharesMoreForSimilarThanForDistinctText() {
		Assert.assertTrue(
			_getSharedHashCount(_A, _A + " 2") > _getSharedHashCount(
				_A, _DISTINCT));
	}

	@Test
	public void testGetHashesSharesMostForNearDuplicateText() {
		Assert.assertTrue(_getSharedHashCount(_A, _A + " 2") >= 20);
	}

	@Test
	public void testGetHashesSharesNothingBetweenDistinctText() {
		Assert.assertEquals(0, _getSharedHashCount(_A, _DISTINCT));
	}

	private int _getSharedHashCount(String text1, String text2) {
		Set<String> hashes = new HashSet<>();

		for (String hash : SimilarAssetHashUtil.getHashes(text1)) {
			hashes.add(hash);
		}

		int count = 0;

		for (String hash : SimilarAssetHashUtil.getHashes(text2)) {
			if (hashes.contains(hash)) {
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