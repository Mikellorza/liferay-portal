/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.site.internal.dto.v1_0.util;

import com.liferay.fragment.entry.processor.constants.FragmentEntryProcessorConstants;
import com.liferay.fragment.entry.processor.util.EditableFragmentEntryProcessorUtil;
import com.liferay.fragment.model.FragmentEntryLink;
import com.liferay.headless.admin.site.dto.v1_0.FragmentEditableElement;
import com.liferay.headless.admin.site.dto.v1_0.FragmentEditableElementValue;
import com.liferay.headless.admin.site.dto.v1_0.FragmentEditableElementValueFragmentLink;
import com.liferay.headless.admin.site.dto.v1_0.FragmentEditableInlineFragmentValue;
import com.liferay.headless.admin.site.dto.v1_0.FragmentEditableMappedFragmentValue;
import com.liferay.headless.admin.site.dto.v1_0.FragmentEditableValue;
import com.liferay.headless.admin.site.dto.v1_0.FragmentInlineValue;
import com.liferay.headless.admin.site.dto.v1_0.FragmentLink;
import com.liferay.headless.admin.site.dto.v1_0.FragmentMappedValue;
import com.liferay.headless.admin.site.dto.v1_0.TextFragmentEditableElementValue;
import com.liferay.headless.admin.site.dto.v1_0.TextFragmentEditableValue;
import com.liferay.info.item.InfoItemServiceRegistry;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.vulcan.util.LocalizedMapUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Rubén Pulido
 */
public class FragmentEditableElementUtil {

	public static FragmentEditableElement[] getFragmentEditableElements(
		long companyId, FragmentEntryLink fragmentEntryLink,
		InfoItemServiceRegistry infoItemServiceRegistry, long scopeGroupId) {

		JSONObject editableValuesJSONObject =
			fragmentEntryLink.getEditableValuesJSONObject();

		if (editableValuesJSONObject == null) {
			return null;
		}

		List<FragmentEditableElement> fragmentEditableElements =
			new ArrayList<>();

		JSONObject editableFragmentEntryProcessorJSONObject =
			editableValuesJSONObject.getJSONObject(
				FragmentEntryProcessorConstants.
					KEY_EDITABLE_FRAGMENT_ENTRY_PROCESSOR);

		if (editableFragmentEntryProcessorJSONObject == null) {
			return fragmentEditableElements.toArray(
				new FragmentEditableElement[0]);
		}

		fragmentEditableElements.addAll(
			_getTextFragmentEditableElements(
				companyId,
				EditableFragmentEntryProcessorUtil.getEditableTypes(
					fragmentEntryLink.getHtml()),
				infoItemServiceRegistry,
				editableFragmentEntryProcessorJSONObject, scopeGroupId));

		return fragmentEditableElements.toArray(new FragmentEditableElement[0]);
	}

	public static JSONObject
		getFragmentEditableElementsEditableValuesJSONObject(
			long companyId, FragmentEditableElement[] fragmentEditableElements,
			InfoItemServiceRegistry infoItemServiceRegistry,
			long scopeGroupId) {

		return JSONUtil.put(
			FragmentEntryProcessorConstants.
				KEY_EDITABLE_FRAGMENT_ENTRY_PROCESSOR,
			() -> {
				JSONObject editableFragmentEntryProcessorJSONObject =
					_getEditableFragmentEntryProcessorJSONObject(
						companyId, fragmentEditableElements,
						infoItemServiceRegistry, scopeGroupId);

				if (editableFragmentEntryProcessorJSONObject.length() > 0) {
					return editableFragmentEntryProcessorJSONObject;
				}

				return null;
			});
	}

	private static JSONObject _getEditableFragmentEntryProcessorJSONObject(
			long companyId, FragmentEditableElement[] fragmentEditableElements,
			InfoItemServiceRegistry infoItemServiceRegistry, long scopeGroupId)
		throws Exception {

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

		if (fragmentEditableElements == null) {
			return jsonObject;
		}

		for (FragmentEditableElement fragmentEditableElement :
				fragmentEditableElements) {

			if ((fragmentEditableElement == null) ||
				(fragmentEditableElement.getId() == null)) {

				continue;
			}

			FragmentEditableElementValue fragmentEditableElementValue =
				fragmentEditableElement.getFragmentEditableElementValue();

			if ((fragmentEditableElementValue == null) ||
				(fragmentEditableElementValue.getType() !=
					FragmentEditableElementValue.Type.TEXT)) {

				jsonObject.put(
					fragmentEditableElement.getId(), (JSONObject)null);

				continue;
			}

			jsonObject.put(
				fragmentEditableElement.getId(),
				_getFragmentEditableElementJSONObject(
					companyId, infoItemServiceRegistry, scopeGroupId,
					(TextFragmentEditableElementValue)
						fragmentEditableElementValue));
		}

		return jsonObject;
	}

	private static JSONObject _getFragmentEditableElementJSONObject(
			long companyId, InfoItemServiceRegistry infoItemServiceRegistry,
			long scopeGroupId,
			TextFragmentEditableElementValue textFragmentEditableElementValue)
		throws Exception {

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

		TextFragmentEditableValue textFragmentEditableValue =
			textFragmentEditableElementValue.getTextFragmentEditableValue();

		if (textFragmentEditableValue == null) {
			return jsonObject;
		}

		jsonObject.put(
			"config",
			() -> {
				FragmentEditableElementValueFragmentLink
					fragmentEditableElementValueFragmentLink =
						textFragmentEditableElementValue.
							getFragmentEditableElementValueFragmentLink();

				if (fragmentEditableElementValueFragmentLink == null) {
					return null;
				}

				JSONObject configJSONObject = FragmentLinkUtil.toJSONObject(
					companyId,
					fragmentEditableElementValueFragmentLink.getFragmentLink(),
					infoItemServiceRegistry, scopeGroupId);

				if (JSONUtil.isEmpty(configJSONObject)) {
					return null;
				}

				configJSONObject.put("mapperType", "link");

				FragmentEditableElementValueFragmentLink.Prefix prefix =
					fragmentEditableElementValueFragmentLink.getPrefix();

				if ((prefix == null) ||
					Objects.equals(
						prefix,
						FragmentEditableElementValueFragmentLink.Prefix.NONE)) {

					return configJSONObject;
				}

				if (Objects.equals(
						prefix,
						FragmentEditableElementValueFragmentLink.Prefix.
							EMAIL)) {

					return configJSONObject.put("prefix", "mailto:");
				}

				return configJSONObject.put("prefix", "tel:");
			}
		).put(
			"defaultValue", textFragmentEditableValue.getDefaultValue()
		);

		FragmentEditableValue fragmentEditableValue =
			textFragmentEditableValue.getFragmentEditableValue();

		if (fragmentEditableValue == null) {
			return jsonObject;
		}

		if (fragmentEditableValue instanceof
				FragmentEditableInlineFragmentValue) {

			FragmentEditableInlineFragmentValue
				fragmentEditableInlineFragmentValue =
					(FragmentEditableInlineFragmentValue)fragmentEditableValue;

			FragmentInlineValue fragmentInlineValue =
				fragmentEditableInlineFragmentValue.getFragmentInlineValue();

			if (fragmentInlineValue == null) {
				return jsonObject;
			}

			Map<String, String> languageIdMap =
				LocalizedMapUtil.getLanguageIdMap(
					LocalizedMapUtil.getLocalizedMap(
						fragmentInlineValue.getValue_i18n()));

			for (Map.Entry<String, String> entry : languageIdMap.entrySet()) {
				jsonObject.put(entry.getKey(), entry.getValue());
			}

			return jsonObject;
		}

		if (!(fragmentEditableValue instanceof
				FragmentEditableMappedFragmentValue)) {

			return jsonObject;
		}

		FragmentEditableMappedFragmentValue
			fragmentEditableMappedFragmentValue =
				(FragmentEditableMappedFragmentValue)fragmentEditableValue;

		FragmentMappedValue fragmentMappedValue =
			fragmentEditableMappedFragmentValue.getFragmentMappedValue();

		if (fragmentMappedValue == null) {
			return jsonObject;
		}

		return JSONUtil.merge(
			jsonObject,
			FragmentMappingUtil.getFragmentMappedValueJSONObject(
				companyId, infoItemServiceRegistry,
				fragmentMappedValue.getMapping(), scopeGroupId));
	}

	private static FragmentEditableValue _getFragmentEditableValue(
		long companyId, InfoItemServiceRegistry infoItemServiceRegistry,
		JSONObject jsonObject, long scopeGroupId) {

		if (FragmentMappingUtil.isMappedValue(jsonObject)) {
			FragmentEditableMappedFragmentValue
				fragmentEditableMappedFragmentValue =
					new FragmentEditableMappedFragmentValue();

			fragmentEditableMappedFragmentValue.setFragmentMappedValue(
				() -> FragmentMappingUtil.toFragmentMappedValue(
					companyId, infoItemServiceRegistry, jsonObject,
					scopeGroupId));

			return fragmentEditableMappedFragmentValue;
		}

		Map<String, String> i18nMap = LocalizedMapUtil.getI18nMap(
			true,
			LocalizedMapUtil.populateLocalizedMap(
				JSONUtil.toStringMap(jsonObject)));

		if (MapUtil.isEmpty(i18nMap)) {
			return null;
		}

		FragmentEditableInlineFragmentValue
			fragmentEditableInlineFragmentValue =
				new FragmentEditableInlineFragmentValue();

		fragmentEditableInlineFragmentValue.setFragmentInlineValue(
			() -> {
				FragmentInlineValue fragmentInlineValue =
					new FragmentInlineValue();

				fragmentInlineValue.setValue_i18n(() -> i18nMap);

				return fragmentInlineValue;
			});

		return fragmentEditableInlineFragmentValue;
	}

	private static List<FragmentEditableElement>
		_getTextFragmentEditableElements(
			long companyId, Map<String, String> editableTypes,
			final InfoItemServiceRegistry infoItemServiceRegistry,
			JSONObject jsonObject, long scopeGroupId) {

		return TransformUtil.transform(
			jsonObject.keySet(),
			textId -> new FragmentEditableElement() {
				{
					setFragmentEditableElementValue(
						() -> {
							String type = editableTypes.getOrDefault(
								textId, "text");

							if (!Objects.equals(type, "text")) {
								return null;
							}

							return _toTextFragmentEditableElementValue(
								companyId, infoItemServiceRegistry,
								jsonObject.getJSONObject(textId), scopeGroupId);
						});
					setId(() -> textId);
				}
			});
	}

	private static FragmentEditableElementValueFragmentLink
		_toFragmentEditableElementValueFragmentLink(
			long companyId, InfoItemServiceRegistry infoItemServiceRegistry,
			JSONObject jsonObject, long scopeGroupId) {

		FragmentLink fragmentLink = FragmentLinkUtil.toFragmentLink(
			companyId, infoItemServiceRegistry, jsonObject, scopeGroupId);

		if (fragmentLink == null) {
			return null;
		}

		FragmentEditableElementValueFragmentLink
			fragmentEditableElementValueFragmentLink =
				new FragmentEditableElementValueFragmentLink();

		fragmentEditableElementValueFragmentLink.setFragmentLink(
			() -> fragmentLink);
		fragmentEditableElementValueFragmentLink.setPrefix(
			() -> {
				if (!jsonObject.has("prefix")) {
					return null;
				}

				String prefix = jsonObject.getString("prefix");

				if (Validator.isNull(prefix)) {
					return FragmentEditableElementValueFragmentLink.Prefix.NONE;
				}

				if (prefix.equals("mailto:")) {
					return FragmentEditableElementValueFragmentLink.Prefix.
						EMAIL;
				}

				if (prefix.equals("tel:")) {
					return FragmentEditableElementValueFragmentLink.Prefix.
						PHONE;
				}

				return null;
			});

		return fragmentEditableElementValueFragmentLink;
	}

	private static TextFragmentEditableElementValue
		_toTextFragmentEditableElementValue(
			long companyId, InfoItemServiceRegistry infoItemServiceRegistry,
			JSONObject jsonObject, long scopeGroupId) {

		if (jsonObject == null) {
			return null;
		}

		TextFragmentEditableElementValue textFragmentEditableElementValue =
			new TextFragmentEditableElementValue();

		textFragmentEditableElementValue.
			setFragmentEditableElementValueFragmentLink(
				() -> _toFragmentEditableElementValueFragmentLink(
					companyId, infoItemServiceRegistry,
					jsonObject.getJSONObject("config"), scopeGroupId));
		textFragmentEditableElementValue.setTextFragmentEditableValue(
			() -> _toTextFragmentEditableValue(
				companyId, infoItemServiceRegistry, jsonObject, scopeGroupId));

		return textFragmentEditableElementValue;
	}

	private static TextFragmentEditableValue _toTextFragmentEditableValue(
		long companyId, InfoItemServiceRegistry infoItemServiceRegistry,
		JSONObject jsonObject, long scopeGroupId) {

		if (jsonObject == null) {
			return null;
		}

		TextFragmentEditableValue textFragmentEditableValue =
			new TextFragmentEditableValue();

		textFragmentEditableValue.setDefaultValue(
			() -> jsonObject.getString("defaultValue"));

		textFragmentEditableValue.setFragmentEditableValue(
			() -> _getFragmentEditableValue(
				companyId, infoItemServiceRegistry, jsonObject, scopeGroupId));

		return textFragmentEditableValue;
	}

}