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

package com.liferay.segments.web.internal.display.context.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutTypePortlet;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.template.react.renderer.ComponentDescriptor;
import com.liferay.portal.template.react.renderer.ReactRenderer;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.product.navigation.control.menu.ProductNavigationControlMenuEntry;
import com.liferay.segments.constants.SegmentsEntryConstants;
import com.liferay.segments.model.SegmentsEntry;
import com.liferay.segments.model.SegmentsExperience;
import com.liferay.segments.service.SegmentsEntryLocalService;
import com.liferay.segments.service.SegmentsExperienceLocalService;
import com.liferay.segments.test.util.SegmentsTestUtil;

import java.io.IOException;
import java.io.Writer;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

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
public class SegmentsExperienceSelectorDisplayContextTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		_layout = LayoutTestUtil.addTypeContentLayout(_group);
	}

	@Test
	public void testGetDataWithDefaultSegmentsExperience() throws Exception {
		long defaultSegmentsExperienceId =
			_segmentsExperienceLocalService.fetchDefaultSegmentsExperienceId(
				_layout.getPlid());

		SegmentsExperience expectedDefaultSegmentsExperience =
			_segmentsExperienceLocalService.fetchSegmentsExperience(
				defaultSegmentsExperienceId);

		Assert.assertNotNull(expectedDefaultSegmentsExperience);

		Map<String, Object> actualData = _renderSegmentExperienceSelector(
			expectedDefaultSegmentsExperience.getSegmentsExperienceId());

		Assert.assertNotNull(actualData);

		JSONArray actualSegmentsExperiencesJSONArray =
			(JSONArray)actualData.get("segmentsExperiences");

		Assert.assertEquals(1, actualSegmentsExperiencesJSONArray.length());

		JSONObject actualSegmentsExperienceJSONObject =
			actualSegmentsExperiencesJSONArray.getJSONObject(0);

		Assert.assertTrue(
			actualSegmentsExperienceJSONObject.getBoolean("active"));
		Assert.assertEquals(
			expectedDefaultSegmentsExperience.getSegmentsEntryId(),
			actualSegmentsExperienceJSONObject.getLong("segmentsEntryId"));
		Assert.assertEquals(
			SegmentsEntryConstants.getDefaultSegmentsEntryName(
				LocaleUtil.ENGLISH),
			actualSegmentsExperienceJSONObject.getString("segmentsEntryName"));
		Assert.assertEquals(
			expectedDefaultSegmentsExperience.getSegmentsExperienceId(),
			actualSegmentsExperienceJSONObject.getLong("segmentsExperienceId"));
		Assert.assertEquals(
			expectedDefaultSegmentsExperience.getName(LocaleUtil.ENGLISH),
			actualSegmentsExperienceJSONObject.getString(
				"segmentsExperienceName"));
		Assert.assertEquals(
			"Active",
			actualSegmentsExperienceJSONObject.getString("statusLabel"));
		Assert.assertNotNull(
			actualSegmentsExperienceJSONObject.getString("url"));

		JSONObject actualSelectedSegmentsExperienceJSONObject =
			(JSONObject)actualData.get("selectedSegmentsExperience");

		Assert.assertEquals(
			expectedDefaultSegmentsExperience.getSegmentsExperienceId(),
			actualSelectedSegmentsExperienceJSONObject.getLong(
				"segmentsExperienceId"));
	}

	@Test
	public void testGetDataWithDefaultSegmentsExperienceAndSelectedAnExperienceDoesNotBelongToLayout()
		throws Exception {

		long defaultSegmentsExperienceId =
			_segmentsExperienceLocalService.fetchDefaultSegmentsExperienceId(
				_layout.getPlid());

		SegmentsExperience expectedDefaultSegmentsExperience =
			_segmentsExperienceLocalService.fetchSegmentsExperience(
				defaultSegmentsExperienceId);

		Layout layout = LayoutTestUtil.addTypeContentLayout(_group);

		SegmentsExperience segmentsExperience =
			SegmentsTestUtil.addSegmentsExperience(
				_group.getGroupId(), layout.getPlid());

		Map<String, Object> actualData = _renderSegmentExperienceSelector(
			segmentsExperience.getSegmentsExperienceId());

		JSONObject actualSelectedSegmentsExperienceJSONObject =
			(JSONObject)actualData.get("selectedSegmentsExperience");

		Assert.assertEquals(
			expectedDefaultSegmentsExperience.getSegmentsExperienceId(),
			actualSelectedSegmentsExperienceJSONObject.getLong(
				"segmentsExperienceId"));
	}

	@Test
	public void testGetDataWithDefaultSegmentsExperienceAndSelectedAnonExistingSegmentsExperience()
		throws Exception {

		long defaultSegmentsExperienceId =
			_segmentsExperienceLocalService.fetchDefaultSegmentsExperienceId(
				_layout.getPlid());

		SegmentsExperience expectedDefaultSegmentsExperience =
			_segmentsExperienceLocalService.fetchSegmentsExperience(
				defaultSegmentsExperienceId);

		Map<String, Object> actualData = _renderSegmentExperienceSelector(
			123456);

		JSONObject actualSelectedSegmentsExperienceJSONObject =
			(JSONObject)actualData.get("selectedSegmentsExperience");

		Assert.assertEquals(
			expectedDefaultSegmentsExperience.getSegmentsExperienceId(),
			actualSelectedSegmentsExperienceJSONObject.getLong(
				"segmentsExperienceId"));
	}

	@Test
	public void testGetDataWithSegmentsExperience() throws Exception {
		SegmentsExperience expectedSegmentsExperience =
			SegmentsTestUtil.addSegmentsExperience(
				_group.getGroupId(), _layout.getPlid());

		SegmentsEntry expectedSegmentsEntry =
			_segmentsEntryLocalService.fetchSegmentsEntry(
				expectedSegmentsExperience.getSegmentsEntryId());

		Assert.assertNotNull(expectedSegmentsEntry);

		Map<String, Object> actualData = _renderSegmentExperienceSelector(
			expectedSegmentsExperience.getSegmentsExperienceId());

		Assert.assertNotNull(actualData);

		JSONArray actualSegmentsExperiencesJSONArray =
			(JSONArray)actualData.get("segmentsExperiences");

		Assert.assertEquals(2, actualSegmentsExperiencesJSONArray.length());

		JSONObject actualSegmentsExperienceJSONObject =
			actualSegmentsExperiencesJSONArray.getJSONObject(1);

		Assert.assertFalse(
			actualSegmentsExperienceJSONObject.getBoolean("active"));
		Assert.assertEquals(
			expectedSegmentsExperience.getSegmentsEntryId(),
			actualSegmentsExperienceJSONObject.getLong("segmentsEntryId"));
		Assert.assertEquals(
			expectedSegmentsEntry.getName(LocaleUtil.ENGLISH),
			actualSegmentsExperienceJSONObject.getString("segmentsEntryName"));
		Assert.assertEquals(
			expectedSegmentsExperience.getSegmentsExperienceId(),
			actualSegmentsExperienceJSONObject.getLong("segmentsExperienceId"));
		Assert.assertEquals(
			expectedSegmentsExperience.getName(LocaleUtil.ENGLISH),
			actualSegmentsExperienceJSONObject.getString(
				"segmentsExperienceName"));
		Assert.assertEquals(
			"Inactive",
			actualSegmentsExperienceJSONObject.getString("statusLabel"));
		Assert.assertNotNull(
			actualSegmentsExperienceJSONObject.getString("url"));

		JSONObject actualSelectedSegmentsExperienceJSONObject =
			(JSONObject)actualData.get("selectedSegmentsExperience");

		Assert.assertEquals(
			expectedSegmentsExperience.getSegmentsExperienceId(),
			actualSelectedSegmentsExperienceJSONObject.getLong(
				"segmentsExperienceId"));
	}

	private MockHttpServletRequest _getMockHttpServletRequest(
			long segmentsExperienceId)
		throws Exception {

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.setAttribute(
			WebKeys.CURRENT_URL, "http://localhost:8080/");

		mockHttpServletRequest.setAttribute(
			WebKeys.THEME_DISPLAY, _getThemeDisplay());

		mockHttpServletRequest.addParameter(
			"segmentsExperienceId", String.valueOf(segmentsExperienceId));

		return mockHttpServletRequest;
	}

	private ThemeDisplay _getThemeDisplay() throws Exception {
		ThemeDisplay themeDisplay = new ThemeDisplay();

		themeDisplay.setCompany(
			_companyLocalService.getCompany(TestPropsValues.getCompanyId()));
		themeDisplay.setLayout(_layout);
		themeDisplay.setLayoutTypePortlet(
			(LayoutTypePortlet)_layout.getLayoutType());
		themeDisplay.setLocale(LocaleUtil.ENGLISH);
		themeDisplay.setPermissionChecker(
			PermissionCheckerFactoryUtil.create(TestPropsValues.getUser()));
		themeDisplay.setPlid(_layout.getPlid());
		themeDisplay.setScopeGroupId(_group.getGroupId());
		themeDisplay.setUser(TestPropsValues.getUser());

		return themeDisplay;
	}

	private Map<String, Object> _renderSegmentExperienceSelector(
			long selectedSegmentsExperienceId)
		throws Exception {

		Assert.assertNotNull(_productNavigationControlMenuEntry);

		MockReactRenderer mockReactRenderer = new MockReactRenderer();

		ReflectionTestUtil.setFieldValue(
			_productNavigationControlMenuEntry, "_reactRenderer",
			mockReactRenderer);

		_productNavigationControlMenuEntry.includeIcon(
			_getMockHttpServletRequest(selectedSegmentsExperienceId),
			new MockHttpServletResponse());

		return mockReactRenderer.getData();
	}

	@Inject
	private CompanyLocalService _companyLocalService;

	@DeleteAfterTestRun
	private Group _group;

	private Layout _layout;

	@Inject(
		filter = "component.name=com.liferay.segments.web.internal.product.navigation.control.menu.SegmentsExperienceSelectorProductNavigationControlMenuEntry"
	)
	private ProductNavigationControlMenuEntry
		_productNavigationControlMenuEntry;

	@Inject
	private SegmentsEntryLocalService _segmentsEntryLocalService;

	@Inject
	private SegmentsExperienceLocalService _segmentsExperienceLocalService;

	private class MockReactRenderer implements ReactRenderer {

		public Map<String, Object> getData() {
			return _data;
		}

		@Override
		public void renderReact(
				ComponentDescriptor componentDescriptor,
				Map<String, Object> data, HttpServletRequest httpServletRequest,
				Writer writer)
			throws IOException {

			_data = data;
		}

		private Map<String, Object> _data;

	}

}