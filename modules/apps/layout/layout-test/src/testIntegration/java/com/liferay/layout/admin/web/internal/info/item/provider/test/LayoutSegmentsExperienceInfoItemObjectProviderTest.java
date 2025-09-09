/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.admin.web.internal.info.item.provider.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.info.exception.NoSuchInfoItemException;
import com.liferay.info.item.ClassPKInfoItemIdentifier;
import com.liferay.info.item.ERCInfoItemIdentifier;
import com.liferay.info.item.provider.InfoItemObjectProvider;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.test.AssertUtils;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.segments.model.SegmentsEntry;
import com.liferay.segments.model.SegmentsExperience;
import com.liferay.segments.test.util.SegmentsTestUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Mikel Lorza
 */
@RunWith(Arquillian.class)
public class LayoutSegmentsExperienceInfoItemObjectProviderTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		Layout layout = LayoutTestUtil.addTypeContentLayout(_group);

		SegmentsEntry segmentsEntry = SegmentsTestUtil.addSegmentsEntry(
			layout.getGroupId());

		_segmentsExperience = SegmentsTestUtil.addSegmentsExperience(
			layout.getGroupId(), segmentsEntry.getSegmentsEntryId(),
			layout.getPlid());

		ServiceContextThreadLocal.pushServiceContext(
			ServiceContextTestUtil.getServiceContext(
				_group, TestPropsValues.getUserId()));
	}

	@After
	public void tearDown() throws Exception {
		ServiceContextThreadLocal.popServiceContext();
	}

	@Test
	public void testGetInfoItem() throws Exception {
		long groupId = RandomTestUtil.randomLong();
		long segmentsEntryId = RandomTestUtil.randomLong();

		AssertUtils.assertFailure(
			NoSuchInfoItemException.class,
			StringBundler.concat(
				"com.liferay.segments.exception.NoSuchExperienceException: No ",
				"SegmentsExperience exists with the primary key ",
				segmentsEntryId),
			() -> _layoutSegmentsExperienceInfoItemObjectProvider.getInfoItem(
				groupId, new ClassPKInfoItemIdentifier(segmentsEntryId)));

		AssertUtils.assertFailure(
			NoSuchInfoItemException.class,
			StringBundler.concat(
				"com.liferay.segments.exception.NoSuchExperienceException: No ",
				"SegmentsExperience exists with the key {",
				"externalReferenceCode=",
				_segmentsExperience.getExternalReferenceCode(), ", groupId=",
				groupId, "}"),
			() -> _layoutSegmentsExperienceInfoItemObjectProvider.getInfoItem(
				groupId,
				new ERCInfoItemIdentifier(
					_segmentsExperience.getExternalReferenceCode())));

		Assert.assertEquals(
			_segmentsExperience,
			_layoutSegmentsExperienceInfoItemObjectProvider.getInfoItem(
				_group.getGroupId(),
				new ClassPKInfoItemIdentifier(
					_segmentsExperience.getSegmentsExperienceId())));
		Assert.assertEquals(
			_segmentsExperience,
			_layoutSegmentsExperienceInfoItemObjectProvider.getInfoItem(
				RandomTestUtil.randomLong(),
				new ClassPKInfoItemIdentifier(
					_segmentsExperience.getSegmentsExperienceId())));
		Assert.assertEquals(
			_segmentsExperience,
			_layoutSegmentsExperienceInfoItemObjectProvider.getInfoItem(
				_group.getGroupId(),
				new ERCInfoItemIdentifier(
					_segmentsExperience.getExternalReferenceCode())));
		Assert.assertEquals(
			_segmentsExperience,
			_layoutSegmentsExperienceInfoItemObjectProvider.getInfoItem(
				RandomTestUtil.randomLong(),
				new ERCInfoItemIdentifier(
					_segmentsExperience.getExternalReferenceCode(),
					_group.getExternalReferenceCode())));
	}

	@DeleteAfterTestRun
	private Group _group;

	@Inject(
		filter = "component.name=com.liferay.layout.admin.web.internal.info.item.provider.LayoutSegmentsExperienceInfoItemObjectProvider"
	)
	private InfoItemObjectProvider<SegmentsExperience>
		_layoutSegmentsExperienceInfoItemObjectProvider;

	private SegmentsExperience _segmentsExperience;

}