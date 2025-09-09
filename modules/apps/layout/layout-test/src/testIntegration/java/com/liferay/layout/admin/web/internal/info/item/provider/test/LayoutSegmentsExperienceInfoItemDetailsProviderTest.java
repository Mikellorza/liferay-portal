/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.admin.web.internal.info.item.provider.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.info.item.ERCInfoItemIdentifier;
import com.liferay.info.item.InfoItemDetails;
import com.liferay.info.item.InfoItemReference;
import com.liferay.info.item.InfoItemServiceRegistry;
import com.liferay.info.item.provider.InfoItemDetailsProvider;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
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
public class LayoutSegmentsExperienceInfoItemDetailsProviderTest {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
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
	public void tearDown() {
		ServiceContextThreadLocal.popServiceContext();
	}

	@Test
	public void testGetInfoItem() {
		InfoItemDetails infoItemDetails =
			_layoutSegmentsExperienceInfoItemDetailsProvider.getInfoItemDetails(
				_segmentsExperience);

		Assert.assertEquals(
			SegmentsExperience.class.getName(), infoItemDetails.getClassName());
		Assert.assertEquals(
			new InfoItemReference(
				SegmentsExperience.class.getName(),
				_segmentsExperience.getSegmentsExperienceId()),
			infoItemDetails.getInfoItemReference());

		infoItemDetails =
			_layoutSegmentsExperienceInfoItemDetailsProvider.getInfoItemDetails(
				_group.getGroupId(), ERCInfoItemIdentifier.class,
				_segmentsExperience);

		Assert.assertEquals(
			SegmentsExperience.class.getName(), infoItemDetails.getClassName());
		Assert.assertEquals(
			new InfoItemReference(
				SegmentsExperience.class.getName(),
				new ERCInfoItemIdentifier(
					_segmentsExperience.getExternalReferenceCode(), null)),
			infoItemDetails.getInfoItemReference());

		infoItemDetails =
			_layoutSegmentsExperienceInfoItemDetailsProvider.getInfoItemDetails(
				RandomTestUtil.randomLong(), ERCInfoItemIdentifier.class,
				_segmentsExperience);

		Assert.assertEquals(
			SegmentsExperience.class.getName(), infoItemDetails.getClassName());
		Assert.assertEquals(
			new InfoItemReference(
				SegmentsExperience.class.getName(),
				new ERCInfoItemIdentifier(
					_segmentsExperience.getExternalReferenceCode(),
					_group.getExternalReferenceCode())),
			infoItemDetails.getInfoItemReference());
	}

	@DeleteAfterTestRun
	private Group _group;

	@Inject
	private GroupLocalService _groupLocalService;

	@Inject
	private InfoItemServiceRegistry _infoItemServiceRegistry;

	@Inject(
		filter = "component.name=com.liferay.layout.admin.web.internal.info.item.provider.LayoutSegmentsExperienceInfoItemDetailsProvider"
	)
	private InfoItemDetailsProvider<SegmentsExperience>
		_layoutSegmentsExperienceInfoItemDetailsProvider;

	private SegmentsExperience _segmentsExperience;

}