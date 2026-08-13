/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.cms.internal.similarity;

import com.liferay.portal.test.rule.LiferayUnitTestRule;

import jakarta.ws.rs.BadRequestException;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Mikel Lorza
 */
public class SimilarityDimensionTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testGetIsTextWhenDimensionIsNull() {
		Assert.assertEquals(
			SimilarityDimension.TEXT, SimilarityDimension.get(null));
	}

	@Test
	public void testGetIsTextWhenDimensionIsText() {
		Assert.assertEquals(
			SimilarityDimension.TEXT, SimilarityDimension.get("TEXT"));
	}

	@Test(expected = BadRequestException.class)
	public void testGetWhenDimensionIsLowerCase() {
		SimilarityDimension.get("text");
	}

	@Test(expected = BadRequestException.class)
	public void testGetWhenDimensionIsUnknown() {
		SimilarityDimension.get("TITLE");
	}

}