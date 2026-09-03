/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.search.spi.model.index.contributor;

import com.liferay.asset.kernel.service.AssetCategoryLocalService;
import com.liferay.asset.kernel.service.AssetTagLocalService;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.BaseModel;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.DocumentContributor;
import com.liferay.portal.kernel.util.HtmlParser;
import com.liferay.site.cms.site.initializer.internal.search.similar.asset.CMSContentSimilarAssetExtractor;
import com.liferay.site.cms.site.initializer.internal.search.similar.asset.CMSContentSimilarAssetMetadataExtractor;
import com.liferay.site.cms.site.initializer.internal.search.similar.asset.CMSContentSimilarAssetTextExtractor;
import com.liferay.site.cms.site.initializer.internal.search.similar.asset.CMSContentSimilarAssetTitleExtractor;
import com.liferay.site.cms.site.initializer.internal.search.similar.asset.SimilarAssetUtil;

import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Mikel Lorza
 */
@Component(service = DocumentContributor.class)
public class CMSContentSimilarAssetDocumentContributor
	implements DocumentContributor<ObjectEntry> {

	@Override
	public void contribute(
		Document document, BaseModel<ObjectEntry> baseModel) {

		if (!(baseModel instanceof ObjectEntry)) {
			return;
		}

		ObjectEntry objectEntry = (ObjectEntry)baseModel;

		try {
			if (!FeatureFlagManagerUtil.isEnabled(
					objectEntry.getCompanyId(), "LPD-82226")) {

				return;
			}

			ObjectDefinition objectDefinition =
				objectEntry.getObjectDefinition();

			if (!objectDefinition.isCMS()) {
				return;
			}

			for (CMSContentSimilarAssetExtractor
					cmsContentSimilarAssetExtractor :
						_cmsContentSimilarAssetExtractors) {

				_contribute(
					cmsContentSimilarAssetExtractor, document, objectEntry);
			}
		}
		catch (Exception exception) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to contribute similar assets for object entry " +
						objectEntry.getObjectEntryId(),
					exception);
			}
		}
	}

	@Activate
	protected void activate() {
		_cmsContentSimilarAssetExtractors.add(
			new CMSContentSimilarAssetMetadataExtractor(
				_assetCategoryLocalService, _assetTagLocalService));
		_cmsContentSimilarAssetExtractors.add(
			new CMSContentSimilarAssetTextExtractor(_htmlParser));
		_cmsContentSimilarAssetExtractors.add(
			new CMSContentSimilarAssetTitleExtractor());
	}

	private void _contribute(
			CMSContentSimilarAssetExtractor cmsContentSimilarAssetExtractor,
			Document document, ObjectEntry objectEntry)
		throws Exception {

		List<String> similarAssets = new ArrayList<>();

		for (String tokenLanguageId :
				cmsContentSimilarAssetExtractor.getTokenLanguageIds(
					objectEntry)) {

			similarAssets.addAll(
				TransformUtil.transformToList(
					SimilarAssetUtil.getSimilarAssets(
						cmsContentSimilarAssetExtractor.getElements(
							objectEntry, tokenLanguageId)),
					similarAsset -> StringBundler.concat(
						tokenLanguageId, StringPool.UNDERLINE, similarAsset)));
		}

		if (similarAssets.isEmpty()) {
			return;
		}

		document.addKeyword(
			cmsContentSimilarAssetExtractor.getFieldName(),
			similarAssets.toArray(new String[0]));
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CMSContentSimilarAssetDocumentContributor.class);

	@Reference
	private AssetCategoryLocalService _assetCategoryLocalService;

	@Reference
	private AssetTagLocalService _assetTagLocalService;

	private final List<CMSContentSimilarAssetExtractor>
		_cmsContentSimilarAssetExtractors = new ArrayList<>();

	@Reference
	private HtmlParser _htmlParser;

}