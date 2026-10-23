/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.cms.internal.usage;

import com.liferay.analytics.machine.learning.content.MostViewedContentRecommendation;
import com.liferay.analytics.machine.learning.content.MostViewedContentRecommendationManager;
import com.liferay.asset.kernel.configuration.provider.AssetCategoryConfigurationProviderUtil;
import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.service.AssetCategoryLocalService;
import com.liferay.asset.kernel.service.AssetEntryLocalService;
import com.liferay.asset.kernel.service.persistence.AssetEntryQuery;
import com.liferay.asset.list.asset.entry.provider.AssetListAssetEntryProvider;
import com.liferay.asset.list.constants.AssetListEntryTypeConstants;
import com.liferay.asset.list.model.AssetListEntry;
import com.liferay.asset.list.model.AssetListEntryAssetEntryRel;
import com.liferay.asset.list.model.AssetListEntrySegmentsEntryRel;
import com.liferay.asset.list.model.AssetListEntryUsageTable;
import com.liferay.asset.list.service.AssetListEntryAssetEntryRelLocalService;
import com.liferay.asset.list.service.AssetListEntryLocalService;
import com.liferay.asset.list.service.AssetListEntrySegmentsEntryRelLocalService;
import com.liferay.asset.list.service.AssetListEntryUsageLocalService;
import com.liferay.depot.constants.DepotConstants;
import com.liferay.depot.model.DepotEntry;
import com.liferay.depot.service.DepotEntryLocalService;
import com.liferay.headless.cms.internal.util.CMSSearchUtil;
import com.liferay.info.collection.provider.InfoCollectionProvider;
import com.liferay.info.collection.provider.RelatedInfoItemCollectionProvider;
import com.liferay.info.item.InfoItemServiceRegistry;
import com.liferay.layout.page.template.model.LayoutPageTemplateEntry;
import com.liferay.layout.page.template.model.LayoutPageTemplateStructure;
import com.liferay.layout.page.template.service.LayoutPageTemplateEntryLocalService;
import com.liferay.layout.page.template.service.LayoutPageTemplateStructureLocalService;
import com.liferay.layout.util.constants.LayoutDataItemTypeConstants;
import com.liferay.layout.util.structure.CollectionStyledLayoutStructureItem;
import com.liferay.layout.util.structure.LayoutStructure;
import com.liferay.layout.util.structure.LayoutStructureItem;
import com.liferay.object.constants.ObjectDefinitionConstants;
import com.liferay.object.constants.ObjectFieldConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectField;
import com.liferay.object.model.ObjectRelationship;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.object.service.ObjectRelationshipLocalService;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.sql.dsl.DSLQueryFactoryUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutConstants;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.search.BooleanClause;
import com.liferay.portal.kernel.search.BooleanClauseOccur;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.search.aggregation.Aggregations;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.QueriesUtil;
import com.liferay.portal.search.query.Query;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.searcher.Searcher;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author Mikel Lorza
 */
public class CollectionUsageResolver {

	public CollectionUsageResolver(
		Aggregations aggregations,
		AssetCategoryLocalService assetCategoryLocalService,
		AssetEntryLocalService assetEntryLocalService,
		AssetListAssetEntryProvider assetListAssetEntryProvider,
		AssetListEntryAssetEntryRelLocalService
			assetListEntryAssetEntryRelLocalService,
		AssetListEntryLocalService assetListEntryLocalService,
		AssetListEntrySegmentsEntryRelLocalService
			assetListEntrySegmentsEntryRelLocalService,
		AssetListEntryUsageLocalService assetListEntryUsageLocalService,
		DepotEntryLocalService depotEntryLocalService,
		InfoItemServiceRegistry infoItemServiceRegistry,
		LayoutLocalService layoutLocalService,
		LayoutPageTemplateEntryLocalService layoutPageTemplateEntryLocalService,
		LayoutPageTemplateStructureLocalService
			layoutPageTemplateStructureLocalService,
		ObjectDefinitionLocalService objectDefinitionLocalService,
		ObjectEntryLocalService objectEntryLocalService,
		ObjectFieldLocalService objectFieldLocalService,
		ObjectRelationshipLocalService objectRelationshipLocalService,
		Portal portal, Searcher searcher,
		SearchRequestBuilderFactory searchRequestBuilderFactory) {

		_aggregations = aggregations;
		_assetCategoryLocalService = assetCategoryLocalService;
		_assetEntryLocalService = assetEntryLocalService;
		_assetListAssetEntryProvider = assetListAssetEntryProvider;
		_assetListEntryAssetEntryRelLocalService =
			assetListEntryAssetEntryRelLocalService;
		_assetListEntryLocalService = assetListEntryLocalService;
		_assetListEntrySegmentsEntryRelLocalService =
			assetListEntrySegmentsEntryRelLocalService;
		_assetListEntryUsageLocalService = assetListEntryUsageLocalService;
		_depotEntryLocalService = depotEntryLocalService;
		_infoItemServiceRegistry = infoItemServiceRegistry;
		_layoutLocalService = layoutLocalService;
		_layoutPageTemplateEntryLocalService =
			layoutPageTemplateEntryLocalService;
		_layoutPageTemplateStructureLocalService =
			layoutPageTemplateStructureLocalService;
		_objectDefinitionLocalService = objectDefinitionLocalService;
		_objectEntryLocalService = objectEntryLocalService;
		_objectFieldLocalService = objectFieldLocalService;
		_objectRelationshipLocalService = objectRelationshipLocalService;
		_portal = portal;
		_searcher = searcher;
		_searchRequestBuilderFactory = searchRequestBuilderFactory;
	}

	public CollectionUsages getCollectionUsages(long companyId) {
		CollectionUsages collectionUsages = new CollectionUsages();

		ResolutionContext resolutionContext = new ResolutionContext(
			collectionUsages, companyId);

		for (Object[] row : _getUsageRows(companyId)) {
			long groupId = (Long)row[0];
			long plid = (Long)row[1];
			long classNameId = (Long)row[2];
			String key = (String)row[3];
			String containerKey = (String)row[4];

			try {
				Layout layout = resolutionContext.getLayout(plid);

				if ((layout == null) || !_isPublished(layout)) {
					continue;
				}

				Set<Long> connectedDepotGroupIds =
					resolutionContext.getConnectedDepotGroupIds(groupId);

				if (connectedDepotGroupIds.isEmpty()) {
					continue;
				}

				_resolveUsage(
					classNameId, containerKey, groupId, key, layout,
					resolutionContext);
			}
			catch (Exception exception) {
				if (_log.isDebugEnabled()) {
					_log.debug(exception);
				}
			}
		}

		return collectionUsages;
	}

	private void _addAssetCategoriesQuery(
			Set<Long> assetCategoryIds, long groupId,
			ResolutionContext resolutionContext)
		throws Exception {

		if (assetCategoryIds.isEmpty()) {
			return;
		}

		Set<Long> searchAssetCategoryIds = new HashSet<>();

		for (long assetCategoryId : assetCategoryIds) {
			List<Long> subcategoryIds = Collections.emptyList();

			if (AssetCategoryConfigurationProviderUtil.isSearchHierarchical(
					resolutionContext.getCompanyId())) {

				subcategoryIds = _assetCategoryLocalService.getSubcategoryIds(
					assetCategoryId);
			}

			if (subcategoryIds.isEmpty()) {
				searchAssetCategoryIds.add(assetCategoryId);
			}
			else {
				searchAssetCategoryIds.addAll(subcategoryIds);
			}
		}

		Set<Long> groupIds = new HashSet<>(
			resolutionContext.getConnectedDepotGroupIds(groupId));

		groupIds.add(groupId);

		BooleanQuery booleanQuery = QueriesUtil.booleanQuery();

		booleanQuery.addFilterQueryClauses(
			CMSSearchUtil.getTermsQuery(
				Field.ASSET_CATEGORY_IDS,
				ArrayUtil.toStringArray(searchAssetCategoryIds)),
			CMSSearchUtil.getTermsQuery(
				Field.GROUP_ID, ArrayUtil.toStringArray(groupIds)));

		CollectionUsages collectionUsages =
			resolutionContext.getCollectionUsages();

		collectionUsages.addQuery(booleanQuery);
	}

	private void _addBucketKeys(
		Set<String> bucketKeys, String fieldName,
		Supplier<SearchRequestBuilder> searchRequestBuilderSupplier) {

		CMSSearchUtil.addBucketKeys(
			includeExcludeClause -> CMSSearchUtil.getTermsAggregationResult(
				_aggregations, fieldName, includeExcludeClause, _searcher,
				searchRequestBuilderSupplier.get(), 10000),
			bucketKeys, StringPool.BLANK);
	}

	private void _addObjectRelationshipQuery(
			String key,
			Supplier<SearchRequestBuilder> outerSearchRequestBuilderSupplier,
			ResolutionContext resolutionContext)
		throws Exception {

		String[] parts = StringUtil.split(key, StringPool.UNDERLINE);

		ObjectDefinition objectDefinition1 =
			_objectDefinitionLocalService.fetchObjectDefinitionByClassName(
				resolutionContext.getCompanyId(), parts[parts.length - 2]);

		if (objectDefinition1 == null) {
			return;
		}

		ObjectRelationship objectRelationship =
			_objectRelationshipLocalService.
				fetchObjectRelationshipByObjectDefinitionId1(
					objectDefinition1.getObjectDefinitionId(),
					parts[parts.length - 1]);

		if (objectRelationship == null) {
			return;
		}

		ObjectField objectField = _objectFieldLocalService.getObjectField(
			objectRelationship.getObjectFieldId2());

		Set<String> outerEntryClassPKs = CMSSearchUtil.getEntryClassPKs(
			_aggregations, _searcher, outerSearchRequestBuilderSupplier);

		BooleanQuery nestedBooleanQuery = QueriesUtil.booleanQuery();

		nestedBooleanQuery.addFilterQueryClauses(
			QueriesUtil.term(
				"nestedFieldArray.fieldName", objectField.getName()),
			CMSSearchUtil.getTermsQuery(
				"nestedFieldArray.value_long",
				outerEntryClassPKs.toArray(new String[0])));

		ObjectDefinition objectDefinition2 =
			_objectDefinitionLocalService.getObjectDefinition(
				objectRelationship.getObjectDefinitionId2());

		BooleanQuery booleanQuery = QueriesUtil.booleanQuery();

		booleanQuery.addFilterQueryClauses(
			QueriesUtil.term(
				Field.ENTRY_CLASS_NAME, objectDefinition2.getClassName()),
			QueriesUtil.nested("nestedFieldArray", nestedBooleanQuery));

		CollectionUsages collectionUsages =
			resolutionContext.getCollectionUsages();

		collectionUsages.addQuery(booleanQuery);
	}

	private Set<Long> _getAssetCategoryIds(
		Supplier<SearchRequestBuilder> searchRequestBuilderSupplier) {

		Set<String> bucketKeys = new HashSet<>();

		_addBucketKeys(
			bucketKeys, Field.ASSET_CATEGORY_IDS, searchRequestBuilderSupplier);
		_addBucketKeys(
			bucketKeys, Field.ASSET_INTERNAL_CATEGORY_IDS,
			searchRequestBuilderSupplier);

		return new HashSet<>(
			TransformUtil.transform(bucketKeys, Long::valueOf));
	}

	private Set<Long> _getAssetListEntryObjectEntryIds(
		AssetListEntry assetListEntry) {

		return _getObjectEntryIds(
			TransformUtil.transform(
				_assetListEntryAssetEntryRelLocalService.
					getAssetListEntryAssetEntryRels(
						assetListEntry.getAssetListEntryId(), QueryUtil.ALL_POS,
						QueryUtil.ALL_POS),
				AssetListEntryAssetEntryRel::getAssetEntryId));
	}

	private BooleanClause<com.liferay.portal.kernel.search.Query>[]
			_getBooleanClauses(AssetListEntry assetListEntry)
		throws Exception {

		com.liferay.portal.kernel.search.BooleanQuery booleanQuery =
			new com.liferay.portal.kernel.search.BooleanQuery();

		for (long segmentsEntryId : _getSegmentsEntryIds(assetListEntry)) {
			com.liferay.portal.kernel.search.BooleanQuery
				segmentsEntryBooleanQuery =
					_assetListAssetEntryProvider.getBooleanQuery(
						assetListEntry, new long[] {segmentsEntryId},
						StringPool.BLANK);

			if (segmentsEntryBooleanQuery == null) {
				continue;
			}

			booleanQuery.add(
				segmentsEntryBooleanQuery, BooleanClauseOccur.SHOULD);
		}

		if (!booleanQuery.hasClauses()) {
			return null;
		}

		return new BooleanClause[] {
			new BooleanClause<>(booleanQuery, BooleanClauseOccur.MUST)
		};
	}

	private ObjectDefinition _getCMSObjectDefinition(
		long companyId, String className) {

		if (Validator.isNull(className) ||
			!className.startsWith(
				ObjectDefinitionConstants.
					CLASS_NAME_PREFIX_CUSTOM_OBJECT_DEFINITION)) {

			return null;
		}

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.fetchObjectDefinitionByClassName(
				companyId, className);

		if ((objectDefinition == null) || !objectDefinition.isCMS()) {
			return null;
		}

		return objectDefinition;
	}

	private Supplier<SearchRequestBuilder>
			_getCollectionSearchRequestBuilderSupplier(
				JSONObject collectionJSONObject, long groupId,
				ResolutionContext resolutionContext)
		throws Exception {

		long companyId = resolutionContext.getCompanyId();

		long classPK = collectionJSONObject.getLong("classPK");

		if (classPK <= 0) {
			ObjectDefinition objectDefinition = _getCMSObjectDefinition(
				companyId, collectionJSONObject.getString("key"));

			if (objectDefinition == null) {
				return null;
			}

			Query query = _getInfoCollectionProviderQuery(
				collectionJSONObject.getJSONObject("config"), groupId,
				objectDefinition, resolutionContext);

			if (query == null) {
				return null;
			}

			return () -> _getSearchRequestBuilder(null, companyId, query);
		}

		AssetListEntry assetListEntry =
			_assetListEntryLocalService.fetchAssetListEntry(classPK);

		if (assetListEntry == null) {
			return null;
		}

		if (assetListEntry.getType() ==
				AssetListEntryTypeConstants.TYPE_MANUAL) {

			Query query = CMSSearchUtil.getTermsQuery(
				Field.ENTRY_CLASS_PK,
				ArrayUtil.toStringArray(
					_getAssetListEntryObjectEntryIds(assetListEntry)));

			return () -> _getSearchRequestBuilder(null, companyId, query);
		}

		BooleanClause<com.liferay.portal.kernel.search.Query>[] booleanClauses =
			_getBooleanClauses(assetListEntry);

		if (booleanClauses == null) {
			return null;
		}

		Query query = QueriesUtil.prefix(
			Field.ENTRY_CLASS_NAME,
			ObjectDefinitionConstants.
				CLASS_NAME_PREFIX_CUSTOM_OBJECT_DEFINITION);

		return () -> _getSearchRequestBuilder(booleanClauses, companyId, query);
	}

	private CollectionStyledLayoutStructureItem
		_getCollectionStyledLayoutStructureItem(
			LayoutStructure layoutStructure, String itemId) {

		LayoutStructureItem layoutStructureItem =
			layoutStructure.getLayoutStructureItem(itemId);

		if (layoutStructureItem instanceof
				CollectionStyledLayoutStructureItem) {

			return (CollectionStyledLayoutStructureItem)layoutStructureItem;
		}

		return null;
	}

	private Query _getInfoCollectionProviderQuery(
			JSONObject configJSONObject, long groupId,
			ObjectDefinition objectDefinition,
			ResolutionContext resolutionContext)
		throws Exception {

		BooleanQuery booleanQuery = (BooleanQuery)_getObjectDefinitionQuery(
			objectDefinition.getClassName(),
			resolutionContext.getConnectedDepotGroupIds(groupId));

		if (configJSONObject == null) {
			return booleanQuery;
		}

		for (String fieldName : configJSONObject.keySet()) {
			String fieldValue = configJSONObject.getString(fieldName);

			JSONArray fieldValuesJSONArray = configJSONObject.getJSONArray(
				fieldName);

			if (fieldValuesJSONArray != null) {
				fieldValue = fieldValuesJSONArray.getString(0);
			}

			if (Validator.isNull(fieldValue)) {
				continue;
			}

			ObjectField objectField = _objectFieldLocalService.fetchObjectField(
				objectDefinition.getObjectDefinitionId(), fieldName);

			if (objectField == null) {
				continue;
			}

			Query query = null;

			if (Objects.equals(
					objectField.getBusinessType(),
					ObjectFieldConstants.BUSINESS_TYPE_BOOLEAN)) {

				query = _getNestedFieldQuery(
					fieldName, "value_boolean",
					GetterUtil.getBoolean(fieldValue));
			}
			else if (Objects.equals(
						objectField.getBusinessType(),
						ObjectFieldConstants.BUSINESS_TYPE_PICKLIST)) {

				if (objectField.isIndexedAsKeyword()) {
					query = _getNestedFieldQuery(
						fieldName, "value_keyword",
						StringUtil.lowerCase(fieldValue));
				}
				else {
					query = _getNestedFieldQuery(
						fieldName, "value_keyword_lowercase", fieldValue);
				}
			}

			if (query == null) {
				return null;
			}

			booleanQuery.addFilterQueryClauses(query);
		}

		return booleanQuery;
	}

	private Query _getNestedFieldQuery(
		String fieldName, String valueFieldName, Object value) {

		BooleanQuery booleanQuery = QueriesUtil.booleanQuery();

		booleanQuery.addFilterQueryClauses(
			QueriesUtil.term("nestedFieldArray.fieldName", fieldName),
			QueriesUtil.term("nestedFieldArray." + valueFieldName, value));

		return QueriesUtil.nested("nestedFieldArray", booleanQuery);
	}

	private Query _getObjectDefinitionQuery(
		String className, Collection<Long> groupIds) {

		BooleanQuery booleanQuery = QueriesUtil.booleanQuery();

		booleanQuery.addFilterQueryClauses(
			QueriesUtil.term(Field.ENTRY_CLASS_NAME, className),
			CMSSearchUtil.getTermsQuery(
				Field.GROUP_ID, ArrayUtil.toStringArray(groupIds)),
			QueriesUtil.term(Field.STATUS, WorkflowConstants.STATUS_APPROVED));

		return booleanQuery;
	}

	private Set<Long> _getObjectEntryIds(List<Long> assetEntryIds) {
		Set<Long> objectEntryIds = new HashSet<>();

		for (long assetEntryId : assetEntryIds) {
			AssetEntry assetEntry = _assetEntryLocalService.fetchEntry(
				assetEntryId);

			if ((assetEntry == null) ||
				!StringUtil.startsWith(
					assetEntry.getClassName(),
					ObjectDefinitionConstants.
						CLASS_NAME_PREFIX_CUSTOM_OBJECT_DEFINITION)) {

				continue;
			}

			objectEntryIds.add(assetEntry.getClassPK());
		}

		return objectEntryIds;
	}

	private Supplier<SearchRequestBuilder>
			_getOuterSearchRequestBuilderSupplier(
				String containerKey, long groupId, String key, Layout layout,
				ResolutionContext resolutionContext)
		throws Exception {

		LayoutStructure layoutStructure = resolutionContext.getLayoutStructure(
			groupId, layout.getPlid());

		if (layoutStructure == null) {
			return null;
		}

		CollectionStyledLayoutStructureItem
			collectionStyledLayoutStructureItem =
				_getCollectionStyledLayoutStructureItem(
					layoutStructure, containerKey);

		if (collectionStyledLayoutStructureItem == null) {
			return null;
		}

		Set<String> resolutionKeys = resolutionContext.getResolutionKeys();

		LayoutStructureItem parentLayoutStructureItem =
			layoutStructure.getLayoutStructureItem(
				collectionStyledLayoutStructureItem.getParentItemId());

		while (parentLayoutStructureItem != null) {
			if (Objects.equals(
					parentLayoutStructureItem.getItemType(),
					LayoutDataItemTypeConstants.TYPE_COLLECTION_ITEM)) {

				CollectionStyledLayoutStructureItem
					outerCollectionStyledLayoutStructureItem =
						_getCollectionStyledLayoutStructureItem(
							layoutStructure,
							parentLayoutStructureItem.getParentItemId());

				if (outerCollectionStyledLayoutStructureItem == null) {
					break;
				}

				JSONObject outerCollectionJSONObject =
					outerCollectionStyledLayoutStructureItem.
						getCollectionJSONObject();

				if (!resolutionKeys.add(
						StringBundler.concat(
							"related#", groupId, StringPool.POUND, key,
							StringPool.POUND, outerCollectionJSONObject))) {

					return null;
				}

				return _getCollectionSearchRequestBuilderSupplier(
					outerCollectionJSONObject, groupId, resolutionContext);
			}

			parentLayoutStructureItem = layoutStructure.getLayoutStructureItem(
				parentLayoutStructureItem.getParentItemId());
		}

		if (!Objects.equals(
				layout.getType(), LayoutConstants.TYPE_ASSET_DISPLAY) ||
			!resolutionKeys.add(
				StringBundler.concat(
					"displayPageTemplate#", layout.getPlid(), StringPool.POUND,
					key))) {

			return null;
		}

		LayoutPageTemplateEntry layoutPageTemplateEntry =
			_layoutPageTemplateEntryLocalService.
				fetchLayoutPageTemplateEntryByPlid(layout.getPlid());

		Query query = _getObjectDefinitionQuery(
			_portal.getClassName(layoutPageTemplateEntry.getClassNameId()),
			resolutionContext.getConnectedDepotGroupIds(groupId));

		return () -> _getSearchRequestBuilder(
			null, resolutionContext.getCompanyId(), query);
	}

	private SearchRequestBuilder _getSearchRequestBuilder(
		BooleanClause<com.liferay.portal.kernel.search.Query>[] booleanClauses,
		long companyId, Query query) {

		return _searchRequestBuilderFactory.builder(
		).companyId(
			companyId
		).emptySearchEnabled(
			true
		).modelIndexerClasses(
		).query(
			query
		).withSearchContext(
			searchContext -> {
				searchContext.setAttribute(
					Field.STATUS, WorkflowConstants.STATUS_ANY);

				if (booleanClauses != null) {
					searchContext.setBooleanClauses(booleanClauses);
				}
			}
		);
	}

	private long[] _getSegmentsEntryIds(AssetListEntry assetListEntry) {
		List<AssetListEntrySegmentsEntryRel> assetListEntrySegmentsEntryRels =
			_assetListEntrySegmentsEntryRelLocalService.
				getAssetListEntrySegmentsEntryRels(
					assetListEntry.getAssetListEntryId(), QueryUtil.ALL_POS,
					QueryUtil.ALL_POS);

		if (assetListEntrySegmentsEntryRels.isEmpty()) {
			return new long[] {0};
		}

		return ListUtil.toLongArray(
			assetListEntrySegmentsEntryRels,
			AssetListEntrySegmentsEntryRel::getSegmentsEntryId);
	}

	private List<Object[]> _getUsageRows(long companyId) {
		return _assetListEntryUsageLocalService.dslQuery(
			DSLQueryFactoryUtil.select(
				AssetListEntryUsageTable.INSTANCE.groupId,
				AssetListEntryUsageTable.INSTANCE.plid,
				AssetListEntryUsageTable.INSTANCE.classNameId,
				AssetListEntryUsageTable.INSTANCE.key,
				AssetListEntryUsageTable.INSTANCE.containerKey
			).from(
				AssetListEntryUsageTable.INSTANCE
			).where(
				AssetListEntryUsageTable.INSTANCE.companyId.eq(
					companyId
				).and(
					AssetListEntryUsageTable.INSTANCE.containerType.eq(
						_portal.getClassNameId(
							CollectionStyledLayoutStructureItem.class))
				)
			));
	}

	private Set<Long> _getVocabularyAssetCategoryIds(
		Set<Long> assetCategoryIds) {

		Set<Long> vocabularyAssetCategoryIds = new HashSet<>();

		for (long assetCategoryId : assetCategoryIds) {
			AssetCategory assetCategory =
				_assetCategoryLocalService.fetchAssetCategory(assetCategoryId);

			if (assetCategory == null) {
				continue;
			}

			vocabularyAssetCategoryIds.addAll(
				TransformUtil.transform(
					_assetCategoryLocalService.getVocabularyCategories(
						assetCategory.getVocabularyId(), QueryUtil.ALL_POS,
						QueryUtil.ALL_POS, null),
					AssetCategory::getCategoryId));
		}

		return vocabularyAssetCategoryIds;
	}

	private boolean _isPublished(Layout layout) {
		if (layout.isDraftLayout()) {
			return false;
		}

		if (Objects.equals(
				layout.getType(), LayoutConstants.TYPE_ASSET_DISPLAY)) {

			LayoutPageTemplateEntry layoutPageTemplateEntry =
				_layoutPageTemplateEntryLocalService.
					fetchLayoutPageTemplateEntryByPlid(layout.getPlid());

			if ((layoutPageTemplateEntry != null) &&
				(layoutPageTemplateEntry.getStatus() ==
					WorkflowConstants.STATUS_APPROVED)) {

				return true;
			}

			return false;
		}

		if (layout.isTypeContent() && layout.isPublished()) {
			return true;
		}

		return false;
	}

	private void _resolveAssetListEntryUsage(
			String key, ResolutionContext resolutionContext)
		throws Exception {

		Set<String> resolutionKeys = resolutionContext.getResolutionKeys();

		if (!resolutionKeys.add("assetListEntry#" + key)) {
			return;
		}

		AssetListEntry assetListEntry =
			_assetListEntryLocalService.fetchAssetListEntry(
				GetterUtil.getLong(key));

		if (assetListEntry == null) {
			return;
		}

		CollectionUsages collectionUsages =
			resolutionContext.getCollectionUsages();

		if (assetListEntry.getType() ==
				AssetListEntryTypeConstants.TYPE_MANUAL) {

			for (long objectEntryId :
					_getAssetListEntryObjectEntryIds(assetListEntry)) {

				collectionUsages.addObjectEntryId(objectEntryId);
			}

			return;
		}

		for (long segmentsEntryId : _getSegmentsEntryIds(assetListEntry)) {
			String booleanQueryKey = StringBundler.concat(
				assetListEntry.getGroupId(), StringPool.POUND,
				assetListEntry.getAssetEntryType(), StringPool.POUND,
				assetListEntry.getAssetEntrySubtype(), StringPool.POUND,
				assetListEntry.getTypeSettings(segmentsEntryId));

			if (collectionUsages.hasBooleanQuery(booleanQueryKey)) {
				continue;
			}

			com.liferay.portal.kernel.search.BooleanQuery booleanQuery =
				_assetListAssetEntryProvider.getBooleanQuery(
					assetListEntry, new long[] {segmentsEntryId},
					StringPool.BLANK);

			if (booleanQuery == null) {
				continue;
			}

			AssetEntryQuery assetEntryQuery =
				_assetListAssetEntryProvider.getAssetEntryQuery(
					assetListEntry, new long[] {segmentsEntryId},
					StringPool.BLANK);

			collectionUsages.addBooleanQuery(
				booleanQuery, assetEntryQuery.getGroupIds(), booleanQueryKey);
		}
	}

	private void _resolveInfoCollectionProviderUsage(
			String containerKey, long groupId, String key, Layout layout,
			ObjectDefinition objectDefinition,
			ResolutionContext resolutionContext)
		throws Exception {

		JSONObject configJSONObject = null;

		LayoutStructure layoutStructure = resolutionContext.getLayoutStructure(
			groupId, layout.getPlid());

		if (layoutStructure != null) {
			CollectionStyledLayoutStructureItem
				collectionStyledLayoutStructureItem =
					_getCollectionStyledLayoutStructureItem(
						layoutStructure, containerKey);

			if (collectionStyledLayoutStructureItem != null) {
				JSONObject collectionJSONObject =
					collectionStyledLayoutStructureItem.
						getCollectionJSONObject();

				configJSONObject = collectionJSONObject.getJSONObject("config");
			}
		}

		Set<String> resolutionKeys = resolutionContext.getResolutionKeys();

		if (!resolutionKeys.add(
				StringBundler.concat(
					"provider#", groupId, StringPool.POUND, key,
					StringPool.POUND, configJSONObject))) {

			return;
		}

		Query query = _getInfoCollectionProviderQuery(
			configJSONObject, groupId, objectDefinition, resolutionContext);

		if (query != null) {
			CollectionUsages collectionUsages =
				resolutionContext.getCollectionUsages();

			collectionUsages.addQuery(query);
		}
	}

	private void _resolveMostViewedContentRecommendationUsage(
			ResolutionContext resolutionContext)
		throws Exception {

		MostViewedContentRecommendationManager
			mostViewedContentRecommendationManager =
				_mostViewedContentRecommendationManagerSnapshot.get();

		if (mostViewedContentRecommendationManager == null) {
			return;
		}

		long userId = 0;

		int count =
			(int)
				mostViewedContentRecommendationManager.
					getMostViewedContentRecommendationsCount(
						null, resolutionContext.getCompanyId(), userId);

		CollectionUsages collectionUsages =
			resolutionContext.getCollectionUsages();

		for (long objectEntryId :
				_getObjectEntryIds(
					TransformUtil.transform(
						mostViewedContentRecommendationManager.
							getMostViewedContentRecommendations(
								null, resolutionContext.getCompanyId(), count,
								0, userId),
						MostViewedContentRecommendation::
							getRecommendedEntryClassPK))) {

			collectionUsages.addObjectEntryId(objectEntryId);
		}
	}

	private void _resolveRelatedUsage(
			String containerKey, long groupId, String key, Layout layout,
			RelatedInfoItemCollectionProvider<?, ?>
				relatedInfoItemCollectionProvider,
			ResolutionContext resolutionContext)
		throws Exception {

		Supplier<SearchRequestBuilder> outerSearchRequestBuilderSupplier =
			_getOuterSearchRequestBuilderSupplier(
				containerKey, groupId, key, layout, resolutionContext);

		if (outerSearchRequestBuilderSupplier == null) {
			return;
		}

		Class<?> clazz = relatedInfoItemCollectionProvider.getClass();

		String simpleName = clazz.getSimpleName();

		if (Objects.equals(
				simpleName,
				"AssetEntriesWithSameAssetCategoryRelatedInfoItemCollection" +
					"Provider")) {

			_addAssetCategoriesQuery(
				_getAssetCategoryIds(outerSearchRequestBuilderSupplier),
				groupId, resolutionContext);
		}
		else if (Objects.equals(
					simpleName,
					"AssetEntriesWithAssetCategoriesInTheSameAsset" +
						"VocabulariesRelatedInfoItemCollectionProvider")) {

			_addAssetCategoriesQuery(
				_getVocabularyAssetCategoryIds(
					_getAssetCategoryIds(outerSearchRequestBuilderSupplier)),
				groupId, resolutionContext);
		}
		else if (Objects.equals(
					simpleName,
					"OneToManyObjectRelationshipRelatedInfoCollection" +
						"Provider")) {

			_addObjectRelationshipQuery(
				key, outerSearchRequestBuilderSupplier, resolutionContext);
		}
	}

	private void _resolveUsage(
			long classNameId, String containerKey, long groupId, String key,
			Layout layout, ResolutionContext resolutionContext)
		throws Exception {

		if (classNameId == _portal.getClassNameId(AssetListEntry.class)) {
			_resolveAssetListEntryUsage(key, resolutionContext);

			return;
		}

		ObjectDefinition objectDefinition = _getCMSObjectDefinition(
			resolutionContext.getCompanyId(), key);

		if (objectDefinition != null) {
			_resolveInfoCollectionProviderUsage(
				containerKey, groupId, key, layout, objectDefinition,
				resolutionContext);

			return;
		}

		RelatedInfoItemCollectionProvider<?, ?>
			relatedInfoItemCollectionProvider =
				_infoItemServiceRegistry.getInfoItemService(
					RelatedInfoItemCollectionProvider.class, key);

		if (relatedInfoItemCollectionProvider != null) {
			_resolveRelatedUsage(
				containerKey, groupId, key, layout,
				relatedInfoItemCollectionProvider, resolutionContext);

			return;
		}

		InfoCollectionProvider<?> infoCollectionProvider =
			_infoItemServiceRegistry.getInfoItemService(
				InfoCollectionProvider.class, key);

		if (infoCollectionProvider == null) {
			return;
		}

		Class<?> clazz = infoCollectionProvider.getClass();

		if (Objects.equals(
				clazz.getSimpleName(),
				"MostViewedContentRecommendationInfoItemCollectionProvider") &&
			infoCollectionProvider.isAvailable()) {

			_resolveMostViewedContentRecommendationUsage(resolutionContext);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CollectionUsageResolver.class);

	private static final Snapshot<MostViewedContentRecommendationManager>
		_mostViewedContentRecommendationManagerSnapshot = new Snapshot<>(
			CollectionUsageResolver.class,
			MostViewedContentRecommendationManager.class, null, true);

	private final Aggregations _aggregations;
	private final AssetCategoryLocalService _assetCategoryLocalService;
	private final AssetEntryLocalService _assetEntryLocalService;
	private final AssetListAssetEntryProvider _assetListAssetEntryProvider;
	private final AssetListEntryAssetEntryRelLocalService
		_assetListEntryAssetEntryRelLocalService;
	private final AssetListEntryLocalService _assetListEntryLocalService;
	private final AssetListEntrySegmentsEntryRelLocalService
		_assetListEntrySegmentsEntryRelLocalService;
	private final AssetListEntryUsageLocalService
		_assetListEntryUsageLocalService;
	private final DepotEntryLocalService _depotEntryLocalService;
	private final InfoItemServiceRegistry _infoItemServiceRegistry;
	private final LayoutLocalService _layoutLocalService;
	private final LayoutPageTemplateEntryLocalService
		_layoutPageTemplateEntryLocalService;
	private final LayoutPageTemplateStructureLocalService
		_layoutPageTemplateStructureLocalService;
	private final ObjectDefinitionLocalService _objectDefinitionLocalService;
	private final ObjectEntryLocalService _objectEntryLocalService;
	private final ObjectFieldLocalService _objectFieldLocalService;
	private final ObjectRelationshipLocalService
		_objectRelationshipLocalService;
	private final Portal _portal;
	private final SearchRequestBuilderFactory _searchRequestBuilderFactory;
	private final Searcher _searcher;

	private class ResolutionContext {

		public CollectionUsages getCollectionUsages() {
			return _collectionUsages;
		}

		public long getCompanyId() {
			return _companyId;
		}

		public Set<Long> getConnectedDepotGroupIds(long groupId)
			throws Exception {

			Set<Long> connectedDepotGroupIds = _connectedDepotGroupIdsMap.get(
				groupId);

			if (connectedDepotGroupIds != null) {
				return connectedDepotGroupIds;
			}

			connectedDepotGroupIds = new HashSet<>(
				TransformUtil.transform(
					_depotEntryLocalService.getGroupConnectedDepotEntries(
						groupId, DepotConstants.TYPE_SPACE, QueryUtil.ALL_POS,
						QueryUtil.ALL_POS),
					DepotEntry::getGroupId));

			_connectedDepotGroupIdsMap.put(groupId, connectedDepotGroupIds);

			return connectedDepotGroupIds;
		}

		public Layout getLayout(long plid) {
			return _layouts.computeIfAbsent(
				plid, key -> _layoutLocalService.fetchLayout(plid));
		}

		public LayoutStructure getLayoutStructure(long groupId, long plid) {
			LayoutStructure layoutStructure = _layoutStructures.get(plid);

			if (layoutStructure != null) {
				return layoutStructure;
			}

			LayoutPageTemplateStructure layoutPageTemplateStructure =
				_layoutPageTemplateStructureLocalService.
					fetchLayoutPageTemplateStructure(groupId, plid);

			if (layoutPageTemplateStructure == null) {
				return null;
			}

			layoutStructure = LayoutStructure.of(
				layoutPageTemplateStructure.getDefaultSegmentsExperienceData());

			_layoutStructures.put(plid, layoutStructure);

			return layoutStructure;
		}

		public Set<String> getResolutionKeys() {
			return _resolutionKeys;
		}

		private ResolutionContext(
			CollectionUsages collectionUsages, long companyId) {

			_collectionUsages = collectionUsages;
			_companyId = companyId;
		}

		private final CollectionUsages _collectionUsages;
		private final long _companyId;
		private final Map<Long, Set<Long>> _connectedDepotGroupIdsMap =
			new HashMap<>();
		private final Map<Long, LayoutStructure> _layoutStructures =
			new HashMap<>();
		private final Map<Long, Layout> _layouts = new HashMap<>();
		private final Set<String> _resolutionKeys = new HashSet<>();

	}

}