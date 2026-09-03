/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.search.similar.asset;

import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.asset.kernel.model.AssetTag;
import com.liferay.asset.kernel.service.AssetCategoryLocalService;
import com.liferay.asset.kernel.service.AssetTagLocalService;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.site.cms.site.initializer.constants.SimilarAssetConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Mikel Lorza
 */
public class CMSContentSimilarAssetMetadataExtractorTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_assetCategoryLocalService = Mockito.mock(
			AssetCategoryLocalService.class);
		_assetTagLocalService = Mockito.mock(AssetTagLocalService.class);

		_cmsContentMetadataSimilarityElementExtractor =
			new CMSContentSimilarAssetMetadataExtractor(
				_assetCategoryLocalService, _assetTagLocalService);
	}

	@Test
	public void testGetElementsWithCategoriesAndTags() throws Exception {
		_setCategoryIds(11, 12);
		_setTagIds(21);

		Assert.assertEquals(
			new HashSet<>(Arrays.asList("c11", "c12", "t21")),
			_cmsContentMetadataSimilarityElementExtractor.getElements(
				_mockObjectEntry(),
				SimilarAssetConstants.TOKEN_LANGUAGE_ID_ALL));
	}

	@Test
	public void testGetElementsWithoutCategoriesOrTags() throws Exception {
		_setCategoryIds();
		_setTagIds();

		Assert.assertEquals(
			Collections.emptySet(),
			_cmsContentMetadataSimilarityElementExtractor.getElements(
				_mockObjectEntry(),
				SimilarAssetConstants.TOKEN_LANGUAGE_ID_ALL));
	}

	@Test
	public void testGetElementsWithSameCategoryIdAndTagId() throws Exception {

		// Category ids and tag ids are separate sequences, so without a prefix
		// a content in category 42 and a content tagged 42 would look
		// classified alike

		_setCategoryIds(42);
		_setTagIds(42);

		Assert.assertEquals(
			new HashSet<>(Arrays.asList("c42", "t42")),
			_cmsContentMetadataSimilarityElementExtractor.getElements(
				_mockObjectEntry(),
				SimilarAssetConstants.TOKEN_LANGUAGE_ID_ALL));
	}

	@Test
	public void testGetTokenLanguageIdsIgnoresTheContentLanguages()
		throws Exception {

		Assert.assertEquals(
			Collections.singleton(SimilarAssetConstants.TOKEN_LANGUAGE_ID_ALL),
			_cmsContentMetadataSimilarityElementExtractor.getTokenLanguageIds(
				_mockObjectEntry()));
	}

	private ObjectEntry _mockObjectEntry() throws Exception {
		ObjectDefinition objectDefinition = Mockito.mock(
			ObjectDefinition.class);

		Mockito.when(
			objectDefinition.getClassName()
		).thenReturn(
			_CLASS_NAME
		);

		ObjectEntry objectEntry = Mockito.mock(ObjectEntry.class);

		Mockito.when(
			objectEntry.getDefaultLanguageId()
		).thenReturn(
			"en_US"
		);

		Mockito.when(
			objectEntry.getObjectDefinition()
		).thenReturn(
			objectDefinition
		);

		Mockito.when(
			objectEntry.getObjectEntryId()
		).thenReturn(
			_OBJECT_ENTRY_ID
		);

		return objectEntry;
	}

	private void _setCategoryIds(long... categoryIds) {
		List<AssetCategory> assetCategories = new ArrayList<>();

		for (long categoryId : categoryIds) {
			AssetCategory assetCategory = Mockito.mock(AssetCategory.class);

			Mockito.when(
				assetCategory.getCategoryId()
			).thenReturn(
				categoryId
			);

			assetCategories.add(assetCategory);
		}

		Mockito.when(
			_assetCategoryLocalService.getCategories(
				_CLASS_NAME, _OBJECT_ENTRY_ID)
		).thenReturn(
			assetCategories
		);
	}

	private void _setTagIds(long... tagIds) {
		List<AssetTag> assetTags = new ArrayList<>();

		for (long tagId : tagIds) {
			AssetTag assetTag = Mockito.mock(AssetTag.class);

			Mockito.when(
				assetTag.getTagId()
			).thenReturn(
				tagId
			);

			assetTags.add(assetTag);
		}

		Mockito.when(
			_assetTagLocalService.getTags(_CLASS_NAME, _OBJECT_ENTRY_ID)
		).thenReturn(
			assetTags
		);
	}

	private static final String _CLASS_NAME =
		"com.liferay.object.model.ObjectEntry#CMSBasicWebContent";

	private static final long _OBJECT_ENTRY_ID = 1234;

	private AssetCategoryLocalService _assetCategoryLocalService;
	private AssetTagLocalService _assetTagLocalService;
	private CMSContentSimilarAssetMetadataExtractor
		_cmsContentMetadataSimilarityElementExtractor;

}