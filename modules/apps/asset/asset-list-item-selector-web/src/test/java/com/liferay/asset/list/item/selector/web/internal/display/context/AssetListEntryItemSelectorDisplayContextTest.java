/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.asset.list.item.selector.web.internal.display.context;

import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.list.model.AssetListEntry;
import com.liferay.info.collection.provider.item.selector.InfoCollectionProviderItemSelectorCriterion;
import com.liferay.info.item.InfoItemServiceRegistry;
import com.liferay.info.item.provider.InfoItemFormProvider;
import com.liferay.info.search.InfoSearchClassMapperRegistry;
import com.liferay.portal.kernel.security.permission.ResourceActions;
import com.liferay.portal.kernel.security.permission.ResourceActionsUtil;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;

import org.junit.After;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Jürgen Kappler
 */
public class AssetListEntryItemSelectorDisplayContextTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@After
	public void tearDown() {
		ReflectionTestUtil.setFieldValue(
			ResourceActionsUtil.class, "_resourceActions",
			_originalResourceActions);
	}

	@Test
	public void testGetCreationMenuReturnsNullWhenAddAssetListEntryURLIsNull() {
		InfoCollectionProviderItemSelectorCriterion criterion =
			new InfoCollectionProviderItemSelectorCriterion();

		criterion.setAddDynamicAssetListEntryURL(
			"http://localhost/add-dynamic");

		AssetListEntryItemSelectorDisplayContext context =
			new AssetListEntryItemSelectorDisplayContext(
				Mockito.mock(HttpServletRequest.class), null, null, null, null,
				criterion);

		Assert.assertNull(context.getCreationMenu());
	}

	@Test
	public void testGetCreationMenuReturnsNullWhenAddDynamicAssetListEntryURLIsNull() {
		InfoCollectionProviderItemSelectorCriterion criterion =
			new InfoCollectionProviderItemSelectorCriterion();

		criterion.setAddAssetListEntryURL("http://localhost/add");

		AssetListEntryItemSelectorDisplayContext context =
			new AssetListEntryItemSelectorDisplayContext(
				Mockito.mock(HttpServletRequest.class), null, null, null, null,
				criterion);

		Assert.assertNull(context.getCreationMenu());
	}

	@Test
	public void testGetCreationMenuReturnsNullWhenBothURLsAreNull() {
		InfoCollectionProviderItemSelectorCriterion criterion =
			new InfoCollectionProviderItemSelectorCriterion();

		AssetListEntryItemSelectorDisplayContext context =
			new AssetListEntryItemSelectorDisplayContext(
				Mockito.mock(HttpServletRequest.class), null, null, null, null,
				criterion);

		Assert.assertNull(context.getCreationMenu());
	}

	@Test
	public void testGetInfoItemClassNames() {
		HttpServletRequest httpServletRequest = Mockito.mock(
			HttpServletRequest.class);

		ThemeDisplay themeDisplay = new ThemeDisplay();

		Mockito.when(
			(ThemeDisplay)httpServletRequest.getAttribute(WebKeys.THEME_DISPLAY)
		).thenReturn(
			themeDisplay
		);

		InfoItemServiceRegistry infoItemServiceRegistry = Mockito.mock(
			InfoItemServiceRegistry.class);

		Mockito.when(
			infoItemServiceRegistry.getInfoItemClassNames(
				InfoItemFormProvider.class)
		).thenReturn(
			Arrays.asList("className1", "className2")
		);

		InfoSearchClassMapperRegistry infoSearchClassMapperRegistry =
			Mockito.mock(InfoSearchClassMapperRegistry.class);

		Mockito.when(
			infoSearchClassMapperRegistry.getSearchClassName("className1")
		).thenReturn(
			"searchClassName1"
		);

		Mockito.when(
			infoSearchClassMapperRegistry.getSearchClassName("className2")
		).thenReturn(
			"className2"
		);

		AssetListEntryItemSelectorDisplayContext
			assetListEntryItemSelectorDisplayContext =
				new AssetListEntryItemSelectorDisplayContext(
					httpServletRequest, infoItemServiceRegistry,
					infoSearchClassMapperRegistry, null, null, null);

		String[] infoItemClassNames = ReflectionTestUtil.invoke(
			assetListEntryItemSelectorDisplayContext, "_getInfoItemClassNames",
			new Class<?>[0]);

		Assert.assertEquals(
			Arrays.toString(infoItemClassNames), 3, infoItemClassNames.length);

		Assert.assertTrue(ArrayUtil.contains(infoItemClassNames, "className1"));
		Assert.assertTrue(ArrayUtil.contains(infoItemClassNames, "className2"));
		Assert.assertTrue(
			ArrayUtil.contains(infoItemClassNames, "searchClassName1"));
	}

	@Test
	public void testGetTypeDefaultsToAssetEntryClassNameWhenAssetEntryTypeIsNull() {
		ResourceActions resourceActions = Mockito.mock(ResourceActions.class);

		Mockito.when(
			resourceActions.getModelResource(
				LocaleUtil.US, AssetEntry.class.getName())
		).thenReturn(
			"Asset"
		);

		ReflectionTestUtil.setFieldValue(
			ResourceActionsUtil.class, "_resourceActions", resourceActions);

		AssetListEntry assetListEntry = Mockito.mock(AssetListEntry.class);

		Mockito.when(
			assetListEntry.getAssetEntryType()
		).thenReturn(
			null
		);

		AssetListEntryItemSelectorDisplayContext context =
			new AssetListEntryItemSelectorDisplayContext(
				Mockito.mock(HttpServletRequest.class), null, null, null, null,
				null);

		Assert.assertEquals(
			"Asset", context.getType(assetListEntry, LocaleUtil.US));

		Mockito.verify(
			resourceActions
		).getModelResource(
			LocaleUtil.US, AssetEntry.class.getName()
		);
	}

	@Test
	public void testGetTypeUsesAssetEntryTypeWhenNotNull() {
		String className = "com.liferay.journal.model.JournalArticle";

		ResourceActions resourceActions = Mockito.mock(ResourceActions.class);

		Mockito.when(
			resourceActions.getModelResource(LocaleUtil.US, className)
		).thenReturn(
			"Web Content Article"
		);

		ReflectionTestUtil.setFieldValue(
			ResourceActionsUtil.class, "_resourceActions", resourceActions);

		AssetListEntry assetListEntry = Mockito.mock(AssetListEntry.class);

		Mockito.when(
			assetListEntry.getAssetEntryType()
		).thenReturn(
			className
		);

		AssetListEntryItemSelectorDisplayContext context =
			new AssetListEntryItemSelectorDisplayContext(
				Mockito.mock(HttpServletRequest.class), null, null, null, null,
				null);

		Assert.assertEquals(
			"Web Content Article",
			context.getType(assetListEntry, LocaleUtil.US));

		Mockito.verify(
			resourceActions
		).getModelResource(
			LocaleUtil.US, className
		);
	}

	private final ResourceActions _originalResourceActions =
		ReflectionTestUtil.getFieldValue(
			ResourceActionsUtil.class, "_resourceActions");

}