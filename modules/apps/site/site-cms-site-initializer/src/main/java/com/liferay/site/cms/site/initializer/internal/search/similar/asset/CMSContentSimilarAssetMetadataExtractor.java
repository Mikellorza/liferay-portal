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
import com.liferay.site.cms.site.initializer.constants.SimilarAssetConstants;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Compares CMS content by the categories and tags it is classified under,
 * which are already a set and are therefore signed as they are, with no
 * shingling in between.
 *
 * <p>
 * Metadata does not depend on the language: a category is the same category in
 * every translation, so the elements are signed once under
 * {@link SimilarityConstants#TOKEN_LANGUAGE_ID_ALL} and two contents classified
 * alike are near duplicates even when they exist in different languages.
 * </p>
 *
 * @author Mikel Lorza
 */
public class CMSContentSimilarAssetMetadataExtractor
	implements CMSContentSimilarAssetExtractor {

	public CMSContentSimilarAssetMetadataExtractor(
		AssetCategoryLocalService assetCategoryLocalService,
		AssetTagLocalService assetTagLocalService) {

		_assetCategoryLocalService = assetCategoryLocalService;
		_assetTagLocalService = assetTagLocalService;
	}

	/**
	 * Returns an empty set for content classified under nothing, which is what
	 * keeps unclassified content from grouping with all the other content that
	 * is also classified under nothing.
	 */
	@Override
	public Set<String> getElements(
			ObjectEntry objectEntry, String tokenLanguageId)
		throws Exception {

		Set<String> elements = new LinkedHashSet<>();

		ObjectDefinition objectDefinition = objectEntry.getObjectDefinition();

		String className = objectDefinition.getClassName();

		long objectEntryId = objectEntry.getObjectEntryId();

		// The taxonomy is read from the services rather than from the fields
		// the asset category and asset tag contributors write, because nothing
		// guarantees they run before this one

		for (AssetCategory assetCategory :
				_assetCategoryLocalService.getCategories(
					className, objectEntryId)) {

			// A category ID and a tag ID are both sequences starting at one, so
			// without a prefix a content in category 42 and a content tagged 42
			// would look classified alike

			elements.add("c" + assetCategory.getCategoryId());
		}

		for (AssetTag assetTag :
				_assetTagLocalService.getTags(className, objectEntryId)) {

			elements.add("t" + assetTag.getTagId());
		}

		return elements;
	}

	@Override
	public String getFieldName() {
		return SimilarAssetConstants.FIELD_NAME_METADATA;
	}

	@Override
	public Set<String> getTokenLanguageIds(ObjectEntry objectEntry) {
		return Collections.singleton(
			SimilarAssetConstants.TOKEN_LANGUAGE_ID_ALL);
	}

	private final AssetCategoryLocalService _assetCategoryLocalService;
	private final AssetTagLocalService _assetTagLocalService;

}