/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.display.context;

import com.liferay.frontend.data.set.model.FDSActionDropdownItem;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.CreationMenu;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItem;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntryFolder;
import com.liferay.object.model.ObjectFolder;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectFolderLocalService;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.URLCodec;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.site.cms.site.initializer.internal.configuration.CMSSiteInitializerConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Marco Galluzzi
 */
public abstract class BaseSectionDisplayContext {

	public BaseSectionDisplayContext(
		CMSSiteInitializerConfiguration cmsSiteInitializerConfiguration,
		HttpServletRequest httpServletRequest,
		ObjectDefinitionLocalService objectDefinitionLocalService,
		ObjectFolderLocalService objectFolderLocalService) {

		this.cmsSiteInitializerConfiguration = cmsSiteInitializerConfiguration;
		this.httpServletRequest = httpServletRequest;
		this.objectDefinitionLocalService = objectDefinitionLocalService;
		this.objectFolderLocalService = objectFolderLocalService;

		themeDisplay = (ThemeDisplay)httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
	}

	public String getAPIURL() {
		String[] objectFolderExternalReferenceCodes =
			getObjectFolderExternalReferenceCodes();

		String[] entryClassNames = null;

		if (ArrayUtil.isEmpty(objectFolderExternalReferenceCodes)) {
			entryClassNames = getEntryClassNames();
		}
		else {
			entryClassNames = _getEntryClassNames(
				objectFolderExternalReferenceCodes);
		}

		if (ArrayUtil.isEmpty(entryClassNames)) {
			return "/o/search/v1.0/search?emptySearch=true" +
				"&nestedFields=embedded";
		}

		StringBundler sb = new StringBundler(3);

		sb.append("/o/search/v1.0/search?emptySearch=true&entryClassNames=");
		sb.append(
			URLCodec.encodeURL(
				ArrayUtil.toString(entryClassNames, StringPool.BLANK)));
		sb.append("&nestedFields=embedded");

		return sb.toString();
	}

	public List<DropdownItem> getBulkActionDropdownItems() {
		return new ArrayList<>();
	}

	public CreationMenu getCreationMenu() {
		return new CreationMenu();
	}

	public Map<String, Object> getEmptyState() {
		return Collections.emptyMap();
	}

	public String[] getEntryClassNames() {
		return new String[0];
	}

	public List<FDSActionDropdownItem> getFDSActionDropdownItems() {
		return new ArrayList<>();
	}

	public String[] getObjectFolderExternalReferenceCodes() {
		return new String[0];
	}

	protected final CMSSiteInitializerConfiguration
		cmsSiteInitializerConfiguration;
	protected final HttpServletRequest httpServletRequest;
	protected final ObjectDefinitionLocalService objectDefinitionLocalService;
	protected final ObjectFolderLocalService objectFolderLocalService;
	protected final ThemeDisplay themeDisplay;

	private List<String> _getEntryClassNames(
		String objectFolderExternalReferenceCode) {

		ObjectFolder objectFolder =
			objectFolderLocalService.fetchObjectFolderByExternalReferenceCode(
				objectFolderExternalReferenceCode, themeDisplay.getCompanyId());

		if (objectFolder == null) {
			return Collections.emptyList();
		}

		List<ObjectDefinition> objectDefinitions =
			objectDefinitionLocalService.getObjectFolderObjectDefinitions(
				objectFolder.getObjectFolderId());

		return TransformUtil.transform(
			objectDefinitions, ObjectDefinition::getClassName);
	}

	private String[] _getEntryClassNames(
		String[] objectFolderExternalReferenceCodes) {

		List<String> entryClassNames = new ArrayList<>();

		entryClassNames.add(ObjectEntryFolder.class.getName());

		for (String objectFolderExternalReferenceCode :
				objectFolderExternalReferenceCodes) {

			entryClassNames.addAll(
				_getEntryClassNames(objectFolderExternalReferenceCode));
		}

		return ArrayUtil.toStringArray(entryClassNames);
	}

}