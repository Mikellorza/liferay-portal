/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.search.similar.asset;

import com.liferay.object.constants.ObjectFieldConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectField;
import com.liferay.object.model.bag.ObjectFieldBag;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.io.Serializable;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Mikel Lorza
 */
public class CMSContentSimilarAssetTitleExtractorTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_cmsContentTitleSimilarityElementExtractor =
			new CMSContentSimilarAssetTitleExtractor();
	}

	@Test
	public void testGetElements() throws Exception {
		Assert.assertEquals(
			new HashSet<>(
				Arrays.asList("summ", "umme", "mmer", "mer ", "er s", "r sa")),
			_cmsContentTitleSimilarityElementExtractor.getElements(
				_mockObjectEntry("Summer Sa"), "en_US"));
	}

	@Test
	public void testGetElementsIgnoresCaseAndPunctuation() throws Exception {
		Assert.assertEquals(
			_cmsContentTitleSimilarityElementExtractor.getElements(
				_mockObjectEntry("Summer Sale"), "en_US"),
			_cmsContentTitleSimilarityElementExtractor.getElements(
				_mockObjectEntry("SUMMER, SALE!"), "en_US"));
	}

	@Test
	public void testGetElementsWithoutTranslation() throws Exception {
		Assert.assertEquals(
			Collections.emptySet(),
			_cmsContentTitleSimilarityElementExtractor.getElements(
				_mockObjectEntry("Summer Sale"), "es_ES"));
	}

	@Test
	public void testGetElementsWithShortTitle() throws Exception {

		// A title shorter than the n gram is one element, so it can only ever
		// group with the same short title

		Assert.assertEquals(
			Collections.singleton("faq"),
			_cmsContentTitleSimilarityElementExtractor.getElements(
				_mockObjectEntry("FAQ"), "en_US"));
	}

	@Test
	public void testGetElementsWithTypoKeepsMostElements() throws Exception {
		Set<String> elements =
			_cmsContentTitleSimilarityElementExtractor.getElements(
				_mockObjectEntry("Quarterly Security Report"), "en_US");
		Set<String> typoElements =
			_cmsContentTitleSimilarityElementExtractor.getElements(
				_mockObjectEntry("Quarterly Securty Report"), "en_US");

		Set<String> sharedElements = new HashSet<>(elements);

		sharedElements.retainAll(typoElements);

		// A single missing letter costs the n grams covering it and nothing
		// else, which is what word shingles cannot do

		Assert.assertTrue(
			StringBundler.concat(
				"A one letter typo must keep most of the ", elements.size(),
				" n grams, kept: ", sharedElements.size()),
			sharedElements.size() >= (elements.size() - 4));
	}

	@Test
	public void testGetTokenLanguageIds() throws Exception {
		Assert.assertEquals(
			Arrays.asList("en_US", "es_ES"),
			ListUtil.fromCollection(
				_cmsContentTitleSimilarityElementExtractor.getTokenLanguageIds(
					_mockObjectEntry(
						HashMapBuilder.<String, Serializable>put(
							"i18nTitle",
							HashMapBuilder.put(
								"en_US", "Summer Sale"
							).put(
								"es_ES", "Rebajas de verano"
							).build()
						).build()))));
	}

	private ObjectEntry _mockObjectEntry(
			Map<String, Serializable> indexedValues)
		throws Exception {

		ObjectField titleObjectField = Mockito.mock(ObjectField.class);

		Mockito.when(
			titleObjectField.getBusinessType()
		).thenReturn(
			ObjectFieldConstants.BUSINESS_TYPE_TEXT
		);

		Mockito.when(
			titleObjectField.getI18nObjectFieldName()
		).thenReturn(
			"i18nTitle"
		);

		Mockito.when(
			titleObjectField.getName()
		).thenReturn(
			"title"
		);

		Mockito.when(
			titleObjectField.getObjectFieldId()
		).thenReturn(
			_OBJECT_FIELD_ID_TITLE
		);

		Mockito.when(
			titleObjectField.isIndexed()
		).thenReturn(
			true
		);

		Mockito.when(
			titleObjectField.isLocalized()
		).thenReturn(
			true
		);

		List<ObjectField> objectFields = Arrays.asList(titleObjectField);

		ObjectFieldBag objectFieldBag = new ObjectFieldBag(false, objectFields);

		ObjectDefinition objectDefinition = Mockito.mock(
			ObjectDefinition.class);

		Mockito.when(
			objectDefinition.getObjectFieldBag()
		).thenReturn(
			objectFieldBag
		);

		Mockito.when(
			objectDefinition.getTitleObjectFieldId()
		).thenReturn(
			_OBJECT_FIELD_ID_TITLE
		);

		ObjectEntry objectEntry = Mockito.mock(ObjectEntry.class);

		Mockito.when(
			objectEntry.getDefaultLanguageId()
		).thenReturn(
			"en_US"
		);

		Mockito.when(
			objectEntry.getIndexedValues()
		).thenReturn(
			indexedValues
		);

		Mockito.when(
			objectEntry.getObjectDefinition()
		).thenReturn(
			objectDefinition
		);

		return objectEntry;
	}

	private ObjectEntry _mockObjectEntry(String title) throws Exception {
		return _mockObjectEntry(
			HashMapBuilder.<String, Serializable>put(
				"i18nTitle",
				HashMapBuilder.put(
					"en_US", title
				).build()
			).build());
	}

	private static final long _OBJECT_FIELD_ID_TITLE = 1;

	private CMSContentSimilarAssetTitleExtractor
		_cmsContentTitleSimilarityElementExtractor;

}