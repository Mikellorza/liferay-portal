/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.search.similar.asset;

import com.liferay.object.constants.ObjectFieldConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectField;
import com.liferay.object.model.bag.ObjectFieldBag;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.HtmlParser;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.site.cms.site.initializer.constants.SimilarAssetConstants;

import java.io.Serializable;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Compares CMS content by the prose of its indexed text fields, cut into
 * sequences of three consecutive keywords. The title is left out on purpose,
 * since it is a dimension of its own.
 *
 * @author Mikel Lorza
 */
public class CMSContentSimilarAssetTextExtractor
	implements CMSContentSimilarAssetExtractor {

	public CMSContentSimilarAssetTextExtractor(HtmlParser htmlParser) {
		_htmlParser = htmlParser;
	}

	@Override
	public Set<String> getElements(
			ObjectEntry objectEntry, String tokenLanguageId)
		throws Exception {

		return _getKeywordSequences(getText(tokenLanguageId, objectEntry));
	}

	@Override
	public String getFieldName() {
		return SimilarAssetConstants.FIELD_NAME_TEXT;
	}

	public String getText(String languageId, ObjectEntry objectEntry)
		throws Exception {

		Map<String, Serializable> indexedValues =
			objectEntry.getIndexedValues();

		ObjectDefinition objectDefinition = objectEntry.getObjectDefinition();

		StringBundler sb = new StringBundler();

		for (ObjectField objectField : _getObjectFields(objectDefinition)) {
			Object indexedValue = null;

			if (objectField.isLocalized()) {
				Object localizedValues = indexedValues.get(
					objectField.getI18nObjectFieldName());

				if (localizedValues instanceof Map) {
					Map<?, ?> localizedValuesMap = (Map<?, ?>)localizedValues;

					indexedValue = localizedValuesMap.get(languageId);
				}
			}
			else {
				indexedValue = indexedValues.get(objectField.getName());
			}

			if (indexedValue == null) {
				continue;
			}

			String indexedValueString = String.valueOf(indexedValue);

			if (Objects.equals(
					objectField.getBusinessType(),
					ObjectFieldConstants.BUSINESS_TYPE_RICH_TEXT)) {

				indexedValueString = _htmlParser.extractText(
					indexedValueString);
			}

			sb.append(indexedValueString);
			sb.append(CharPool.SPACE);
		}

		return sb.toString();
	}

	@Override
	public Set<String> getTokenLanguageIds(ObjectEntry objectEntry)
		throws Exception {

		Set<String> languageIds = new LinkedHashSet<>();

		languageIds.add(objectEntry.getDefaultLanguageId());

		Map<String, Serializable> indexedValues =
			objectEntry.getIndexedValues();

		ObjectDefinition objectDefinition = objectEntry.getObjectDefinition();

		for (ObjectField objectField : _getObjectFields(objectDefinition)) {
			if (!objectField.isLocalized()) {
				continue;
			}

			Object localizedValues = indexedValues.get(
				objectField.getI18nObjectFieldName());

			if (!(localizedValues instanceof Map)) {
				continue;
			}

			Map<?, ?> localizedValuesMap = (Map<?, ?>)localizedValues;

			for (Object languageId : localizedValuesMap.keySet()) {
				languageIds.add(String.valueOf(languageId));
			}
		}

		return languageIds;
	}

	private Set<String> _getKeywordSequences(String text) {
		Set<String> keywordSequences = new HashSet<>();

		if (text == null) {
			return keywordSequences;
		}

		String[] words = StringUtil.toLowerCase(
			text, LocaleUtil.ENGLISH
		).replaceAll(
			"[^\\p{L}\\p{Nd}]+", " "
		).trim(
		).split(
			"\\s+"
		);

		if ((words.length == 1) && words[0].isEmpty()) {
			return keywordSequences;
		}

		if (words.length < _KEYWORD_SEQUENCE_SIZE) {
			for (String word : words) {
				keywordSequences.add(word);
			}

			return keywordSequences;
		}

		for (int i = 0; i <= (words.length - _KEYWORD_SEQUENCE_SIZE); i++) {
			StringBundler sb = new StringBundler();

			for (int j = 0; j < _KEYWORD_SEQUENCE_SIZE; j++) {
				if (j > 0) {
					sb.append(StringPool.SPACE);
				}

				sb.append(words[i + j]);
			}

			keywordSequences.add(sb.toString());
		}

		return keywordSequences;
	}

	private Iterable<ObjectField> _getObjectFields(
		ObjectDefinition objectDefinition) {

		ObjectFieldBag objectFieldBag = objectDefinition.getObjectFieldBag();

		Set<ObjectField> objectFields = new LinkedHashSet<>();

		for (ObjectField objectField :
				objectFieldBag.getIndexedObjectFields()) {

			if (_isTitleObjectField(objectDefinition, objectField)) {
				continue;
			}

			if (_isTextObjectField(objectField)) {
				objectFields.add(objectField);
			}
		}

		return objectFields;
	}

	private boolean _isTextObjectField(ObjectField objectField) {
		String businessType = objectField.getBusinessType();

		if (businessType.equals(ObjectFieldConstants.BUSINESS_TYPE_TEXT) ||
			businessType.equals(ObjectFieldConstants.BUSINESS_TYPE_LONG_TEXT) ||
			businessType.equals(ObjectFieldConstants.BUSINESS_TYPE_RICH_TEXT)) {

			return true;
		}

		return false;
	}

	private boolean _isTitleObjectField(
		ObjectDefinition objectDefinition, ObjectField objectField) {

		if (objectField.getObjectFieldId() ==
				objectDefinition.getTitleObjectFieldId()) {

			return true;
		}

		return false;
	}

	private static final int _KEYWORD_SEQUENCE_SIZE = 3;

	private final HtmlParser _htmlParser;

}