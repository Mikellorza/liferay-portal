/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.search.spi.model.index.contributor;

import com.liferay.object.constants.ObjectFieldConstants;
import com.liferay.object.constants.ObjectFolderConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectField;
import com.liferay.object.model.ObjectFolder;
import com.liferay.object.model.bag.ObjectFieldBag;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.util.HtmlParserUtil;
import com.liferay.portal.search.spi.model.index.contributor.ModelDocumentContributor;
import com.liferay.site.cms.site.initializer.internal.search.similarity.TextSimilaritySignatureUtil;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.osgi.service.component.annotations.Component;

/**
 * @author Mikel Lorza
 */
@Component(
	property = "indexer.class.name=com.liferay.object.model.ObjectEntry",
	service = ModelDocumentContributor.class
)
public class CMSContentTextSimilarityModelDocumentContributor
	implements ModelDocumentContributor<ObjectEntry> {

	@Override
	public void contribute(Document document, ObjectEntry objectEntry) {
		try {
			if (!_isCMSContent(objectEntry)) {
				return;
			}

			List<String> bandSignatures = new ArrayList<>();
			List<String> signatures = new ArrayList<>();

			for (String languageId : _getLanguageIds(objectEntry)) {
				String text = _getText(objectEntry, languageId);

				for (String bandSignature :
						TextSimilaritySignatureUtil.getBandSignatures(text)) {

					bandSignatures.add(_getToken(languageId, bandSignature));
				}

				for (String signature :
						TextSimilaritySignatureUtil.getSignature(text)) {

					signatures.add(_getToken(languageId, signature));
				}
			}

			if (!bandSignatures.isEmpty()) {
				document.addKeyword(
					"textSimilarityBands",
					bandSignatures.toArray(new String[0]));
			}

			if (!signatures.isEmpty()) {
				document.addKeyword(
					"textSimilaritySignature",
					signatures.toArray(new String[0]));
			}
		}
		catch (Exception exception) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to contribute text similarity bands for object " +
						"entry " + objectEntry.getObjectEntryId(),
					exception);
			}
		}
	}

	private Set<String> _getLanguageIds(ObjectEntry objectEntry)
		throws Exception {

		Set<String> languageIds = new LinkedHashSet<>();

		languageIds.add(objectEntry.getDefaultLanguageId());

		Map<String, Serializable> indexedValues =
			objectEntry.getIndexedValues();

		ObjectDefinition objectDefinition = objectEntry.getObjectDefinition();

		ObjectFieldBag objectFieldBag = objectDefinition.getObjectFieldBag();

		for (ObjectField objectField :
				objectFieldBag.getIndexedObjectFields()) {

			if (!objectField.isLocalized() ||
				!_isSignatureObjectField(objectDefinition, objectField)) {

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

	private String _getText(ObjectEntry objectEntry, String languageId)
		throws Exception {

		ObjectDefinition objectDefinition = objectEntry.getObjectDefinition();

		Map<String, Serializable> indexedValues =
			objectEntry.getIndexedValues();

		ObjectFieldBag objectFieldBag = objectDefinition.getObjectFieldBag();

		StringBundler sb = new StringBundler();

		for (ObjectField objectField :
				objectFieldBag.getIndexedObjectFields()) {

			if (!_isSignatureObjectField(objectDefinition, objectField)) {
				continue;
			}

			Object value = null;

			if (objectField.isLocalized()) {
				Object localizedValues = indexedValues.get(
					objectField.getI18nObjectFieldName());

				if (localizedValues instanceof Map) {
					Map<?, ?> localizedValuesMap = (Map<?, ?>)localizedValues;

					value = localizedValuesMap.get(languageId);
				}
			}
			else {
				value = indexedValues.get(objectField.getName());
			}

			if (value == null) {
				continue;
			}

			String valueString = String.valueOf(value);

			if (Objects.equals(
					objectField.getBusinessType(),
					ObjectFieldConstants.BUSINESS_TYPE_RICH_TEXT)) {

				valueString = HtmlParserUtil.extractText(valueString);
			}

			sb.append(valueString);
			sb.append(CharPool.SPACE);
		}

		return sb.toString();
	}

	private String _getToken(String languageId, String value) {
		return StringBundler.concat(languageId, StringPool.UNDERLINE, value);
	}

	private boolean _isCMSContent(ObjectEntry objectEntry) throws Exception {
		ObjectDefinition objectDefinition = objectEntry.getObjectDefinition();

		ObjectFolder objectFolder = objectDefinition.getObjectFolder();

		if (objectFolder == null) {
			return false;
		}

		String externalReferenceCode = objectFolder.getExternalReferenceCode();

		if (Objects.equals(
				externalReferenceCode,
				ObjectFolderConstants.
					EXTERNAL_REFERENCE_CODE_CONTENT_STRUCTURES) ||
			Objects.equals(
				externalReferenceCode,
				ObjectFolderConstants.EXTERNAL_REFERENCE_CODE_FILE_TYPES)) {

			return true;
		}

		return false;
	}

	private boolean _isSignatureObjectField(
		ObjectDefinition objectDefinition, ObjectField objectField) {
		if (objectField.getObjectFieldId() ==
				objectDefinition.getTitleObjectFieldId()) {

			return false;
		}

		return _isTextObjectField(objectField);
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

	private static final Log _log = LogFactoryUtil.getLog(
		CMSContentTextSimilarityModelDocumentContributor.class);

}