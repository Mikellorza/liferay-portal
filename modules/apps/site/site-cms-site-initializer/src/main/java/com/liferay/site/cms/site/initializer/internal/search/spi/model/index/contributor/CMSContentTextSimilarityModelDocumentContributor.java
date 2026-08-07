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
import com.liferay.site.cms.site.initializer.internal.constants.TextSimilarityConstants;
import com.liferay.site.cms.site.initializer.internal.search.similarity.CMSContentTextSimilarityTextExtractor;
import com.liferay.site.cms.site.initializer.internal.search.similarity.TextSimilaritySignatureUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Adds the near-duplicate band signatures of a CMS content's main text fields
 * as a keyword field per language, so the Content Governance Dashboard's Text
 * Similarity widget can group near-duplicate content through a single
 * aggregation, without a per-document similarity query at read time.
 *
 * <p>
 * Runs alongside the core {@code ObjectEntryModelDocumentContributor} on the
 * same document and only contributes for CMS content object entries.
 * </p>
 *
 * <p>
 * Registered by {@link CMSContentTextSimilarityContributorRegistrar} rather
 * than by declarative services, because the same instance has to be registered
 * once per object definition class name as well as under the object entry class
 * name.
 * </p>
 *
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

			// Every translation is signed, and each token carries the language

			// it was computed for, so that the aggregation only ever groups

			// content read in the same language. Two translations of the same

			// content share no word shingles, so mixing languages in one field

			// would never group them anyway.

			// The language cannot be a field name suffix: the platform's own

			// dynamic template claims every field ending in a language id and

			// maps it to analyzed text, which cannot back an aggregation.

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
					TextSimilarityConstants.FIELD_NAME_BANDS,
					bandSignatures.toArray(new String[0]));
			}

			if (!signatures.isEmpty()) {
				document.addKeyword(
					TextSimilarityConstants.FIELD_NAME_SIGNATURE,
					signatures.toArray(new String[0]));
			}
		}
		catch (Exception exception) {

			// Never break indexing of the object entry because of the
			// similarity signature.

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