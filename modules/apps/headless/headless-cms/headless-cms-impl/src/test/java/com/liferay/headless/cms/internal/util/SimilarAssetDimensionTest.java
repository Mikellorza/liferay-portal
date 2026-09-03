/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.cms.internal.util;

import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.site.cms.site.initializer.constants.SimilarAssetConstants;

import jakarta.ws.rs.BadRequestException;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Mikel Lorza
 */
public class SimilarAssetDimensionTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testGetDefaultsToText() {
		Assert.assertEquals(
			SimilarAssetDimension.TEXT, SimilarAssetDimension.get(null));
	}

	@Test
	public void testGetFieldNameIsOnePerDimension() {
		Assert.assertEquals(
			SimilarAssetConstants.FIELD_NAME_METADATA,
			SimilarAssetDimension.METADATA.getFieldName());
		Assert.assertEquals(
			SimilarAssetConstants.FIELD_NAME_TEXT,
			SimilarAssetDimension.TEXT.getFieldName());
		Assert.assertEquals(
			SimilarAssetConstants.FIELD_NAME_TITLE,
			SimilarAssetDimension.TITLE.getFieldName());
	}

	@Test
	public void testGetIsCaseSensitive() {
		try {
			SimilarAssetDimension.get("text");

			Assert.fail();
		}
		catch (BadRequestException badRequestException) {
			Assert.assertTrue(
				badRequestException.getMessage(),
				badRequestException.getMessage(
				).contains(
					"[METADATA, TEXT, TITLE]"
				));
		}
	}

	@Test
	public void testGetMinSharedSimilarAssetsIsHigherForMetadata() {
		Assert.assertEquals(
			12, SimilarAssetDimension.METADATA.getMinSharedSimilarAssets());
		Assert.assertEquals(
			3, SimilarAssetDimension.TEXT.getMinSharedSimilarAssets());
		Assert.assertEquals(
			3, SimilarAssetDimension.TITLE.getMinSharedSimilarAssets());
	}

	@Test
	public void testGetRejectsAnUnknownDimension() {
		try {
			SimilarAssetDimension.get("AUTHOR");

			Assert.fail();
		}
		catch (BadRequestException badRequestException) {
			Assert.assertTrue(
				badRequestException.getMessage(),
				badRequestException.getMessage(
				).contains(
					"AUTHOR"
				));
		}
	}

	@Test
	public void testGetTokenLanguageIdIsUnlocalizedForMetadata() {
		Assert.assertEquals(
			SimilarAssetConstants.TOKEN_LANGUAGE_ID_ALL,
			SimilarAssetDimension.METADATA.getTokenLanguageId("en_US"));
		Assert.assertEquals(
			"en_US", SimilarAssetDimension.TEXT.getTokenLanguageId("en_US"));
		Assert.assertEquals(
			"en_US", SimilarAssetDimension.TITLE.getTokenLanguageId("en_US"));
	}

}