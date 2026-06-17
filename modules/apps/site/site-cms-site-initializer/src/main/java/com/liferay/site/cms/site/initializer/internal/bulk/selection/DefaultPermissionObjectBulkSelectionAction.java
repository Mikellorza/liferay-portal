/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.bulk.selection;

import com.liferay.bulk.selection.BulkSelectionAction;
import com.liferay.object.constants.ObjectEntryFolderConstants;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectEntryFolder;
import com.liferay.object.service.ObjectEntryFolderLocalService;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.site.cms.site.initializer.bulk.selection.BaseObjectBulkSelectionAction;
import com.liferay.site.cms.site.initializer.util.CMSDefaultPermissionUtil;

import java.io.Serializable;

import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Andrea Sbarra
 */
@Component(
	property = "bulk.selection.action.key=default.permission.object",
	service = BulkSelectionAction.class
)
public class DefaultPermissionObjectBulkSelectionAction
	extends BaseObjectBulkSelectionAction {

	@Override
	protected void doExecute(
			User user, Map<String, Serializable> inputMap, Object object)
		throws Exception {

		ObjectEntry objectObjectEntry = (ObjectEntry)object;

		Map<String, Serializable> objectObjectEntryValues =
			objectObjectEntry.getValues();

		String roleKey = (String)inputMap.get("roleKey");

		if (Validator.isBlank(roleKey)) {
			objectObjectEntryValues.put(
				"defaultPermissions",
				MapUtil.getString(inputMap, "defaultPermissions"));
		}
		else {
			JSONObject existingJSONObject = _jsonFactory.createJSONObject(
				GetterUtil.getString(
					objectObjectEntryValues.get("defaultPermissions"), "{}"));

			JSONObject newJSONObject = _jsonFactory.createJSONObject(
				GetterUtil.getString(
					MapUtil.getString(inputMap, "defaultPermissions"), "{}"));

			existingJSONObject.put(
				ObjectEntryFolderConstants.EXTERNAL_REFERENCE_CODE_CONTENTS,
				_getJSONObject(
					existingJSONObject.getJSONObject(
						ObjectEntryFolderConstants.
							EXTERNAL_REFERENCE_CODE_CONTENTS),
					newJSONObject.getJSONObject(
						ObjectEntryFolderConstants.
							EXTERNAL_REFERENCE_CODE_CONTENTS),
					roleKey)
			).put(
				ObjectEntryFolderConstants.EXTERNAL_REFERENCE_CODE_FILES,
				_getJSONObject(
					existingJSONObject.getJSONObject(
						ObjectEntryFolderConstants.
							EXTERNAL_REFERENCE_CODE_FILES),
					newJSONObject.getJSONObject(
						ObjectEntryFolderConstants.
							EXTERNAL_REFERENCE_CODE_FILES),
					roleKey)
			).put(
				"OBJECT_ENTRY_FOLDERS",
				_getJSONObject(
					existingJSONObject.getJSONObject("OBJECT_ENTRY_FOLDERS"),
					newJSONObject.getJSONObject("OBJECT_ENTRY_FOLDERS"),
					roleKey)
			);

			objectObjectEntryValues.put(
				"defaultPermissions", existingJSONObject.toString());
		}

		partialUpdateObjectEntry(
			user.getUserId(), objectObjectEntry, objectObjectEntryValues);

		_propagateResourcePermissions(
			objectObjectEntry, objectObjectEntryValues);
	}

	private JSONObject _getJSONObject(
		JSONObject jsonObject1, JSONObject jsonObject2, String key) {

		if (jsonObject1 == null) {
			jsonObject1 = _jsonFactory.createJSONObject();
		}

		if ((jsonObject2 == null) || (jsonObject2.get(key) == null)) {
			return jsonObject1;
		}

		jsonObject1.put(key, jsonObject2.get(key));

		return jsonObject1;
	}

	private void _propagateResourcePermissions(
			ObjectEntry objectObjectEntry,
			Map<String, Serializable> objectObjectEntryValues)
		throws Exception {

		ObjectEntryFolder objectEntryFolder =
			_objectEntryFolderLocalService.
				fetchObjectEntryFolderByExternalReferenceCode(
					GetterUtil.getString(
						objectObjectEntryValues.get(
							"classExternalReferenceCode")),
					GetterUtil.getLong(
						objectObjectEntryValues.get("depotGroupId")),
					objectObjectEntry.getCompanyId());

		if (objectEntryFolder == null) {
			return;
		}

		JSONObject defaultPermissionsJSONObject = _jsonFactory.createJSONObject(
			GetterUtil.getString(
				objectObjectEntryValues.get("defaultPermissions"), "{}"));

		CMSDefaultPermissionUtil.setObjectEntryFolderResourcePermissions(
			objectEntryFolder, defaultPermissionsJSONObject);

		for (ObjectEntry objectEntry :
				objectEntryLocalService.getObjectEntryFolderObjectEntries(
					objectEntryFolder.getGroupId(),
					objectEntryFolder.getObjectEntryFolderId(),
					QueryUtil.ALL_POS, QueryUtil.ALL_POS)) {

			CMSDefaultPermissionUtil.setObjectEntryResourcePermissions(
				objectEntry, defaultPermissionsJSONObject);
		}
	}

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private ObjectEntryFolderLocalService _objectEntryFolderLocalService;

}