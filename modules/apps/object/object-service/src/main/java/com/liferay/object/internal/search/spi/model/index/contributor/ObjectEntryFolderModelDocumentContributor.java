/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.internal.search.spi.model.index.contributor;

import com.liferay.object.model.ObjectEntryFolder;
import com.liferay.object.service.ObjectEntryFolderLocalService;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.spi.model.index.contributor.ModelDocumentContributor;

import java.util.Arrays;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Marco Leo
 */
@Component(
	property = "indexer.class.name=com.liferay.object.model.ObjectEntryFolder",
	service = ModelDocumentContributor.class
)
public class ObjectEntryFolderModelDocumentContributor
	implements ModelDocumentContributor<ObjectEntryFolder> {

	@Override
	public void contribute(
		Document document, ObjectEntryFolder objectEntryFolder) {

		document.addKeyword(
			Field.FOLDER_ID, objectEntryFolder.getObjectEntryFolderId());
		document.addText(Field.NAME, objectEntryFolder.getName());
		document.addLocalizedKeyword(
			"localized_label", objectEntryFolder.getLabelMap(), true, true);
		document.addText(
			"rootObjectEntryFolderExternalReferenceCode",
			_getRootObjectEntryFolderExternalReferenceCode(objectEntryFolder));
	}

	private String _getRootObjectEntryFolderExternalReferenceCode(
		ObjectEntryFolder objectEntryFolder) {

		List<String> treePaths = Arrays.asList(
			StringUtil.split(
				objectEntryFolder.getTreePath(), CharPool.FORWARD_SLASH));

		if (ListUtil.isEmpty(treePaths) || (treePaths.size() < 3)) {
			return StringPool.BLANK;
		}

		try {
			ObjectEntryFolder rootObjectEntryFolder =
				_objectEntryFolderLocalService.getObjectEntryFolder(
					Long.valueOf(treePaths.get(1)));

			return rootObjectEntryFolder.getExternalReferenceCode();
		}
		catch (PortalException portalException) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to get folder " + treePaths.get(0) +
						" while indexing document",
					portalException);
			}
		}

		return StringPool.BLANK;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ObjectEntryFolderModelDocumentContributor.class);

	@Reference
	private ObjectEntryFolderLocalService _objectEntryFolderLocalService;

}