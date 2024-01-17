/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.template.internal.info.field.transformer;

import com.liferay.info.field.InfoField;
import com.liferay.info.field.InfoFieldValue;
import com.liferay.info.field.type.InfoFieldType;
import com.liferay.info.field.type.OptionInfoFieldType;
import com.liferay.info.field.type.SelectInfoFieldType;
import com.liferay.portal.kernel.templateparser.TemplateNode;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.template.info.field.transformer.BaseTemplateNodeTransformer;
import com.liferay.template.info.field.transformer.TemplateNodeTransformer;
import com.liferay.template.internal.templateparser.SelectInfoFieldTypeTemplateNode;

import java.util.Collections;
import java.util.List;

import org.osgi.service.component.annotations.Component;

/**
 * @author Lourdes Fernández Besada
 */
@Component(
	property = "info.field.type.class.name=com.liferay.info.field.type.SelectInfoFieldType",
	service = TemplateNodeTransformer.class
)
public class SelectInfoFieldTypeTemplateNodeTransformer
	extends BaseTemplateNodeTransformer {

	@Override
	public TemplateNode transform(
		InfoFieldValue<Object> infoFieldValue, ThemeDisplay themeDisplay) {

		InfoField infoField = infoFieldValue.getInfoField();

		InfoFieldType infoFieldType = infoField.getInfoFieldType();

		TemplateNode templateNode = new SelectInfoFieldTypeTemplateNode(
			HashMapBuilder.put(
				"multiple", Boolean.FALSE.toString()
			).build(),
			infoFieldValue, infoField.getName(), themeDisplay,
			infoFieldType.getName());

		List<OptionInfoFieldType> optionInfoFieldTypes =
			(List<OptionInfoFieldType>)infoField.getAttribute(
				SelectInfoFieldType.OPTIONS);

		if (optionInfoFieldTypes == null) {
			optionInfoFieldTypes = Collections.emptyList();
		}

		for (OptionInfoFieldType optionInfoFieldType : optionInfoFieldTypes) {
			templateNode.appendOptionMap(
				optionInfoFieldType.getValue(),
				optionInfoFieldType.getLabel(themeDisplay.getLocale()));
		}

		return templateNode;
	}

}