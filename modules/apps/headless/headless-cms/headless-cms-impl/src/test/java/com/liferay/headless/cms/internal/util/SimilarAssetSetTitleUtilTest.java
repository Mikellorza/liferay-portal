/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.cms.internal.util;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Mikel Lorza
 */
public class SimilarAssetSetTitleUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testGetTitleIgnoresBlankTitles() {
		Assert.assertEquals(
			"Summer Sale",
			SimilarAssetSetTitleUtil.getTitle(
				Arrays.asList(
					"Summer Sale Highlights", "Big Summer Sale",
					StringPool.BLANK, null)));
	}

	@Test
	public void testGetTitleIgnoresWordOrder() {
		Assert.assertEquals(
			"Summer Sale",
			SimilarAssetSetTitleUtil.getTitle(
				Arrays.asList("Summer Sale", "Sale Summer")));
	}

	@Test
	public void testGetTitleReturnsBlankWhenEveryTitleIsBlank() {
		Assert.assertEquals(
			StringPool.BLANK,
			SimilarAssetSetTitleUtil.getTitle(
				Arrays.asList(StringPool.BLANK, null)));
	}

	@Test
	public void testGetTitleReturnsCommonWordsWhenThereIsNoCommonPhrase() {
		Assert.assertEquals(
			"Product Launch",
			SimilarAssetSetTitleUtil.getTitle(
				Arrays.asList(
					"Product Highlights Launch Overview",
					"Product Launch Press Release",
					"New Product Launch Press Release")));
	}

	@Test
	public void testGetTitleReturnsFirstTitleWhenNothingIsShared() {
		Assert.assertEquals(
			"Style Guide",
			SimilarAssetSetTitleUtil.getTitle(
				Arrays.asList("Style Guide", "Guía de Redacción")));
	}

	@Test
	public void testGetTitleReturnsLongestCommonPhrase() {
		Assert.assertEquals(
			"Summer Sale",
			SimilarAssetSetTitleUtil.getTitle(
				Arrays.asList(
					"Summer Sale Highlights", "Big Summer Sale",
					"Summer Sale 2026")));
	}

	@Test
	public void testGetTitleShortensLongTitle() {
		String longTitle =
			"Quarterly Financial Performance And Market Outlook Report For " +
				"Every Region";

		String title = SimilarAssetSetTitleUtil.getTitle(
			Arrays.asList(longTitle, longTitle + " Draft"));

		Assert.assertTrue(title.length() <= 60);
		Assert.assertTrue(title.endsWith("..."));
		Assert.assertTrue(longTitle.startsWith(title.substring(0, 20)));
	}

	@Test
	public void testGetTitleSkipsABlankFirstTitle() {
		Assert.assertEquals(
			"Fallback Title",
			SimilarAssetSetTitleUtil.getTitle(
				Arrays.asList(null, "Fallback Title")));
	}

	@Test
	public void testGetTitleTrimsStopWords() {
		Assert.assertEquals(
			"Report",
			SimilarAssetSetTitleUtil.getTitle(
				ListUtil.fromArray(
					"The Report of Sales", "The Report of Costs")));
	}

}