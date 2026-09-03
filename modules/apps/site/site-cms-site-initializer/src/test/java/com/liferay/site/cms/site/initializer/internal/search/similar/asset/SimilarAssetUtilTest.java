/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.search.similar.asset;

import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
	public void testGetSimilarAssetsIgnoresElementOrder() {
		Set<String> elements = _getElements(0, 200);

		Set<String> reversedElements = new LinkedHashSet<>();

		for (int i = 199; i >= 0; i--) {
			reversedElements.add("e" + i);
		}

		Assert.assertArrayEquals(
			SimilarAssetUtil.getSimilarAssets(elements),
			SimilarAssetUtil.getSimilarAssets(reversedElements));
	}

	@Test
	public void testGetSimilarAssetsIsDeterministic() {
		Set<String> elements = _getElements(0, 200);

		Assert.assertArrayEquals(
			SimilarAssetUtil.getSimilarAssets(elements),
			SimilarAssetUtil.getSimilarAssets(elements));
	}

	@Test
	public void testGetSimilarAssetsIsEmptyWithoutElements() {
		Assert.assertEquals(0, SimilarAssetUtil.getSimilarAssets(null).length);
		Assert.assertEquals(
			0,
			SimilarAssetUtil.getSimilarAssets(Collections.emptySet()).length);
	}

	@Test
	public void testGetSimilarAssetsSharesMoreTheMoreTheSetsOverlap() {
		int wideOverlapCount = _getSharedSimilarAssetCount(
			_getElements(0, 200), _getElements(20, 220));
		int narrowOverlapCount = _getSharedSimilarAssetCount(
			_getElements(0, 200), _getElements(100, 300));

		Assert.assertTrue(
			wideOverlapCount + " vs " + narrowOverlapCount,
			wideOverlapCount > narrowOverlapCount);
	}

	@Test
	public void testGetSimilarAssetsSharesNothingBetweenDistinctSets() {
		Assert.assertEquals(
			0,
			_getSharedSimilarAssetCount(
				_getElements(0, 200), _getElements(1000, 1200)));
	}

	@Test
	public void testGetSimilarAssetsYieldsOneTokenPerBand() {
		Assert.assertEquals(
			32,
			SimilarAssetUtil.getSimilarAssets(
				Collections.singleton(RandomTestUtil.randomString())).length);
	}

	private Set<String> _getElements(int start, int end) {
		Set<String> elements = new LinkedHashSet<>();

		for (int i = start; i < end; i++) {
			elements.add("e" + i);
		}

		return elements;
	}

	private int _getSharedSimilarAssetCount(
		Set<String> elements1, Set<String> elements2) {

		Set<String> similarAssets = new HashSet<>();

		Collections.addAll(
			similarAssets, SimilarAssetUtil.getSimilarAssets(elements1));

		int count = 0;

		for (String similarAsset :
				SimilarAssetUtil.getSimilarAssets(elements2)) {

			if (similarAssets.contains(similarAsset)) {
				count++;
			}
		}

		return count;
	}

}