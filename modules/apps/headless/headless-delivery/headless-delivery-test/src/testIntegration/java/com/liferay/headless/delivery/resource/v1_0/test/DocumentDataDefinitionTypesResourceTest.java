/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.delivery.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.document.library.kernel.model.DLFileEntryType;
import com.liferay.document.library.kernel.service.DLFileEntryTypeLocalService;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalService;
import com.liferay.headless.delivery.client.dto.v1_0.DocumentDataDefinitionType;
import com.liferay.headless.delivery.client.serdes.v1_0.DataDefinitionFieldSerDes;
import com.liferay.headless.delivery.client.serdes.v1_0.DataLayoutSerDes;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Javier Gamarra
 */
@RunWith(Arquillian.class)
public class DocumentDataDefinitionTypesResourceTest
	extends BaseDocumentDataDefinitionTypesResourceTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Override
	@Test
	public void testPostAssetLibraryDocumentDataDefinitionTypes()
		throws Exception {

		_assertDocumentDataDefinitionType(
			documentDataDefinitionTypesResource.
				postAssetLibraryDocumentDataDefinitionTypes(
					testDepotEntry.getDepotEntryId(),
					_createDocumentDataDefinitionType()),
			testDepotEntry.getGroupId());
	}

	@Override
	@Test
	public void testPostSiteDocumentDataDefinitionTypes() throws Exception {
		_assertDocumentDataDefinitionType(
			documentDataDefinitionTypesResource.
				postSiteDocumentDataDefinitionTypes(
					testGroup.getGroupId(),
					_createDocumentDataDefinitionType()),
			testGroup.getGroupId());
	}

	private void _assertDocumentDataDefinitionType(
			DocumentDataDefinitionType documentDataDefinitionType, long groupId)
		throws Exception {

		Assert.assertNotNull(documentDataDefinitionType);

		DLFileEntryType dlFileEntryType =
			_dlFileEntryTypeLocalService.getDLFileEntryType(
				documentDataDefinitionType.getId());

		Assert.assertNotNull(dlFileEntryType);
		Assert.assertEquals(
			HashMapBuilder.put(
				LocaleUtil.SPAIN, "Definición de datos del documento"
			).put(
				LocaleUtil.US, "Document data definition"
			).build(),
			dlFileEntryType.getNameMap());
	}

	private DocumentDataDefinitionType _createDocumentDataDefinitionType()
		throws Exception {

		return new DocumentDataDefinitionType() {
			{
				setAvailableLanguages(new String[] {"en_US", "es_ES"});
				setDataDefinitionFields(
					DataDefinitionFieldSerDes.toDTOs(
						_read("test-ddm-fields.json")));
				setDataLayout(
					DataLayoutSerDes.toDTO(_read("test-data-layout.json")));
				setName("Document data definition");
				setName_i18n(
					HashMapBuilder.put(
						"en-US", "Document data definition"
					).put(
						"es-ES", "Definición de datos del documento"
					).build());
			}
		};
	}

	private String _read(String fileName) throws Exception {
		Class<?> clazz = getClass();

		InputStream inputStream = clazz.getResourceAsStream(
			"dependencies/" + fileName);

		return StringUtil.read(inputStream);
	}

	@Inject
	private DDMStructureLocalService _ddmStructureLocalService;

	@Inject
	private DLFileEntryTypeLocalService _dlFileEntryTypeLocalService;

}