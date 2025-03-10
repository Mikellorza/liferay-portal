/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.internal.entry.util;

import com.liferay.object.model.ObjectEntryFolder;
import com.liferay.object.service.ObjectEntryFolderLocalService;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.StringUtil;

/**
 * @author Mikel Lorza
 */
public class ObjectEntryFolderUtil {

	public static String getRootObjectEntryFolderExternalReferenceCode(
		ObjectEntryFolder objectEntryFolder,
		ObjectEntryFolderLocalService objectEntryFolderLocalService) {

		if (objectEntryFolder == null) {
			return StringPool.BLANK;
		}

		String[] treePaths = StringUtil.split(
			objectEntryFolder.getTreePath(), CharPool.FORWARD_SLASH);

		if (ArrayUtil.isEmpty(treePaths) || (treePaths.length < 3)) {
			return StringPool.BLANK;
		}

		try {
			ObjectEntryFolder rootObjectEntryFolder =
				objectEntryFolderLocalService.getObjectEntryFolder(
					Long.valueOf(treePaths[1]));

			return rootObjectEntryFolder.getExternalReferenceCode();
		}
		catch (PortalException portalException) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to get folder " + treePaths[1] +
						" while indexing document",
					portalException);
			}
		}

		return StringPool.BLANK;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ObjectEntryFolderUtil.class);

}