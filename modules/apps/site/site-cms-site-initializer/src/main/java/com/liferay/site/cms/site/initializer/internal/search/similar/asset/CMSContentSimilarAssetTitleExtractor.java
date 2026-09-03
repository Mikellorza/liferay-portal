/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.search.similar.asset;

import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectField;
import com.liferay.object.model.bag.ObjectFieldBag;
import com.liferay.site.cms.site.initializer.constants.SimilarAssetConstants;

import java.io.Serializable;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Compares CMS content by its title, reduced to character n grams.
 *
 * <p>
 * Word shingles are what the text dimension uses and they do not work here: a
 * three word shingle over a four word title yields a single shingle, so two
 * titles either match exactly or share nothing, which is what the exact title
 * aggregation already reports. Character n grams degrade smoothly instead, so a
 * typo costs a few elements rather than all of them.
 * </p>
 *
 * @author Mikel Lorza
 */
public class CMSContentSimilarAssetTitleExtractor
	implements CMSContentSimilarAssetExtractor {

	/**
	 * Returns an empty set when the content has no title in that language, so
	 * it stays out of clustering.
	 */
	@Override
	public Set<String> getElements(
			ObjectEntry objectEntry, String tokenLanguageId)
		throws Exception {

		return _getNGrams(_getTitle(objectEntry, tokenLanguageId));
	}

	@Override
	public String getFieldName() {
		return SimilarAssetConstants.FIELD_NAME_TITLE;
	}

	@Override
	public Set<String> getTokenLanguageIds(ObjectEntry objectEntry)
		throws Exception {

		Set<String> languageIds = new LinkedHashSet<>();

		languageIds.add(objectEntry.getDefaultLanguageId());

		ObjectField objectField = _getTitleObjectField(objectEntry);

		if ((objectField == null) || !objectField.isLocalized()) {
			return languageIds;
		}

		Map<String, Serializable> indexedValues =
			objectEntry.getIndexedValues();

		Object localizedValues = indexedValues.get(
			objectField.getI18nObjectFieldName());

		if (localizedValues instanceof Map) {
			Map<?, ?> localizedValuesMap = (Map<?, ?>)localizedValues;

			for (Object languageId : localizedValuesMap.keySet()) {
				languageIds.add(String.valueOf(languageId));
			}
		}

		return languageIds;
	}

	private Set<String> _getNGrams(String title) {
		Set<String> ngrams = new HashSet<>();

		if (title == null) {
			return ngrams;
		}

		String normalizedTitle = title.toLowerCase(
		).replaceAll(
			"[^\\p{L}\\p{Nd}]+", " "
		).trim();

		if (normalizedTitle.isEmpty()) {
			return ngrams;
		}

		if (normalizedTitle.length() <= _NGRAM_SIZE) {
			ngrams.add(normalizedTitle);

			return ngrams;
		}

		for (int i = 0; i <= (normalizedTitle.length() - _NGRAM_SIZE); i++) {
			ngrams.add(normalizedTitle.substring(i, i + _NGRAM_SIZE));
		}

		return ngrams;
	}

	private String _getTitle(ObjectEntry objectEntry, String languageId)
		throws Exception {

		ObjectField objectField = _getTitleObjectField(objectEntry);

		if (objectField == null) {
			return null;
		}

		Map<String, Serializable> indexedValues =
			objectEntry.getIndexedValues();

		if (!objectField.isLocalized()) {
			Object indexedValue = indexedValues.get(objectField.getName());

			if (indexedValue == null) {
				return null;
			}

			return String.valueOf(indexedValue);
		}

		Object localizedValues = indexedValues.get(
			objectField.getI18nObjectFieldName());

		if (!(localizedValues instanceof Map)) {
			return null;
		}

		Map<?, ?> localizedValuesMap = (Map<?, ?>)localizedValues;

		Object indexedValue = localizedValuesMap.get(languageId);

		if (indexedValue == null) {
			return null;
		}

		return String.valueOf(indexedValue);
	}

	private ObjectField _getTitleObjectField(ObjectEntry objectEntry)
		throws Exception {

		ObjectDefinition objectDefinition = objectEntry.getObjectDefinition();

		long titleObjectFieldId = objectDefinition.getTitleObjectFieldId();

		if (titleObjectFieldId == 0) {
			return null;
		}

		ObjectFieldBag objectFieldBag = objectDefinition.getObjectFieldBag();

		return objectFieldBag.getObjectField(titleObjectFieldId);
	}

	// Measured against 3 and 5 over the same pairs: 3 groups more unrelated
	// titles, 5 loses the two typo case, and 4 separates them best

	private static final int _NGRAM_SIZE = 4;

}