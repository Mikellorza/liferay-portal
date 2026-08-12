/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.search.similarity;

import com.liferay.object.constants.ObjectFieldConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectField;
import com.liferay.object.model.bag.ObjectFieldBag;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.util.HtmlParser;

import java.io.Serializable;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Decides which text of a CMS content the similarity signature is computed
 * from, and in which languages. An object definition exposes several indexed
 * fields, only some of them hold prose, and one of them is the title, which
 * carries its own dimension.
 *
 * @author Mikel Lorza
 */
public class CMSContentTextSimilarityTextExtractor {

	public CMSContentTextSimilarityTextExtractor(HtmlParser htmlParser) {
		_htmlParser = htmlParser;
	}

	/**
	 * Returns the default language plus every language the signature fields are
	 * translated into, so each translation is signed on its own.
	 */
	public Set<String> getLanguageIds(ObjectEntry objectEntry)
		throws Exception {

		Set<String> languageIds = new LinkedHashSet<>();

		languageIds.add(objectEntry.getDefaultLanguageId());

		Map<String, Serializable> indexedValues =
			objectEntry.getIndexedValues();

		ObjectDefinition objectDefinition = objectEntry.getObjectDefinition();

		for (ObjectField objectField :
				_getSignatureObjectFields(objectDefinition)) {

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

	/**
	 * Returns blank when there is no text in that language, so the content
	 * yields no signature and stays out of clustering.
	 */
	public String getText(ObjectEntry objectEntry, String languageId)
		throws Exception {

		Map<String, Serializable> indexedValues =
			objectEntry.getIndexedValues();

		ObjectDefinition objectDefinition = objectEntry.getObjectDefinition();

		StringBundler sb = new StringBundler();

		for (ObjectField objectField :
				_getSignatureObjectFields(objectDefinition)) {

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

				// A field that is not translated reads the same in every
				// language, so it belongs to every language's text

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

	private Iterable<ObjectField> _getSignatureObjectFields(
		ObjectDefinition objectDefinition) {

		ObjectFieldBag objectFieldBag = objectDefinition.getObjectFieldBag();

		Set<ObjectField> objectFields = new LinkedHashSet<>();

		for (ObjectField objectField :
				objectFieldBag.getIndexedObjectFields()) {

			// The title carries its own dimension, and needs character n grams
			// rather than word shingles, which over a few words degenerate into
			// exact matching. Folding it in here would also let a content
			// with a short or empty body group on its title alone

			if (objectField.getObjectFieldId() ==
					objectDefinition.getTitleObjectFieldId()) {

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

	private final HtmlParser _htmlParser;

}