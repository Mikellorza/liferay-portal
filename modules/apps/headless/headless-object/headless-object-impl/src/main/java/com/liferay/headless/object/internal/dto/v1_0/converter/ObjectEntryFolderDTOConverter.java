/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.object.internal.dto.v1_0.converter;

import com.liferay.headless.delivery.dto.v1_0.util.CreatorUtil;
import com.liferay.headless.object.dto.v1_0.ObjectEntryFolder;
import com.liferay.headless.object.dto.v1_0.ParentObjectEntryFolderBrief;
import com.liferay.object.service.ObjectEntryFolderLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.PermissionService;
import com.liferay.portal.kernel.service.ResourceActionLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.vulcan.dto.converter.DTOConverter;
import com.liferay.portal.vulcan.dto.converter.DTOConverterContext;
import com.liferay.portal.vulcan.fields.NestedFieldsSupplier;
import com.liferay.portal.vulcan.permission.Permission;
import com.liferay.portal.vulcan.permission.PermissionUtil;
import com.liferay.portal.vulcan.util.LocalizedMapUtil;

import java.util.Collection;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Alicia García
 */
@Component(
	property = "dto.class.name=com.liferay.object.model.ObjectEntryFolder",
	service = DTOConverter.class
)
public class ObjectEntryFolderDTOConverter
	implements DTOConverter
		<com.liferay.object.model.ObjectEntryFolder, ObjectEntryFolder> {

	@Override
	public String getContentType() {
		return ObjectEntryFolder.class.getSimpleName();
	}

	@Override
	public ObjectEntryFolder toDTO(DTOConverterContext dtoConverterContext)
		throws Exception {

		com.liferay.object.model.ObjectEntryFolder objectEntryFolder =
			_objectEntryFolderLocalService.getObjectEntryFolder(
				(Long)dtoConverterContext.getId());

		com.liferay.object.model.ObjectEntryFolder parentObjectEntryFolder =
			_getParentObjectEntryFolder(objectEntryFolder);

		return new ObjectEntryFolder() {
			{
				setActions(dtoConverterContext::getActions);
				setCreator(
					() -> CreatorUtil.toCreator(
						dtoConverterContext, _portal,
						_userLocalService.fetchUser(
							objectEntryFolder.getUserId())));
				setDateCreated(objectEntryFolder::getCreateDate);
				setDateModified(objectEntryFolder::getModifiedDate);
				setDescription(objectEntryFolder::getDescription);
				setExternalReferenceCode(
					objectEntryFolder::getExternalReferenceCode);
				setId(objectEntryFolder::getObjectEntryFolderId);
				setLabel(
					() -> objectEntryFolder.getLabel(
						dtoConverterContext.getLocale()));
				setLabel_i18n(
					() -> LocalizedMapUtil.getLanguageIdMap(
						objectEntryFolder.getLabelMap()));
				setNumberOfObjectEntries(
					() -> NestedFieldsSupplier.supply(
						"numberOfObjectEntries",
						nestedField ->
							_objectEntryLocalService.
								getObjectEntryFolderObjectEntriesCount(
									objectEntryFolder.getGroupId(),
									objectEntryFolder.
										getObjectEntryFolderId())));
				setNumberOfObjectEntryFolders(
					() -> NestedFieldsSupplier.supply(
						"numberOfObjectEntryFolders",
						nestedField ->
							_objectEntryFolderLocalService.
								getObjectEntryFoldersCount(
									objectEntryFolder.getGroupId(),
									objectEntryFolder.getCompanyId(),
									objectEntryFolder.
										getObjectEntryFolderId())));
				setParentObjectEntryFolderBrief(
					() -> NestedFieldsSupplier.supply(
						"parentObjectEntryFolderBrief",
						nestedField -> _getParentObjectEntryFolderBrief(
							dtoConverterContext, parentObjectEntryFolder)));
				setParentObjectEntryFolderExternalReferenceCode(
					() -> {
						if (parentObjectEntryFolder != null) {
							return parentObjectEntryFolder.
								getExternalReferenceCode();
						}

						return null;
					});
				setParentObjectEntryFolderId(
					() -> {
						if (parentObjectEntryFolder != null) {
							return parentObjectEntryFolder.
								getObjectEntryFolderId();
						}

						return null;
					});
				setPermissions(() -> _toPermissions(objectEntryFolder));
				setScopeKey(
					() -> {
						Group group = _groupLocalService.fetchGroup(
							objectEntryFolder.getGroupId());

						if (group == null) {
							return String.valueOf(
								objectEntryFolder.getGroupId());
						}

						return group.getGroupKey();
					});
				setTitle(objectEntryFolder::getName);
			}
		};
	}

	private com.liferay.object.model.ObjectEntryFolder
			_getParentObjectEntryFolder(
				com.liferay.object.model.ObjectEntryFolder objectEntryFolder)
		throws Exception {

		if (objectEntryFolder.getParentObjectEntryFolderId() > 0L) {
			return _objectEntryFolderLocalService.getObjectEntryFolder(
				objectEntryFolder.getParentObjectEntryFolderId());
		}

		return null;
	}

	private ParentObjectEntryFolderBrief _getParentObjectEntryFolderBrief(
		DTOConverterContext dtoConverterContext,
		com.liferay.object.model.ObjectEntryFolder parentObjectEntryFolder) {

		if (parentObjectEntryFolder == null) {
			return null;
		}

		return new ParentObjectEntryFolderBrief() {
			{
				setExternalReferenceCode(
					parentObjectEntryFolder::getExternalReferenceCode);
				setId(parentObjectEntryFolder::getObjectEntryFolderId);
				setLabel(
					() -> parentObjectEntryFolder.getLabel(
						dtoConverterContext.getLocale()));
				setLabel_i18n(
					() -> LocalizedMapUtil.getLanguageIdMap(
						parentObjectEntryFolder.getLabelMap()));
				setTitle(parentObjectEntryFolder::getName);
			}
		};
	}

	private Permission[] _toPermissions(
			com.liferay.object.model.ObjectEntryFolder objectEntryFolder)
		throws Exception {

		return NestedFieldsSupplier.supply(
			"permissions",
			nestedFieldNames -> {
				_permissionService.checkPermission(
					objectEntryFolder.getGroupId(),
					com.liferay.object.model.ObjectEntryFolder.class.getName(),
					objectEntryFolder.getObjectEntryFolderId());

				Collection<Permission> permissions =
					PermissionUtil.getPermissions(
						objectEntryFolder.getCompanyId(),
						_resourceActionLocalService.getResourceActions(
							com.liferay.object.model.ObjectEntryFolder.class.
								getName()),
						objectEntryFolder.getObjectEntryFolderId(),
						com.liferay.object.model.ObjectEntryFolder.class.
							getName(),
						null);

				return permissions.toArray(new Permission[0]);
			});
	}

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private ObjectEntryFolderLocalService _objectEntryFolderLocalService;

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

	@Reference
	private PermissionService _permissionService;

	@Reference
	private Portal _portal;

	@Reference
	private ResourceActionLocalService _resourceActionLocalService;

	@Reference
	private UserLocalService _userLocalService;

}