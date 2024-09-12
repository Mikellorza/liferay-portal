/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.content.dashboard.web.internal.item.type;

import com.liferay.content.dashboard.info.item.ClassNameClassPKInfoItemIdentifier;
import com.liferay.content.dashboard.item.type.ContentDashboardItemSubtype;
import com.liferay.content.dashboard.item.type.ContentDashboardItemSubtypeFactory;
import com.liferay.content.dashboard.item.type.ContentDashboardItemSubtypeFactoryRegistry;
import com.liferay.info.item.ClassPKInfoItemIdentifier;
import com.liferay.info.item.InfoItemReference;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Cristina González
 */
public class ContentDashboardItemSubtypeUtil {

	public static ContentDashboardItemSubtype toContentDashboardItemSubtype(
		ContentDashboardItemSubtypeFactoryRegistry
			contentDashboardItemSubtypeFactoryRegistry,
		InfoItemReference infoItemReference) {

		if (infoItemReference.getInfoItemIdentifier() instanceof
				ClassNameClassPKInfoItemIdentifier) {

			ClassNameClassPKInfoItemIdentifier
				classNameClassPKInfoItemIdentifier =
					(ClassNameClassPKInfoItemIdentifier)
						infoItemReference.getInfoItemIdentifier();

			return _toContentDashboardItemSubtype(
				contentDashboardItemSubtypeFactoryRegistry.
					getContentDashboardItemSubtypeFactory(
						classNameClassPKInfoItemIdentifier.getClassName()),
				classNameClassPKInfoItemIdentifier.getClassPK());
		}
		else if (infoItemReference.getInfoItemIdentifier() instanceof
					ClassPKInfoItemIdentifier) {

			ClassPKInfoItemIdentifier classPKInfoItemIdentifier =
				(ClassPKInfoItemIdentifier)
					infoItemReference.getInfoItemIdentifier();

			return _toContentDashboardItemSubtype(
				contentDashboardItemSubtypeFactoryRegistry.
					getContentDashboardItemSubtypeFactory(
						infoItemReference.getClassName()),
				classPKInfoItemIdentifier.getClassPK());
		}

		return null;
	}

	public static ContentDashboardItemSubtype toContentDashboardItemSubtype(
		ContentDashboardItemSubtypeFactoryRegistry
			contentDashboardItemSubtypeFactoryRegistry,
		JSONObject contentDashboardItemSubtypePayloadJSONObject) {

		String className =
			contentDashboardItemSubtypePayloadJSONObject.getString("className");

		if (Validator.isNull(className)) {
			return toContentDashboardItemSubtype(
				contentDashboardItemSubtypeFactoryRegistry,
				new InfoItemReference(
					contentDashboardItemSubtypePayloadJSONObject.getString(
						"entryClassName"),
					0));
		}

		return toContentDashboardItemSubtype(
			contentDashboardItemSubtypeFactoryRegistry,
			new InfoItemReference(
				contentDashboardItemSubtypePayloadJSONObject.getString(
					"entryClassName"),
				new ClassNameClassPKInfoItemIdentifier(
					GetterUtil.getString(className),
					GetterUtil.getLong(
						contentDashboardItemSubtypePayloadJSONObject.getLong(
							"classPK")))));
	}

	public static ContentDashboardItemSubtype toContentDashboardItemSubtype(
		ContentDashboardItemSubtypeFactoryRegistry
			contentDashboardItemSubtypeFactoryRegistry,
		String contentDashboardItemSubtypePayload) {

		try {
			return toContentDashboardItemSubtype(
				contentDashboardItemSubtypeFactoryRegistry,
				JSONFactoryUtil.createJSONObject(
					contentDashboardItemSubtypePayload));
		}
		catch (JSONException jsonException) {
			_log.error(jsonException);

			return null;
		}
	}

	public static ContentDashboardItemSubtype toContentDashboardItemSubtype(
		ContentDashboardItemSubtypeFactoryRegistry
			contentDashboardItemSubtypeFactoryRegistry,
		String className, String classPK, String entryClassName) {

		if (Validator.isNull(className)) {
			return toContentDashboardItemSubtype(
				contentDashboardItemSubtypeFactoryRegistry,
				new InfoItemReference(entryClassName, 0));
		}

		return toContentDashboardItemSubtype(
			contentDashboardItemSubtypeFactoryRegistry,
			new InfoItemReference(
				entryClassName,
				new ClassNameClassPKInfoItemIdentifier(
					className, GetterUtil.getLong(classPK))));
	}

	public static List<ContentDashboardItemSubtype>
		toContentDashboardItemSubtypes(
			ContentDashboardItemSubtypeFactoryRegistry
				contentDashboardItemSubtypeFactoryRegistry,
			String contentDashboardItemSubtypePayload) {

		if (Validator.isNull(contentDashboardItemSubtypePayload)) {
			return Collections.emptyList();
		}

		List<ContentDashboardItemSubtype> contentDashboardItemSubtypes =
			new ArrayList<>();

		try {
			JSONArray jsonArray = JSONFactoryUtil.createJSONArray(
				contentDashboardItemSubtypePayload);

			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);

				if (jsonObject == null) {
					continue;
				}

				if (jsonObject.has("className")) {
					JSONArray classPKsJSONArray = jsonObject.getJSONArray(
						"classPKs");

					if (classPKsJSONArray == null) {
						continue;
					}

					for (int j = 0; j < classPKsJSONArray.length(); j++) {
						contentDashboardItemSubtypes.add(
							toContentDashboardItemSubtype(
								contentDashboardItemSubtypeFactoryRegistry,
								jsonObject.getString("className"),
								classPKsJSONArray.getString(j),
								jsonObject.getString(Field.ENTRY_CLASS_NAME)));
					}
				}
				else {
					contentDashboardItemSubtypes.add(
						toContentDashboardItemSubtype(
							contentDashboardItemSubtypeFactoryRegistry, null,
							null, jsonObject.getString("entryClassName")));
				}
			}
		}
		catch (JSONException jsonException) {
			_log.error(jsonException);
		}

		return contentDashboardItemSubtypes;
	}

	private static ContentDashboardItemSubtype _toContentDashboardItemSubtype(
		ContentDashboardItemSubtypeFactory contentDashboardItemSubtypeFactory,
		Long classPK) {

		try {
			if (contentDashboardItemSubtypeFactory != null) {
				return contentDashboardItemSubtypeFactory.create(classPK);
			}
		}
		catch (PortalException portalException) {
			_log.error(portalException);
		}

		return null;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ContentDashboardItemSubtypeUtil.class);

}