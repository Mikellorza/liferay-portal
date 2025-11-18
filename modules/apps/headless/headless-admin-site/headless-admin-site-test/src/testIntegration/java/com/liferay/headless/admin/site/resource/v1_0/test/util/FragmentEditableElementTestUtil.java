/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.site.resource.v1_0.test.util;

import com.liferay.headless.admin.site.client.dto.v1_0.FragmentEditableElement;
import com.liferay.headless.admin.site.client.dto.v1_0.FragmentEditableElementValueFragmentLink;
import com.liferay.headless.admin.site.client.dto.v1_0.FragmentEditableInlineFragmentValue;
import com.liferay.headless.admin.site.client.dto.v1_0.FragmentEditableMappedFragmentValue;
import com.liferay.headless.admin.site.client.dto.v1_0.FragmentEditableValue;
import com.liferay.headless.admin.site.client.dto.v1_0.FragmentInlineValue;
import com.liferay.headless.admin.site.client.dto.v1_0.FragmentLink;
import com.liferay.headless.admin.site.client.dto.v1_0.FragmentMappedValue;
import com.liferay.headless.admin.site.client.dto.v1_0.FragmentMappedValueItemContextReference;
import com.liferay.headless.admin.site.client.dto.v1_0.FragmentMappedValueItemExternalReference;
import com.liferay.headless.admin.site.client.dto.v1_0.FragmentMappedValueItemReference;
import com.liferay.headless.admin.site.client.dto.v1_0.Mapping;
import com.liferay.headless.admin.site.client.dto.v1_0.TextFragmentEditableElementValue;
import com.liferay.headless.admin.site.client.dto.v1_0.TextFragmentEditableValue;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;

/**
 * @author Rubén Pulido
 */
public class FragmentEditableElementTestUtil {

	public static FragmentEditableElement[] getFragmentEditableElements(
		FragmentEditableElementValueFragmentLink.Prefix prefix,
		FragmentLink fragmentLink,
		FragmentMappedValueItemContextReference.ContextSource contextSource,
		FragmentMappedValueItemReference.Type
			fragmentMappedValueItemReferenceType,
		FragmentEditableValue.Type fragmentEditableValueType) {

		return new FragmentEditableElement[] {
			new FragmentEditableElement() {
				{
					setFragmentEditableElementValue(
						() -> new TextFragmentEditableElementValue() {
							{
								setFragmentEditableElementValueFragmentLink(
									() ->
										_getFragmentEditableElementValueFragmentLink(
											prefix, fragmentLink));
								setTextFragmentEditableValue(
									() -> _getTextFragmentEditableValue(
										contextSource,
										fragmentMappedValueItemReferenceType,
										fragmentEditableValueType));
								setType(Type.TEXT);
							}
						});
					setId("element-text");
				}
			}
		};
	}

	private static FragmentEditableElementValueFragmentLink
		_getFragmentEditableElementValueFragmentLink(
			FragmentEditableElementValueFragmentLink.Prefix prefix,
			FragmentLink fragmentLink) {

		if (fragmentLink == null) {
			return null;
		}

		FragmentEditableElementValueFragmentLink
			fragmentEditableElementValueFragmentLink =
				new FragmentEditableElementValueFragmentLink();

		fragmentEditableElementValueFragmentLink.setFragmentLink(
			() -> fragmentLink);
		fragmentEditableElementValueFragmentLink.setPrefix(() -> prefix);

		return fragmentEditableElementValueFragmentLink;
	}

	private static FragmentEditableInlineFragmentValue
		_getFragmentEditableInlineFragmentValue() {

		return new FragmentEditableInlineFragmentValue() {
			{
				setFragmentInlineValue(
					() -> {
						FragmentInlineValue fragmentInlineValue =
							new FragmentInlineValue();

						fragmentInlineValue.setValue_i18n(
							() -> HashMapBuilder.put(
								"en-US", RandomTestUtil.randomString()
							).put(
								"es-ES", RandomTestUtil.randomString()
							).build());

						return fragmentInlineValue;
					});
				setType(Type.INLINE);
			}
		};
	}

	private static FragmentEditableMappedFragmentValue
		_getFragmentEditableMappedFragmentValue(
			FragmentMappedValueItemContextReference.ContextSource contextSource,
			FragmentMappedValueItemReference.Type
				fragmentMappedValueItemReferenceType) {

		return new FragmentEditableMappedFragmentValue() {
			{
				setFragmentMappedValue(
					() -> new FragmentMappedValue() {
						{
							setMapping(
								new Mapping() {
									{
										setFieldKey("field-key");
										setItemReference(
											_getFragmentMappedValueItemReference(
												contextSource,
												fragmentMappedValueItemReferenceType));
									}
								});
						}
					});
				setType(Type.MAPPED);
			}
		};
	}

	private static FragmentEditableValue _getFragmentEditableValue(
		FragmentMappedValueItemContextReference.ContextSource contextSource,
		FragmentMappedValueItemReference.Type
			fragmentMappedValueItemReferenceType,
		FragmentEditableValue.Type fragmentEditableValueType) {

		if (fragmentEditableValueType == FragmentEditableValue.Type.INLINE) {
			return _getFragmentEditableInlineFragmentValue();
		}

		if (fragmentEditableValueType == FragmentEditableValue.Type.MAPPED) {
			return _getFragmentEditableMappedFragmentValue(
				contextSource, fragmentMappedValueItemReferenceType);
		}

		return null;
	}

	private static FragmentMappedValueItemContextReference
		_getFragmentMappedValueItemContextReference(
			FragmentMappedValueItemContextReference.ContextSource
				contextSource) {

		FragmentMappedValueItemContextReference
			fragmentMappedValueItemContextReference =
				new FragmentMappedValueItemContextReference();

		fragmentMappedValueItemContextReference.setContextSource(contextSource);
		fragmentMappedValueItemContextReference.setType(
			FragmentMappedValueItemReference.Type.CONTEXT_REFERENCE);

		return fragmentMappedValueItemContextReference;
	}

	private static FragmentMappedValueItemExternalReference
		_getFragmentMappedValueItemExternalReference() {

		return new FragmentMappedValueItemExternalReference() {
			{
				setClassName(FileEntry.class.getName());
				setExternalReferenceCode(RandomTestUtil.randomString());
				setType(Type.ITEM_EXTERNAL_REFERENCE);
			}
		};
	}

	private static FragmentMappedValueItemReference
		_getFragmentMappedValueItemReference(
			FragmentMappedValueItemContextReference.ContextSource contextSource,
			FragmentMappedValueItemReference.Type type) {

		if (type == FragmentMappedValueItemReference.Type.CONTEXT_REFERENCE) {
			return _getFragmentMappedValueItemContextReference(contextSource);
		}

		if (type ==
				FragmentMappedValueItemReference.Type.ITEM_EXTERNAL_REFERENCE) {

			return _getFragmentMappedValueItemExternalReference();
		}

		return null;
	}

	private static TextFragmentEditableValue _getTextFragmentEditableValue(
		FragmentMappedValueItemContextReference.ContextSource contextSource,
		FragmentMappedValueItemReference.Type
			fragmentMappedValueItemReferenceType,
		FragmentEditableValue.Type fragmentEditableValueType) {

		TextFragmentEditableValue textFragmentEditableValue =
			new TextFragmentEditableValue();

		textFragmentEditableValue.setDefaultValue(RandomTestUtil::randomString);

		textFragmentEditableValue.setFragmentEditableValue(
			_getFragmentEditableValue(
				contextSource, fragmentMappedValueItemReferenceType,
				fragmentEditableValueType));

		return textFragmentEditableValue;
	}

}