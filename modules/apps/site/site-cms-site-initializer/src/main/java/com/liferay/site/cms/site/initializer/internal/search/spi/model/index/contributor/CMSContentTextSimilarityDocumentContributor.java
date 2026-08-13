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
import com.liferay.site.cms.site.initializer.internal.search.similarity.CMSContentTextSimilarityTextExtractor;
import com.liferay.site.cms.site.initializer.internal.search.similarity.TextSimilaritySignatureUtil;

import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
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

			List<String> bandSignatures = new ArrayList<>();
			List<String> signatures = new ArrayList<>();

			for (String languageId :
					_cmsContentTextSimilarityTextExtractor.getLanguageIds(
						objectEntry)) {

				String text = _cmsContentTextSimilarityTextExtractor.getText(
					languageId, objectEntry);

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

			// A failure here yields no tokens, hence no clusters, hence a
			// dashboard reporting no near duplicates, so this log is the only
			// signal that the feature is not working

			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to contribute text similarity tokens for object " +
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