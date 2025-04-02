/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.user.internal.dto.v1_0.converter;

import com.liferay.headless.admin.user.dto.v1_0.SharedAsset;
import com.liferay.headless.admin.user.internal.dto.v1_0.util.CreatorUtil;
import com.liferay.info.exception.NoSuchInfoItemException;
import com.liferay.info.field.InfoFieldValue;
import com.liferay.info.item.ClassPKInfoItemIdentifier;
import com.liferay.info.item.InfoItemServiceRegistry;
import com.liferay.info.item.provider.InfoItemFieldValuesProvider;
import com.liferay.info.item.provider.InfoItemObjectProvider;
import com.liferay.info.search.InfoSearchClassMapperRegistry;
import com.liferay.object.model.ObjectEntryFolder;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.File;
import com.liferay.portal.kernel.util.MimeTypesUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.vulcan.dto.converter.DTOConverter;
import com.liferay.portal.vulcan.dto.converter.DTOConverterContext;
import com.liferay.sharing.interpreter.SharingEntryInterpreter;
import com.liferay.sharing.interpreter.SharingEntryInterpreterProvider;
import com.liferay.sharing.model.SharingEntry;
import com.liferay.sharing.security.permission.SharingEntryAction;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Mikel Lorza
 */
@Component(
	property = "dto.class.name=com.liferay.sharing.model.SharingEntry",
	service = DTOConverter.class
)
public class SharedAssetDTOConverter
	implements DTOConverter<SharingEntry, SharedAsset> {

	@Override
	public String getContentType() {
		return SharingEntry.class.getSimpleName();
	}

	@Override
	public SharedAsset toDTO(
		DTOConverterContext dtoConverterContext, SharingEntry sharingEntry) {

		SharingEntryInterpreter sharingEntryInterpreter =
			_sharingEntryInterpreterProvider.getSharingEntryInterpreter(
				sharingEntry);

		return new SharedAsset() {
			{
				setActionIds(
					() -> TransformUtil.transformToArray(
						SharingEntryAction.getSharingEntryActions(
							sharingEntry.getActionIds()),
						SharingEntryAction::getActionId, String.class));
				setAssetType(
					() -> {
						if (sharingEntryInterpreter == null) {
							return null;
						}

						return sharingEntryInterpreter.getAssetTypeTitle(
							sharingEntry, dtoConverterContext.getLocale());
					});
				setAssetSubType(
					() -> {
						if (StringUtil.equals(ObjectEntryFolder.class.getName(),sharingEntry.getClassName())){
							return "folder";
						}

						Object object = _getInfoItem(sharingEntry.getClassName(),
							sharingEntry.getClassPK());

						if (object == null) {
							return null;
						}

						InfoItemFieldValuesProvider infoItemFieldValuesProvider =
							_getInfoItemFieldValuesProvider(
								sharingEntry.getClassName());

						if (infoItemFieldValuesProvider == null) {
							return null;
						}

						InfoFieldValue<Object> infoFieldValue = infoItemFieldValuesProvider.getInfoFieldValue(object,"mimeType");

						if (infoFieldValue!=null) {
							return (String) infoFieldValue.getValue();
						}

						return null;
					}
				);
				setClassName(sharingEntry::getClassName);
				setClassPK(sharingEntry::getClassPK);
				setCreator(
					() -> CreatorUtil.toCreator(
						_portal,
						_userLocalService.getUser(sharingEntry.getUserId())));
				setDateCreated(sharingEntry::getCreateDate);
				setDateModified(sharingEntry::getModifiedDate);
				setExternalReferenceCode(
					sharingEntry::getExternalReferenceCode);
				setId(sharingEntry::getSharingEntryId);
				setShareable(sharingEntry::isShareable);
				setSiteName(
					() -> {
						Group group = _groupLocalService.getGroup(
							sharingEntry.getGroupId());

						return group.getName(dtoConverterContext.getLocale());
					});
				setTitle(
					() -> {
						if (sharingEntryInterpreter == null) {
							return null;
						}

						return sharingEntryInterpreter.getTitle(
							sharingEntry, dtoConverterContext.getLocale());
					});
			}
		};
	}

	private Object _getInfoItem(String className,long classPK)
		throws NoSuchInfoItemException {

		InfoItemObjectProvider<Object> infoItemObjectProvider =
			_infoItemServiceRegistry.getFirstInfoItemService(
				InfoItemObjectProvider.class, className);

		if (infoItemObjectProvider == null) {
			return null;
		}

		return infoItemObjectProvider.getInfoItem(
			new ClassPKInfoItemIdentifier(classPK)
		);

	}

	private InfoItemFieldValuesProvider<Object> _getInfoItemFieldValuesProvider(
		String className) {

		className = _infoSearchClassMapperRegistry.getClassName(className);

		InfoItemFieldValuesProvider<Object> infoItemFieldValuesProvider =
			_infoItemServiceRegistry.getFirstInfoItemService(
				InfoItemFieldValuesProvider.class, className);

		if (infoItemFieldValuesProvider == null) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to get info item form provider for class " +
					className);
			}

			return null;
		}

		return infoItemFieldValuesProvider;
	}

	@Reference
	private InfoItemServiceRegistry _infoItemServiceRegistry;

	@Reference
	private InfoSearchClassMapperRegistry _infoSearchClassMapperRegistry;

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private Portal _portal;

	@Reference
	private SharingEntryInterpreterProvider _sharingEntryInterpreterProvider;

	@Reference
	private UserLocalService _userLocalService;

	@Reference
	private ClassNameLocalService _classNameLocalService;


	private static final Log _log = LogFactoryUtil.getLog(
		SharedAssetDTOConverter.class);
}