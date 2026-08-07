/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.search.spi.model.index.contributor;

import com.liferay.object.constants.ObjectFolderConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectFolder;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.search.spi.model.index.contributor.ModelDocumentContributor;
import com.liferay.site.cms.site.initializer.internal.constants.SimilarityConstants;
import com.liferay.site.cms.site.initializer.internal.search.similarity.CMSContentTextSimilarityTextExtractor;
import com.liferay.site.cms.site.initializer.internal.search.similarity.TextSimilaritySignatureUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Mikel Lorza
 */
public class CMSContentTextSimilarityModelDocumentContributor
	implements ModelDocumentContributor<ObjectEntry> {

	public CMSContentTextSimilarityModelDocumentContributor(
		CMSContentTextSimilarityTextExtractor
			cmsContentTextSimilarityTextExtractor) {

		_cmsContentTextSimilarityTextExtractor =
			cmsContentTextSimilarityTextExtractor;
	}

	@Override
	public void contribute(Document document, ObjectEntry objectEntry) {
		try {
			if (!_isCMSContent(objectEntry)) {
				return;
			}

			List<String> bandSignatures = new ArrayList<>();
			List<String> signatures = new ArrayList<>();

			for (String languageId :
					_cmsContentTextSimilarityTextExtractor.getLanguageIds(
						objectEntry)) {

				String text = _cmsContentTextSimilarityTextExtractor.getText(
					objectEntry, languageId);

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
					SimilarityConstants.FIELD_NAME_TEXT_BANDS,
					bandSignatures.toArray(new String[0]));
			}

			if (!signatures.isEmpty()) {
				document.addKeyword(
					SimilarityConstants.FIELD_NAME_TEXT_SIGNATURE,
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

	private static final Log _log = LogFactoryUtil.getLog(
		CMSContentTextSimilarityModelDocumentContributor.class);

	private final CMSContentTextSimilarityTextExtractor
		_cmsContentTextSimilarityTextExtractor;

}