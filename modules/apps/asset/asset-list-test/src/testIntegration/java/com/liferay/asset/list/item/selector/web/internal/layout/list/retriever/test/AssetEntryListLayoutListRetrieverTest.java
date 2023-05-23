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

package com.liferay.asset.list.item.selector.web.internal.layout.list.retriever.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.service.AssetEntryLocalService;
import com.liferay.asset.list.constants.AssetListEntryTypeConstants;
import com.liferay.asset.list.model.AssetListEntry;
import com.liferay.asset.list.service.AssetListEntryLocalService;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.model.DDMTemplate;
import com.liferay.dynamic.data.mapping.test.util.DDMStructureTestUtil;
import com.liferay.dynamic.data.mapping.test.util.DDMTemplateTestUtil;
import com.liferay.info.filter.InfoFilter;
import com.liferay.info.pagination.Pagination;
import com.liferay.item.selector.criteria.InfoListItemSelectorReturnType;
import com.liferay.journal.constants.JournalArticleConstants;
import com.liferay.journal.constants.JournalFolderConstants;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.layout.list.retriever.ClassedModelListObjectReference;
import com.liferay.layout.list.retriever.LayoutListRetriever;
import com.liferay.layout.list.retriever.LayoutListRetrieverContext;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.rule.SynchronousDestinationTestRule;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.segments.constants.SegmentsEntryConstants;
import com.liferay.segments.criteria.Criteria;
import com.liferay.segments.criteria.CriteriaSerializer;
import com.liferay.segments.criteria.contributor.SegmentsCriteriaContributor;
import com.liferay.segments.model.SegmentsEntry;
import com.liferay.segments.model.SegmentsExperience;
import com.liferay.segments.service.SegmentsExperienceLocalService;
import com.liferay.segments.test.util.SegmentsTestUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

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
public class AssetEntryListLayoutListRetrieverTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE,
			SynchronousDestinationTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		_layout = LayoutTestUtil.addTypeContentLayout(_group);

		_serviceContext = ServiceContextTestUtil.getServiceContext(
			_group.getGroupId(), TestPropsValues.getUserId());
	}

	@Test
	public void testGetListCountOfCollectionVariationAssetEntriesSelectingSegmentsExperienceWithDifferentSegmentsEntry()
		throws Exception {

		DDMStructure ddmStructure = DDMStructureTestUtil.addStructure(
			_group.getGroupId(), JournalArticle.class.getName());

		JournalArticle expectedJournalArticle1 = _addJournalArticle(
			ddmStructure);

		AssetEntry assetEntry1 = _assetEntryLocalService.fetchEntry(
			JournalArticle.class.getName(),
			expectedJournalArticle1.getResourcePrimKey());

		JournalArticle expectedJournalArticle2 = _addJournalArticle(
			ddmStructure);

		AssetEntry assetEntry2 = _assetEntryLocalService.fetchEntry(
			JournalArticle.class.getName(),
			expectedJournalArticle2.getResourcePrimKey());

		JournalArticle expectedJournalArticle3 = _addJournalArticle(
			ddmStructure);

		AssetEntry assetEntry3 = _assetEntryLocalService.fetchEntry(
			JournalArticle.class.getName(),
			expectedJournalArticle3.getResourcePrimKey());

		SegmentsEntry segmentsEntry1 = _addSegmentsEntryByFirstName(
			_group.getGroupId(), "Test");

		AssetListEntry assetListEntry =
			_assetListEntryLocalService.addAssetListEntry(
				TestPropsValues.getUserId(), _group.getGroupId(),
				RandomTestUtil.randomString(),
				AssetListEntryTypeConstants.TYPE_MANUAL, _serviceContext);

		_assetListEntryLocalService.addAssetEntrySelections(
			assetListEntry.getAssetListEntryId(),
			new long[] {assetEntry1.getEntryId()},
			SegmentsEntryConstants.ID_DEFAULT, _serviceContext);

		_assetListEntryLocalService.addAssetEntrySelections(
			assetListEntry.getAssetListEntryId(),
			new long[] {assetEntry2.getEntryId(), assetEntry3.getEntryId()},
			segmentsEntry1.getSegmentsEntryId(), _serviceContext);

		SegmentsEntry segmentsEntry2 = _addSegmentsEntryByFirstName(
			_group.getGroupId(), "User");

		SegmentsExperience segmentsExperience =
			_segmentsExperienceLocalService.addSegmentsExperience(
				TestPropsValues.getUserId(), _group.getGroupId(),
				segmentsEntry2.getSegmentsEntryId(), _layout.getPlid(),
				RandomTestUtil.randomLocaleStringMap(), true,
				new UnicodeProperties(true),
				ServiceContextTestUtil.getServiceContext(_group.getGroupId()));

		Assert.assertEquals(
			1,
			_layoutListRetriever.getListCount(
				new ClassedModelListObjectReference(
					JSONUtil.put(
						"classPK", assetListEntry.getAssetListEntryId())),
				new MockLayoutListRetrieverContext(
					HashMapBuilder.<String, Object>put(
						"segmentsExperienceId",
						segmentsExperience.getSegmentsExperienceId()
					).build(),
					new long[] {
						SegmentsEntryConstants.ID_DEFAULT,
						segmentsEntry2.getSegmentsEntryId()
					})));
	}

	@Test
	public void testGetListCountOfCollectionVariationAssetEntriesSelectingSegmentsExperienceWithSameSegmentsEntry()
		throws Exception {

		DDMStructure ddmStructure = DDMStructureTestUtil.addStructure(
			_group.getGroupId(), JournalArticle.class.getName());

		JournalArticle expectedJournalArticle1 = _addJournalArticle(
			ddmStructure);

		AssetEntry assetEntry1 = _assetEntryLocalService.fetchEntry(
			JournalArticle.class.getName(),
			expectedJournalArticle1.getResourcePrimKey());

		JournalArticle expectedJournalArticle2 = _addJournalArticle(
			ddmStructure);

		AssetEntry assetEntry2 = _assetEntryLocalService.fetchEntry(
			JournalArticle.class.getName(),
			expectedJournalArticle2.getResourcePrimKey());

		JournalArticle expectedJournalArticle3 = _addJournalArticle(
			ddmStructure);

		AssetEntry assetEntry3 = _assetEntryLocalService.fetchEntry(
			JournalArticle.class.getName(),
			expectedJournalArticle3.getResourcePrimKey());

		SegmentsEntry segmentsEntry = _addSegmentsEntryByFirstName(
			_group.getGroupId(), "Test");

		AssetListEntry assetListEntry =
			_assetListEntryLocalService.addAssetListEntry(
				TestPropsValues.getUserId(), _group.getGroupId(),
				RandomTestUtil.randomString(),
				AssetListEntryTypeConstants.TYPE_MANUAL, _serviceContext);

		_assetListEntryLocalService.addAssetEntrySelections(
			assetListEntry.getAssetListEntryId(),
			new long[] {assetEntry1.getEntryId()},
			SegmentsEntryConstants.ID_DEFAULT, _serviceContext);

		_assetListEntryLocalService.addAssetEntrySelections(
			assetListEntry.getAssetListEntryId(),
			new long[] {assetEntry2.getEntryId(), assetEntry3.getEntryId()},
			segmentsEntry.getSegmentsEntryId(), _serviceContext);

		SegmentsExperience segmentsExperience =
			_segmentsExperienceLocalService.addSegmentsExperience(
				TestPropsValues.getUserId(), _group.getGroupId(),
				segmentsEntry.getSegmentsEntryId(), _layout.getPlid(),
				RandomTestUtil.randomLocaleStringMap(), true,
				new UnicodeProperties(true),
				ServiceContextTestUtil.getServiceContext(_group.getGroupId()));

		if (FeatureFlagManagerUtil.isEnabled("LPS-183723")) {
			Assert.assertEquals(
				2,
				_layoutListRetriever.getListCount(
					new ClassedModelListObjectReference(
						JSONUtil.put(
							"classPK", assetListEntry.getAssetListEntryId())),
					new MockLayoutListRetrieverContext(
						HashMapBuilder.<String, Object>put(
							"segmentsExperienceId",
							segmentsExperience.getSegmentsExperienceId()
						).build(),
						new long[] {
							SegmentsEntryConstants.ID_DEFAULT,
							segmentsEntry.getSegmentsEntryId()
						})));
		}
		else {
			Assert.assertEquals(
				1,
				_layoutListRetriever.getListCount(
					new ClassedModelListObjectReference(
						JSONUtil.put(
							"classPK", assetListEntry.getAssetListEntryId())),
					new MockLayoutListRetrieverContext(
						HashMapBuilder.<String, Object>put(
							"segmentsExperienceId",
							segmentsExperience.getSegmentsExperienceId()
						).build(),
						new long[] {
							SegmentsEntryConstants.ID_DEFAULT,
							segmentsEntry.getSegmentsEntryId()
						})));
		}
	}

	@Test
	public void testGetListCountOfCollectionVariationAssetEntriesWithoutSelectingSegmentsExperience()
		throws Exception {

		DDMStructure ddmStructure = DDMStructureTestUtil.addStructure(
			_group.getGroupId(), JournalArticle.class.getName());

		JournalArticle expectedJournalArticle1 = _addJournalArticle(
			ddmStructure);

		AssetEntry assetEntry1 = _assetEntryLocalService.fetchEntry(
			JournalArticle.class.getName(),
			expectedJournalArticle1.getResourcePrimKey());

		JournalArticle expectedJournalArticle2 = _addJournalArticle(
			ddmStructure);

		AssetEntry assetEntry2 = _assetEntryLocalService.fetchEntry(
			JournalArticle.class.getName(),
			expectedJournalArticle2.getResourcePrimKey());

		JournalArticle expectedJournalArticle3 = _addJournalArticle(
			ddmStructure);

		AssetEntry assetEntry3 = _assetEntryLocalService.fetchEntry(
			JournalArticle.class.getName(),
			expectedJournalArticle3.getResourcePrimKey());

		SegmentsEntry segmentsEntry = _addSegmentsEntryByFirstName(
			_group.getGroupId(), "Test");

		AssetListEntry assetListEntry =
			_assetListEntryLocalService.addAssetListEntry(
				TestPropsValues.getUserId(), _group.getGroupId(),
				RandomTestUtil.randomString(),
				AssetListEntryTypeConstants.TYPE_MANUAL, _serviceContext);

		_assetListEntryLocalService.addAssetEntrySelections(
			assetListEntry.getAssetListEntryId(),
			new long[] {assetEntry1.getEntryId()},
			SegmentsEntryConstants.ID_DEFAULT, _serviceContext);

		_assetListEntryLocalService.addAssetEntrySelections(
			assetListEntry.getAssetListEntryId(),
			new long[] {assetEntry2.getEntryId(), assetEntry3.getEntryId()},
			segmentsEntry.getSegmentsEntryId(), _serviceContext);

		Assert.assertEquals(
			1,
			_layoutListRetriever.getListCount(
				new ClassedModelListObjectReference(
					JSONUtil.put(
						"classPK", assetListEntry.getAssetListEntryId())),
				new MockLayoutListRetrieverContext(
					null,
					new long[] {
						SegmentsEntryConstants.ID_DEFAULT,
						segmentsEntry.getSegmentsEntryId()
					})));
	}

	@Test
	public void testGetListOfCollectionVariationAssetEntriesSelectingSegmentsExperienceWithDifferentSegmentsEntry()
		throws Exception {

		DDMStructure ddmStructure = DDMStructureTestUtil.addStructure(
			_group.getGroupId(), JournalArticle.class.getName());

		JournalArticle expectedJournalArticle1 = _addJournalArticle(
			ddmStructure);

		AssetEntry assetEntry1 = _assetEntryLocalService.fetchEntry(
			JournalArticle.class.getName(),
			expectedJournalArticle1.getResourcePrimKey());

		JournalArticle expectedJournalArticle2 = _addJournalArticle(
			ddmStructure);

		AssetEntry assetEntry2 = _assetEntryLocalService.fetchEntry(
			JournalArticle.class.getName(),
			expectedJournalArticle2.getResourcePrimKey());

		SegmentsEntry segmentsEntry1 = _addSegmentsEntryByFirstName(
			_group.getGroupId(), "Test");

		AssetListEntry assetListEntry =
			_assetListEntryLocalService.addAssetListEntry(
				TestPropsValues.getUserId(), _group.getGroupId(),
				RandomTestUtil.randomString(),
				AssetListEntryTypeConstants.TYPE_MANUAL, _serviceContext);

		_assetListEntryLocalService.addAssetEntrySelections(
			assetListEntry.getAssetListEntryId(),
			new long[] {assetEntry1.getEntryId()},
			SegmentsEntryConstants.ID_DEFAULT, _serviceContext);

		_assetListEntryLocalService.addAssetEntrySelections(
			assetListEntry.getAssetListEntryId(),
			new long[] {assetEntry2.getEntryId()},
			segmentsEntry1.getSegmentsEntryId(), _serviceContext);

		SegmentsEntry segmentsEntry2 = _addSegmentsEntryByFirstName(
			_group.getGroupId(), "User");

		SegmentsExperience segmentsExperience =
			_segmentsExperienceLocalService.addSegmentsExperience(
				TestPropsValues.getUserId(), _group.getGroupId(),
				segmentsEntry2.getSegmentsEntryId(), _layout.getPlid(),
				RandomTestUtil.randomLocaleStringMap(), true,
				new UnicodeProperties(true),
				ServiceContextTestUtil.getServiceContext(_group.getGroupId()));

		List<Object> actualJournalArticles = _layoutListRetriever.getList(
			new ClassedModelListObjectReference(
				JSONUtil.put("classPK", assetListEntry.getAssetListEntryId())),
			new MockLayoutListRetrieverContext(
				HashMapBuilder.<String, Object>put(
					"segmentsExperienceId",
					segmentsExperience.getSegmentsExperienceId()
				).build(),
				new long[] {
					SegmentsEntryConstants.ID_DEFAULT,
					segmentsEntry2.getSegmentsEntryId()
				}));

		Assert.assertEquals(
			actualJournalArticles.toString(), 1, actualJournalArticles.size());

		JournalArticle actualJournalArticle1 =
			(JournalArticle)actualJournalArticles.get(0);

		Assert.assertEquals(
			expectedJournalArticle1.getArticleId(),
			actualJournalArticle1.getArticleId());
	}

	@Test
	public void testGetListOfCollectionVariationAssetEntriesSelectingSegmentsExperienceWithSameSegmentsEntry()
		throws Exception {

		DDMStructure ddmStructure = DDMStructureTestUtil.addStructure(
			_group.getGroupId(), JournalArticle.class.getName());

		JournalArticle expectedJournalArticle1 = _addJournalArticle(
			ddmStructure);

		AssetEntry assetEntry1 = _assetEntryLocalService.fetchEntry(
			JournalArticle.class.getName(),
			expectedJournalArticle1.getResourcePrimKey());

		JournalArticle expectedJournalArticle2 = _addJournalArticle(
			ddmStructure);

		AssetEntry assetEntry2 = _assetEntryLocalService.fetchEntry(
			JournalArticle.class.getName(),
			expectedJournalArticle2.getResourcePrimKey());

		SegmentsEntry segmentsEntry = _addSegmentsEntryByFirstName(
			_group.getGroupId(), "Test");

		AssetListEntry assetListEntry =
			_assetListEntryLocalService.addAssetListEntry(
				TestPropsValues.getUserId(), _group.getGroupId(),
				RandomTestUtil.randomString(),
				AssetListEntryTypeConstants.TYPE_MANUAL, _serviceContext);

		_assetListEntryLocalService.addAssetEntrySelections(
			assetListEntry.getAssetListEntryId(),
			new long[] {assetEntry1.getEntryId()},
			SegmentsEntryConstants.ID_DEFAULT, _serviceContext);

		_assetListEntryLocalService.addAssetEntrySelections(
			assetListEntry.getAssetListEntryId(),
			new long[] {assetEntry2.getEntryId()},
			segmentsEntry.getSegmentsEntryId(), _serviceContext);

		SegmentsExperience segmentsExperience =
			_segmentsExperienceLocalService.addSegmentsExperience(
				TestPropsValues.getUserId(), _group.getGroupId(),
				segmentsEntry.getSegmentsEntryId(), _layout.getPlid(),
				RandomTestUtil.randomLocaleStringMap(), true,
				new UnicodeProperties(true),
				ServiceContextTestUtil.getServiceContext(_group.getGroupId()));

		List<Object> actualJournalArticles = _layoutListRetriever.getList(
			new ClassedModelListObjectReference(
				JSONUtil.put("classPK", assetListEntry.getAssetListEntryId())),
			new MockLayoutListRetrieverContext(
				HashMapBuilder.<String, Object>put(
					"segmentsExperienceId",
					segmentsExperience.getSegmentsExperienceId()
				).build(),
				new long[] {
					SegmentsEntryConstants.ID_DEFAULT,
					segmentsEntry.getSegmentsEntryId()
				}));

		Assert.assertEquals(
			actualJournalArticles.toString(), 1, actualJournalArticles.size());

		JournalArticle actualJournalArticle =
			(JournalArticle)actualJournalArticles.get(0);

		if (FeatureFlagManagerUtil.isEnabled("LPS-183723")) {
			Assert.assertEquals(
				expectedJournalArticle2.getArticleId(),
				actualJournalArticle.getArticleId());
		}
		else {
			Assert.assertEquals(
				expectedJournalArticle1.getArticleId(),
				actualJournalArticle.getArticleId());
		}
	}

	@Test
	public void testGetListOfCollectionVariationAssetEntriesWithoutSelectingSegmentsExperience()
		throws Exception {

		DDMStructure ddmStructure = DDMStructureTestUtil.addStructure(
			_group.getGroupId(), JournalArticle.class.getName());

		JournalArticle expectedJournalArticle1 = _addJournalArticle(
			ddmStructure);

		AssetEntry assetEntry1 = _assetEntryLocalService.fetchEntry(
			JournalArticle.class.getName(),
			expectedJournalArticle1.getResourcePrimKey());

		JournalArticle expectedJournalArticle2 = _addJournalArticle(
			ddmStructure);

		AssetEntry assetEntry2 = _assetEntryLocalService.fetchEntry(
			JournalArticle.class.getName(),
			expectedJournalArticle2.getResourcePrimKey());

		SegmentsEntry segmentsEntry = _addSegmentsEntryByFirstName(
			_group.getGroupId(), "Test");

		AssetListEntry assetListEntry =
			_assetListEntryLocalService.addAssetListEntry(
				TestPropsValues.getUserId(), _group.getGroupId(),
				RandomTestUtil.randomString(),
				AssetListEntryTypeConstants.TYPE_MANUAL, _serviceContext);

		_assetListEntryLocalService.addAssetEntrySelections(
			assetListEntry.getAssetListEntryId(),
			new long[] {assetEntry1.getEntryId()},
			SegmentsEntryConstants.ID_DEFAULT, _serviceContext);

		_assetListEntryLocalService.addAssetEntrySelections(
			assetListEntry.getAssetListEntryId(),
			new long[] {assetEntry2.getEntryId()},
			segmentsEntry.getSegmentsEntryId(), _serviceContext);

		List<Object> actualJournalArticles = _layoutListRetriever.getList(
			new ClassedModelListObjectReference(
				JSONUtil.put("classPK", assetListEntry.getAssetListEntryId())),
			new MockLayoutListRetrieverContext(
				null,
				new long[] {
					SegmentsEntryConstants.ID_DEFAULT,
					segmentsEntry.getSegmentsEntryId()
				}));

		Assert.assertEquals(
			actualJournalArticles.toString(), 1, actualJournalArticles.size());

		JournalArticle actualJournalArticle =
			(JournalArticle)actualJournalArticles.get(0);

		Assert.assertEquals(
			expectedJournalArticle1.getArticleId(),
			actualJournalArticle.getArticleId());
	}

	private JournalArticle _addJournalArticle(DDMStructure ddmStructure)
		throws Exception {

		String content = DDMStructureTestUtil.getSampleStructuredContent();

		DDMTemplate ddmTemplate = DDMTemplateTestUtil.addTemplate(
			_group.getGroupId(), ddmStructure.getStructureId(),
			PortalUtil.getClassNameId(JournalArticle.class));

		Calendar displayDateCalendar = new GregorianCalendar();

		displayDateCalendar.setTime(new Date());

		return _journalArticleLocalService.addArticle(
			null, TestPropsValues.getUserId(), _group.getGroupId(),
			JournalFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			JournalArticleConstants.CLASS_NAME_ID_DEFAULT, 0, StringPool.BLANK,
			true, JournalArticleConstants.VERSION_DEFAULT,
			RandomTestUtil.randomLocaleStringMap(),
			RandomTestUtil.randomLocaleStringMap(),
			RandomTestUtil.randomLocaleStringMap(), content,
			ddmStructure.getStructureId(), ddmTemplate.getTemplateKey(), null,
			displayDateCalendar.get(Calendar.MONTH),
			displayDateCalendar.get(Calendar.DAY_OF_MONTH),
			displayDateCalendar.get(Calendar.YEAR),
			displayDateCalendar.get(Calendar.HOUR_OF_DAY),
			displayDateCalendar.get(Calendar.MINUTE), 0, 0, 0, 0, 0, true, 0, 0,
			0, 0, 0, true, true, false, null, null, null, null,
			ServiceContextTestUtil.getServiceContext(_group.getGroupId()));
	}

	private SegmentsEntry _addSegmentsEntryByFirstName(
			long groupId, String firstName)
		throws Exception {

		Criteria criteria = new Criteria();

		_segmentsCriteriaContributor.contribute(
			criteria, String.format("(firstName eq '%s')", firstName),
			Criteria.Conjunction.AND);

		return SegmentsTestUtil.addSegmentsEntry(
			groupId, CriteriaSerializer.serialize(criteria),
			User.class.getName());
	}

	@Inject
	private AssetEntryLocalService _assetEntryLocalService;

	@Inject
	private AssetListEntryLocalService _assetListEntryLocalService;

	@DeleteAfterTestRun
	private Group _group;

	@Inject
	private JournalArticleLocalService _journalArticleLocalService;

	private Layout _layout;

	@Inject(
		filter = "component.name=com.liferay.asset.list.item.selector.web.internal.layout.list.retriever.AssetEntryListLayoutListRetriever"
	)
	private LayoutListRetriever
		<InfoListItemSelectorReturnType, ClassedModelListObjectReference>
			_layoutListRetriever;

	@Inject(
		filter = "segments.criteria.contributor.key=user",
		type = SegmentsCriteriaContributor.class
	)
	private SegmentsCriteriaContributor _segmentsCriteriaContributor;

	@Inject
	private SegmentsExperienceLocalService _segmentsExperienceLocalService;

	private ServiceContext _serviceContext;

	private class MockLayoutListRetrieverContext
		implements LayoutListRetrieverContext {

		public MockLayoutListRetrieverContext(
			Map<String, Object> contextData, long[] segmentsEntryIds) {

			_contextData = contextData;
			_segmentsEntryIds = segmentsEntryIds;
		}

		@Override
		public Map<String, String[]> getConfiguration() {
			return null;
		}

		@Override
		public Map<String, Object> getContextData() {
			return _contextData;
		}

		@Override
		public Object getContextObject() {
			return null;
		}

		@Override
		public <T> T getInfoFilter(Class<? extends InfoFilter> clazz) {
			return null;
		}

		@Override
		public Map<String, InfoFilter> getInfoFilters() {
			return null;
		}

		@Override
		public Pagination getPagination() {
			return null;
		}

		@Override
		public long[] getSegmentsEntryIds() {
			return _segmentsEntryIds;
		}

		private final Map<String, Object> _contextData;
		private final long[] _segmentsEntryIds;

	}

}