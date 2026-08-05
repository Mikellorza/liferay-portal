/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.cms.internal.similarity;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Mikel Lorza
 */
public class SimilarityClusterTitleUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testGetTitleIgnoresBlankTitles() {
		Assert.assertEquals(
			"Summer Sale",
			SimilarityClusterTitleUtil.getTitle(
				Arrays.asList(
					"Summer Sale Highlights", "Big Summer Sale",
					StringPool.BLANK, null),
				"Summer Sale Highlights"));
	}

	@Test
	public void testGetTitleIgnoresWordOrder() {
		Assert.assertEquals(
			"Summer Sale",
			SimilarityClusterTitleUtil.getTitle(
				Arrays.asList("Summer Sale", "Sale Summer"), "Summer Sale"));
	}

	@Test
	public void testGetTitleReturnsBlankWhenEveryTitleIsBlank() {
		Assert.assertEquals(
			StringPool.BLANK,
			SimilarityClusterTitleUtil.getTitle(
				Arrays.asList(StringPool.BLANK, null), null));
	}

	@Test
	public void testGetTitleReturnsCommonWordsWhenThereIsNoCommonPhrase() {
		Assert.assertEquals(
			"Product Launch",
			SimilarityClusterTitleUtil.getTitle(
				Arrays.asList(
					"Product Highlights Launch Overview",
					"Product Launch Press Release",
					"New Product Launch Press Release"),
				"Product Highlights Launch Overview"));
	}

	@Test
	public void testGetTitleReturnsLongestCommonPhrase() {
		Assert.assertEquals(
			"Summer Sale",
			SimilarityClusterTitleUtil.getTitle(
				Arrays.asList(
					"Summer Sale Highlights", "Big Summer Sale",
					"Summer Sale 2026"),
				"Summer Sale Highlights"));
	}

	@Test
	public void testGetTitleReturnsRepresentativeTitleWhenNothingIsShared() {
		Assert.assertEquals(
			"Style Guide",
			SimilarityClusterTitleUtil.getTitle(
				Arrays.asList("Style Guide", "Guía de Redacción"),
				"Style Guide"));
	}

	@Test
	public void testGetTitleReturnsRepresentativeTitleWhenTopTitleIsBlank() {
		Assert.assertEquals(
			"Fallback Title",
			SimilarityClusterTitleUtil.getTitle(
				Arrays.asList(null, "Fallback Title"), null));
	}

	@Test
	public void testGetTitleShortensLongTitle() {
		String longTitle =
			"Quarterly Financial Performance And Market Outlook Report For " +
				"Every Region";

		String title = SimilarityClusterTitleUtil.getTitle(
			Arrays.asList(longTitle, longTitle + " Draft"), longTitle);

		Assert.assertTrue(title, title.length() <= 60);
		Assert.assertTrue(title, title.endsWith("..."));
		Assert.assertTrue(title, longTitle.startsWith(title.substring(0, 20)));
	}

	@Test
	public void testGetTitleTrimsStopWords() {
		List<String> titles = ListUtil.fromArray(
			"The Report of Sales", "The Report of Costs");

		Assert.assertEquals(
			"Report",
			SimilarityClusterTitleUtil.getTitle(titles, "The Report of Sales"));
	}

}