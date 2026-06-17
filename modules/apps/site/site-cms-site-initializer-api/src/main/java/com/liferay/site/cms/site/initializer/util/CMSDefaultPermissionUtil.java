/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.util;

import com.liferay.depot.constants.DepotRolesConstants;
import com.liferay.depot.model.DepotEntry;
import com.liferay.object.constants.ObjectEntryFolderConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectEntryFolder;
import com.liferay.object.rest.filter.factory.FilterFactory;
import com.liferay.object.service.ObjectDefinitionLocalServiceUtil;
import com.liferay.object.service.ObjectEntryFolderLocalServiceUtil;
import com.liferay.object.service.ObjectEntryLocalServiceUtil;
import com.liferay.petra.sql.dsl.expression.Predicate;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.ResourceActionsUtil;
import com.liferay.portal.kernel.service.GroupLocalServiceUtil;
import com.liferay.portal.kernel.service.ResourcePermissionLocalServiceUtil;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.StringUtil;

import java.io.Serializable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Stefano Motta
 * @author Mikel Lorza
 */
public class CMSDefaultPermissionUtil {

	public static ObjectEntry addOrUpdateObjectEntry(
			String externalReferenceCode, long companyId, long userId,
			String classExternalReferenceCode, String className,
			JSONObject defaultPermissionsJSONObject, long depotGroupId,
			String treePath)
		throws PortalException {

		ObjectDefinition objectDefinition =
			ObjectDefinitionLocalServiceUtil.
				getObjectDefinitionByExternalReferenceCode(
					"L_CMS_DEFAULT_PERMISSION", companyId);

		return ObjectEntryLocalServiceUtil.addOrUpdateObjectEntry(
			externalReferenceCode, 0, userId,
			objectDefinition.getObjectDefinitionId(),
			ObjectEntryFolderConstants.PARENT_OBJECT_ENTRY_FOLDER_ID_DEFAULT,
			HashMapBuilder.<String, Serializable>put(
				"classExternalReferenceCode", classExternalReferenceCode
			).put(
				"className", className
			).put(
				"defaultPermissions", defaultPermissionsJSONObject.toString()
			).put(
				"depotGroupId", depotGroupId
			).put(
				"treePath", treePath
			).build(),
			new ServiceContext());
	}

	public static ObjectEntry fetchObjectEntry(
			long companyId, long userId, String classExternalReferenceCode,
			String className, FilterFactory<Predicate> filterFactory)
		throws PortalException {

		ObjectDefinition objectDefinition =
			ObjectDefinitionLocalServiceUtil.
				getObjectDefinitionByExternalReferenceCode(
					"L_CMS_DEFAULT_PERMISSION", companyId);

		Predicate predicate = filterFactory.create(
			StringBundler.concat(
				"(classExternalReferenceCode eq '", classExternalReferenceCode,
				"') and (className eq '", className, "')"),
			objectDefinition);

		List<Long> primaryKeys = ObjectEntryLocalServiceUtil.getPrimaryKeys(
			new Long[0], companyId, userId,
			objectDefinition.getObjectDefinitionId(), predicate, false, null,
			QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);

		if (ListUtil.isEmpty(primaryKeys)) {
			return null;
		}

		return ObjectEntryLocalServiceUtil.fetchObjectEntry(primaryKeys.get(0));
	}

	public static JSONObject getDefaultPermissionsJSONObject(
			ObjectEntryFolder objectEntryFolder,
			FilterFactory<Predicate> filterFactory)
		throws PortalException {

		ObjectDefinition cmsDefaultPermissionObjectDefinition =
			ObjectDefinitionLocalServiceUtil.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_CMS_DEFAULT_PERMISSION",
					objectEntryFolder.getCompanyId());

		if (cmsDefaultPermissionObjectDefinition == null) {
			return null;
		}

		if (objectEntryFolder.getParentObjectEntryFolderId() != 0) {
			ObjectEntryFolder parentObjectEntryFolder =
				ObjectEntryFolderLocalServiceUtil.getObjectEntryFolder(
					objectEntryFolder.getParentObjectEntryFolderId());

			JSONObject jsonObject = getJSONObject(
				parentObjectEntryFolder.getCompanyId(),
				parentObjectEntryFolder.getUserId(),
				parentObjectEntryFolder.getExternalReferenceCode(),
				parentObjectEntryFolder.getModelClassName(), filterFactory);

			if ((jsonObject != null) && !JSONUtil.isEmpty(jsonObject)) {
				return jsonObject;
			}
		}

		Group group = GroupLocalServiceUtil.getGroup(
			objectEntryFolder.getGroupId());

		return getJSONObject(
			group.getCompanyId(), group.getCreatorUserId(),
			group.getExternalReferenceCode(), DepotEntry.class.getName(),
			filterFactory);
	}

	public static JSONObject getJSONObject(
			long companyId, long userId, String classExternalReferenceCode,
			String className, FilterFactory<Predicate> filterFactory)
		throws PortalException {

		if (classExternalReferenceCode.equals(
				ObjectEntryFolderConstants.EXTERNAL_REFERENCE_CODE_CONTENTS) ||
			classExternalReferenceCode.equals(
				ObjectEntryFolderConstants.EXTERNAL_REFERENCE_CODE_FILES)) {

			return JSONFactoryUtil.createJSONObject();
		}

		ObjectEntry objectEntry = fetchObjectEntry(
			companyId, userId, classExternalReferenceCode, className,
			filterFactory);

		if (objectEntry == null) {
			return JSONFactoryUtil.createJSONObject();
		}

		Map<String, Serializable> values = objectEntry.getValues();

		return JSONFactoryUtil.createJSONObject(
			String.valueOf(values.getOrDefault("defaultPermissions", "{}")));
	}

	public static void setObjectEntryFolderResourcePermissions(
			ObjectEntryFolder objectEntryFolder,
			JSONObject defaultPermissionsJSONObject)
		throws PortalException {

		if ((defaultPermissionsJSONObject == null) ||
			JSONUtil.isEmpty(defaultPermissionsJSONObject)) {

			return;
		}

		JSONObject objectEntryFoldersJSONObject =
			defaultPermissionsJSONObject.getJSONObject("OBJECT_ENTRY_FOLDERS");

		if (objectEntryFoldersJSONObject == null) {
			return;
		}

		List<String> resourceActions = ResourceActionsUtil.getResourceActions(
			ObjectEntryFolder.class.getName());

		for (Role role : _getRoles(objectEntryFolder.getCompanyId())) {
			String[] actionIds = JSONUtil.toStringArray(
				objectEntryFoldersJSONObject.getJSONArray(role.getName()));

			if (objectEntryFolder.getParentObjectEntryFolderId() ==
					ObjectEntryFolderConstants.
						PARENT_OBJECT_ENTRY_FOLDER_ID_DEFAULT) {

				actionIds = ArrayUtil.remove(actionIds, ActionKeys.DELETE);
			}

			ResourcePermissionLocalServiceUtil.setResourcePermissions(
				objectEntryFolder.getCompanyId(),
				ObjectEntryFolder.class.getName(),
				ResourceConstants.SCOPE_INDIVIDUAL,
				String.valueOf(objectEntryFolder.getObjectEntryFolderId()),
				role.getRoleId(),
				ArrayUtil.filter(actionIds, resourceActions::contains));
		}
	}

	public static void setObjectEntryResourcePermissions(
			ObjectEntry objectEntry, FilterFactory<Predicate> filterFactory)
		throws PortalException {

		setObjectEntryResourcePermissions(
			objectEntry,
			_getDefaultPermissionsJSONObject(objectEntry, filterFactory));
	}

	public static void setObjectEntryResourcePermissions(
			ObjectEntry objectEntry, JSONObject defaultPermissionsJSONObject)
		throws PortalException {

		if ((defaultPermissionsJSONObject == null) ||
			JSONUtil.isEmpty(defaultPermissionsJSONObject)) {

			return;
		}

		ObjectEntryFolder rootObjectEntryFolder = _getRootObjectEntryFolder(
			objectEntry);

		if (rootObjectEntryFolder == null) {
			return;
		}

		JSONObject objectEntryJSONObject =
			defaultPermissionsJSONObject.getJSONObject(
				rootObjectEntryFolder.getExternalReferenceCode());

		if (objectEntryJSONObject == null) {
			return;
		}

		List<String> resourceActions = ResourceActionsUtil.getResourceActions(
			objectEntry.getModelClassName());

		for (Role role : _getRoles(objectEntry.getCompanyId())) {
			JSONArray jsonArray = objectEntryJSONObject.getJSONArray(
				role.getName());

			if ((jsonArray == null) || JSONUtil.isEmpty(jsonArray)) {
				jsonArray = JSONFactoryUtil.createJSONArray();
			}

			ResourcePermissionLocalServiceUtil.setResourcePermissions(
				objectEntry.getCompanyId(), objectEntry.getModelClassName(),
				ResourceConstants.SCOPE_INDIVIDUAL,
				String.valueOf(objectEntry.getObjectEntryId()),
				role.getRoleId(),
				ArrayUtil.filter(
					JSONUtil.toStringArray(jsonArray),
					resourceActions::contains));
		}
	}

	private static JSONObject _getDefaultPermissionsJSONObject(
			ObjectEntry objectEntry, FilterFactory<Predicate> filterFactory)
		throws PortalException {

		ObjectDefinition cmsDefaultPermissionObjectDefinition =
			ObjectDefinitionLocalServiceUtil.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_CMS_DEFAULT_PERMISSION", objectEntry.getCompanyId());

		if (cmsDefaultPermissionObjectDefinition == null) {
			return null;
		}

		if (objectEntry.getObjectEntryFolderId() != 0) {
			ObjectEntryFolder parentObjectEntryFolder =
				ObjectEntryFolderLocalServiceUtil.getObjectEntryFolder(
					objectEntry.getObjectEntryFolderId());

			JSONObject jsonObject = getJSONObject(
				parentObjectEntryFolder.getCompanyId(),
				parentObjectEntryFolder.getUserId(),
				parentObjectEntryFolder.getExternalReferenceCode(),
				parentObjectEntryFolder.getModelClassName(), filterFactory);

			if ((jsonObject != null) && !JSONUtil.isEmpty(jsonObject)) {
				return jsonObject;
			}
		}

		Group group = GroupLocalServiceUtil.getGroup(objectEntry.getGroupId());

		return getJSONObject(
			group.getCompanyId(), group.getCreatorUserId(),
			group.getExternalReferenceCode(), DepotEntry.class.getName(),
			filterFactory);
	}

	private static List<Role> _getRoles(long companyId) {
		return RoleLocalServiceUtil.getGroupRolesAndTeamRoles(
			companyId, null,
			Arrays.asList(
				RoleConstants.ADMINISTRATOR,
				DepotRolesConstants.ASSET_LIBRARY_OWNER),
			null, null,
			new int[] {RoleConstants.TYPE_REGULAR, RoleConstants.TYPE_DEPOT},
			null, 0, 0, QueryUtil.ALL_POS, QueryUtil.ALL_POS);
	}

	private static ObjectEntryFolder _getRootObjectEntryFolder(
		ObjectEntry objectEntry) {

		ObjectEntryFolder objectEntryFolder =
			ObjectEntryFolderLocalServiceUtil.fetchObjectEntryFolder(
				objectEntry.getObjectEntryFolderId());

		if (objectEntryFolder == null) {
			return null;
		}

		if (Objects.equals(
				objectEntryFolder.getExternalReferenceCode(),
				ObjectEntryFolderConstants.EXTERNAL_REFERENCE_CODE_CONTENTS) ||
			Objects.equals(
				objectEntryFolder.getExternalReferenceCode(),
				ObjectEntryFolderConstants.EXTERNAL_REFERENCE_CODE_FILES)) {

			return objectEntryFolder;
		}

		String[] parts = StringUtil.split(
			objectEntryFolder.getTreePath(), CharPool.SLASH);

		if (parts.length <= 2) {
			return null;
		}

		return ObjectEntryFolderLocalServiceUtil.fetchObjectEntryFolder(
			GetterUtil.getLong(parts[1]));
	}

}