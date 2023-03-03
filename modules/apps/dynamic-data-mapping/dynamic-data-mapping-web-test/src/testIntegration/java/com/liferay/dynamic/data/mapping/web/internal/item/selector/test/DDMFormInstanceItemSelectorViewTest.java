/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.dynamic.data.mapping.web.internal.item.selector.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.dynamic.data.mapping.model.DDMFormInstance;
import com.liferay.dynamic.data.mapping.test.util.DDMFormInstanceTestUtil;
import com.liferay.item.selector.ItemSelectorView;
import com.liferay.item.selector.criteria.info.item.criterion.InfoItemItemSelectorCriterion;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletRenderRequest;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletRenderResponse;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletURL;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.JavaConstants;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author Mikel Lorza
 */
@RunWith(Arquillian.class)
public class DDMFormInstanceItemSelectorViewTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();
	}

	@Test
	public void testGetSearchContainer() throws Exception {
		DDMFormInstance expectedDDMFormInstance =
			DDMFormInstanceTestUtil.addDDMFormInstance(
				_group, TestPropsValues.getUserId());

		SearchContainer<DDMFormInstance> searchContainer = _getSearchContainer(
			_getMockHttpServletRequest());

		Assert.assertEquals(1, searchContainer.getTotal());
		Assert.assertEquals(
			"no-entries-were-found", searchContainer.getEmptyResultsMessage());
		Assert.assertEquals("modified-date", searchContainer.getOrderByCol());
		Assert.assertEquals("asc", searchContainer.getOrderByType());

		List<DDMFormInstance> ddmFormInstances = searchContainer.getResults();

		Assert.assertEquals(
			ddmFormInstances.toString(), 1, ddmFormInstances.size());

		DDMFormInstance actualDDMFormInstance = ddmFormInstances.get(0);

		Assert.assertEquals(
			expectedDDMFormInstance.getFormInstanceId(),
			actualDDMFormInstance.getFormInstanceId());
	}

	@Test
	public void testGetTitle() {
		Assert.assertEquals(
			"Forms", _itemSelectorView.getTitle(LocaleUtil.ENGLISH));
	}

	@Test
	public void testIsMultipleSelection() throws Exception {
		Assert.assertFalse(_isMultipleSelection(_getMockHttpServletRequest()));
	}

	@Test
	public void testIsVisible() throws Exception {
		Assert.assertTrue(
			_itemSelectorView.isVisible(
				new InfoItemItemSelectorCriterion(), _getThemeDisplay()));
	}

	private MockHttpServletRequest _getMockHttpServletRequest()
		throws Exception {

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		MockLiferayPortletRenderRequest mockLiferayPortletRenderRequest =
			new MockLiferayPortletRenderRequest();

		mockLiferayPortletRenderRequest.setAttribute(
			StringBundler.concat(
				mockLiferayPortletRenderRequest.getPortletName(), "-",
				WebKeys.CURRENT_PORTLET_URL),
			new MockLiferayPortletURL());

		mockHttpServletRequest.setAttribute(
			JavaConstants.JAVAX_PORTLET_REQUEST,
			mockLiferayPortletRenderRequest);

		mockHttpServletRequest.setAttribute(
			JavaConstants.JAVAX_PORTLET_RESPONSE,
			new MockLiferayPortletRenderResponse());

		ThemeDisplay themeDisplay = _getThemeDisplay();

		themeDisplay.setRequest(mockHttpServletRequest);

		mockHttpServletRequest.setAttribute(
			WebKeys.THEME_DISPLAY, themeDisplay);

		return mockHttpServletRequest;
	}

	private SearchContainer<DDMFormInstance> _getSearchContainer(
			MockHttpServletRequest mockHttpServletRequest)
		throws Exception {

		_itemSelectorView.renderHTML(
			mockHttpServletRequest, new MockHttpServletResponse(),
			new InfoItemItemSelectorCriterion(), new MockLiferayPortletURL(),
			RandomTestUtil.randomString(), true);

		return ReflectionTestUtil.invoke(
			mockHttpServletRequest.getAttribute(
				"com.liferay.item.selector.web.internal.display.context." +
					"ItemSelectorViewDescriptorRendererDisplayContext"),
			"getSearchContainer", new Class<?>[0], null);
	}

	private ThemeDisplay _getThemeDisplay() throws Exception {
		ThemeDisplay themeDisplay = new ThemeDisplay();

		themeDisplay.setCompany(
			CompanyLocalServiceUtil.fetchCompany(_group.getCompanyId()));
		themeDisplay.setLocale(LocaleUtil.getDefault());
		themeDisplay.setScopeGroupId(_group.getGroupId());
		themeDisplay.setUser(TestPropsValues.getUser());

		return themeDisplay;
	}

	private boolean _isMultipleSelection(
			MockHttpServletRequest mockHttpServletRequest)
		throws Exception {

		_itemSelectorView.renderHTML(
			mockHttpServletRequest, new MockHttpServletResponse(),
			new InfoItemItemSelectorCriterion(), new MockLiferayPortletURL(),
			RandomTestUtil.randomString(), true);

		return ReflectionTestUtil.invoke(
			mockHttpServletRequest.getAttribute(
				"com.liferay.item.selector.web.internal.display.context." +
					"ItemSelectorViewDescriptorRendererDisplayContext"),
			"isMultipleSelection", new Class<?>[0], null);
	}

	@DeleteAfterTestRun
	private Group _group;

	@Inject(
		filter = "component.name=*.DDMFormInstanceItemSelectorView",
		type = ItemSelectorView.class
	)
	private ItemSelectorView<InfoItemItemSelectorCriterion> _itemSelectorView;

}