/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.search.spi.model.index.contributor;

import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.BaseModel;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.DocumentContributor;
import com.liferay.portal.kernel.util.HtmlParser;
import com.liferay.site.cms.site.initializer.constants.SimilarityConstants;
import com.liferay.site.cms.site.initializer.internal.search.similarity.CMSContentTextSimilarityTextExtractor;
import com.liferay.site.cms.site.initializer.internal.search.similarity.TextSimilaritySignatureUtil;

import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Adds the near-duplicate band signatures of a CMS content's main text fields
 * as a keyword field per language, so the Content Governance Dashboard's Text
 * Similarity widget can group near-duplicate content through a single
 * aggregation, without a per-document similarity query at read time.
 *
 * <p>
 * A document contributor carrying no <code>indexer.class.name</code> property is
 * run by every indexer on every document, which is what the per-object-definition
 * indexers an object entry is written through need. It is therefore invoked for
 * every indexed model and has to guard on the model type first.
 * </p>
 *
 * @author Mikel Lorza
 */
@Component(service = DocumentContributor.class)
public class CMSContentTextSimilarityDocumentContributor
	implements DocumentContributor<ObjectEntry> {

	@Override
	public void contribute(
		Document document, BaseModel<ObjectEntry> baseModel) {

		if (!(baseModel instanceof ObjectEntry)) {
			return;
		}

		ObjectEntry objectEntry = (ObjectEntry)baseModel;

		try {
			ObjectDefinition objectDefinition =
				objectEntry.getObjectDefinition();

			if (!objectDefinition.isCMS()) {
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

				bandSignatures.addAll(
					TransformUtil.transformToList(
						TextSimilaritySignatureUtil.getBandSignatures(text),
						bandSignature -> _getToken(languageId, bandSignature)));
				signatures.addAll(
					TransformUtil.transformToList(
						TextSimilaritySignatureUtil.getSignature(text),
						signature -> _getToken(languageId, signature)));
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

			// Never break indexing of the object entry because of the
			// similarity signature. A systematic failure here produces no
			// signatures, hence no clusters, hence a dashboard reporting no
			// duplicates, so this log is the only signal that the feature is
			// not working.

			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to contribute text similarity bands for object " +
						"entry " + objectEntry.getObjectEntryId(),
					exception);
			}
		}
	}

	@Activate
	protected void activate() {
		_cmsContentTextSimilarityTextExtractor =
			new CMSContentTextSimilarityTextExtractor(_htmlParser);
	}

	private String _getToken(String languageId, String value) {
		return StringBundler.concat(languageId, StringPool.UNDERLINE, value);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CMSContentTextSimilarityDocumentContributor.class);

	private CMSContentTextSimilarityTextExtractor
		_cmsContentTextSimilarityTextExtractor;

	@Reference
	private HtmlParser _htmlParser;

}