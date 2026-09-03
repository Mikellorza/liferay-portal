/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.cms.internal.resource.v1_0;

import com.liferay.depot.constants.DepotConstants;
import com.liferay.depot.service.DepotEntryLocalService;
import com.liferay.depot.service.DepotEntryService;
import com.liferay.headless.cms.dto.v1_0.SimilarAsset;
import com.liferay.headless.cms.dto.v1_0.SimilarAssetSet;
import com.liferay.headless.cms.internal.util.SimilarAssetDimension;
import com.liferay.headless.cms.internal.util.SimilarAssetSetTitleUtil;
import com.liferay.headless.cms.resource.v1_0.SimilarAssetSetResource;
import com.liferay.object.constants.ObjectFolderConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectEntryService;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.odata.entity.DateTimeEntityField;
import com.liferay.portal.odata.entity.EntityModel;
import com.liferay.portal.odata.entity.StringEntityField;
import com.liferay.portal.search.aggregation.Aggregations;
import com.liferay.portal.search.aggregation.bucket.Bucket;
import com.liferay.portal.search.aggregation.bucket.IncludeExcludeClause;
import com.liferay.portal.search.aggregation.bucket.TermsAggregation;
import com.liferay.portal.search.aggregation.bucket.TermsAggregationResult;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.filter.ComplexQueryPartBuilderFactory;
import com.liferay.portal.search.hits.SearchHit;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.query.QueriesUtil;
import com.liferay.portal.search.query.TermsQuery;
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.searcher.Searcher;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;
import com.liferay.portal.vulcan.util.GroupUtil;

import jakarta.ws.rs.core.MultivaluedMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Mikel Lorza
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/similar-asset-set.properties",
	scope = ServiceScope.PROTOTYPE, service = SimilarAssetSetResource.class
)
public class SimilarAssetSetResourceImpl
	extends BaseSimilarAssetSetResourceImpl {

	@Override
	public EntityModel getEntityModel(MultivaluedMap multivaluedMap) {
		return _entityModel;
	}

	@Override
	public Page<SimilarAssetSet> getSimilarAssetSetsPage(
			Long assetLibraryId, String dimension, String search,
			Pagination pagination, Sort[] sorts)
		throws Exception {

		if (!FeatureFlagManagerUtil.isEnabled(
				contextCompany.getCompanyId(), "LPD-82226")) {

			return Page.of(new ArrayList<>(), pagination, 0);
		}

		Long[] groupIds = _getGroupIds(assetLibraryId);
		List<ObjectDefinition> objectDefinitions = _getCMSObjectDefinitions();

		if (ArrayUtil.isEmpty(groupIds) || objectDefinitions.isEmpty()) {
			return Page.of(new ArrayList<>(), pagination, 0);
		}

		SimilarAssetDimension similarAssetDimension = SimilarAssetDimension.get(
			dimension);

		String[] entryClassNames = ArrayUtil.toStringArray(
			ListUtil.toList(objectDefinitions, ObjectDefinition::getClassName));

		List<String> sharedSimilarAssets = _searchSharedSimilarAssets(
			entryClassNames, groupIds, similarAssetDimension);

		Map<Long, List<Long>> objectEntryIdsMap = _getObjectEntryIdsMap(
			_searchSharedSimilarAssetDocuments(
				entryClassNames, groupIds, sharedSimilarAssets,
				similarAssetDimension),
			new HashSet<>(sharedSimilarAssets), similarAssetDimension);

		Map<Long, ObjectDefinition> objectDefinitionsMap = new HashMap<>();

		for (ObjectDefinition objectDefinition : objectDefinitions) {
			objectDefinitionsMap.put(
				objectDefinition.getObjectDefinitionId(), objectDefinition);
		}

		if (Validator.isNull(search) && ArrayUtil.isEmpty(sorts)) {
			long totalCount = 0;

			for (List<Long> objectEntryIds : objectEntryIdsMap.values()) {
				totalCount += objectEntryIds.size();
			}

			return Page.of(
				_getSimilarAssetSets(
					objectDefinitionsMap, objectEntryIdsMap, pagination),
				pagination, totalCount);
		}

		List<SimilarAssetSet> similarAssetSets = _filter(
			search,
			_getSimilarAssetSets(
				objectDefinitionsMap, objectEntryIdsMap, null));

		if (ArrayUtil.isNotEmpty(sorts)) {
			similarAssetSets.sort(_getSimilarAssetSetComparator(sorts));
		}

		return Page.of(
			_getPage(pagination, similarAssetSets), pagination,
			_getTotalCount(similarAssetSets));
	}

	private List<SimilarAssetSet> _filter(
		String search, List<SimilarAssetSet> similarAssetSets) {

		if (Validator.isNull(search)) {
			return similarAssetSets;
		}

		String[] keywords = StringUtil.split(
			StringUtil.toLowerCase(search), ' ');

		if (ArrayUtil.isEmpty(keywords)) {
			return similarAssetSets;
		}

		List<SimilarAssetSet> filteredSimilarAssetSets = new ArrayList<>();

		for (SimilarAssetSet similarAssetSet : similarAssetSets) {
			SimilarAsset[] similarAssets = ArrayUtil.filter(
				similarAssetSet.getSimilarAssets(),
				similarAsset -> _hasKeywords(keywords, similarAsset));

			if (ArrayUtil.isEmpty(similarAssets)) {
				continue;
			}

			similarAssetSet.setSimilarAssets(() -> similarAssets);

			filteredSimilarAssetSets.add(similarAssetSet);
		}

		return filteredSimilarAssetSets;
	}

	private List<ObjectDefinition> _getCMSObjectDefinitions() throws Exception {
		return _objectDefinitionService.getCMSObjectDefinitions(
			contextCompany.getCompanyId(),
			new String[] {
				ObjectFolderConstants.
					EXTERNAL_REFERENCE_CODE_CONTENT_STRUCTURES,
				ObjectFolderConstants.EXTERNAL_REFERENCE_CODE_FILE_TYPES
			});
	}

	private Map<Long, List<Long>> _getGroupedObjectEntryIdsMap(
		Map<Long, Long> parentObjectEntryIds) {

		Map<Long, List<Long>> objectEntryIdsMap = new LinkedHashMap<>();

		for (Long objectEntryId : parentObjectEntryIds.keySet()) {
			List<Long> rootObjectEntryIds = objectEntryIdsMap.computeIfAbsent(
				_getRootObjectEntryId(objectEntryId, parentObjectEntryIds),
				rootObjectEntryId -> new ArrayList<>());

			rootObjectEntryIds.add(objectEntryId);
		}

		Map<Long, List<Long>> groupedObjectEntryIdsMap = new HashMap<>();

		for (List<Long> objectEntryIds : objectEntryIdsMap.values()) {
			if (objectEntryIds.size() < 2) {
				continue;
			}

			Collections.sort(objectEntryIds);

			groupedObjectEntryIdsMap.put(objectEntryIds.get(0), objectEntryIds);
		}

		return _getSortedObjectEntryIdsMap(groupedObjectEntryIdsMap);
	}

	private Long[] _getGroupIds(Long assetLibraryId) {
		List<Long> depotEntryGroupIds =
			_depotEntryService.getDepotEntryGroupIds(
				contextCompany.getCompanyId(), contextUser.getUserId(),
				DepotConstants.TYPE_SPACE);

		if (assetLibraryId == null) {
			return depotEntryGroupIds.toArray(new Long[0]);
		}

		Long groupId = GroupUtil.getDepotGroupId(
			String.valueOf(assetLibraryId), contextCompany.getCompanyId(),
			_depotEntryLocalService, groupLocalService);

		if ((groupId == null) || !depotEntryGroupIds.contains(groupId)) {
			return new Long[0];
		}

		return new Long[] {groupId};
	}

	private String _getItemURL(ObjectEntry objectEntry) throws Exception {
		ModelResourcePermission<ObjectEntry> modelResourcePermission =
			_objectEntryService.getModelResourcePermission(
				objectEntry.getObjectDefinitionId());

		if (!modelResourcePermission.contains(
				PermissionThreadLocal.getPermissionChecker(), objectEntry,
				ActionKeys.UPDATE)) {

			return null;
		}

		return StringBundler.concat(
			_portal.getPortalURL(contextHttpServletRequest),
			_portal.getPathMain(), GroupConstants.CMS_FRIENDLY_URL,
			"/edit_content_item?objectEntryId=",
			objectEntry.getObjectEntryId());
	}

	private Date _getMaxDateModified(SimilarAssetSet similarAssetSet) {
		Date maxDateModified = null;

		for (SimilarAsset similarAsset : similarAssetSet.getSimilarAssets()) {
			Date dateModified = similarAsset.getDateModified();

			if (dateModified == null) {
				continue;
			}

			if ((maxDateModified == null) ||
				dateModified.after(maxDateModified)) {

				maxDateModified = dateModified;
			}
		}

		return maxDateModified;
	}

	private Map<Long, List<Long>> _getObjectEntryIdsMap(
		List<Document> documents, Set<String> sharedSimilarAssets,
		SimilarAssetDimension similarAssetDimension) {

		Map<Long, Long> parentObjectEntryIds = new LinkedHashMap<>();
		Map<String, List<Long>> objectEntryIdsMap = new HashMap<>();

		for (Document document : documents) {
			Long objectEntryId = document.getLong("objectEntryId");

			if (objectEntryId == null) {
				continue;
			}

			parentObjectEntryIds.putIfAbsent(objectEntryId, objectEntryId);

			for (String similarAsset :
					document.getStrings(similarAssetDimension.getFieldName())) {

				if (!sharedSimilarAssets.contains(similarAsset)) {
					continue;
				}

				List<Long> similarAssetObjectEntryIds =
					objectEntryIdsMap.computeIfAbsent(
						similarAsset, key -> new ArrayList<>());

				similarAssetObjectEntryIds.add(objectEntryId);
			}
		}

		_mergeSimilarObjectEntryIds(
			objectEntryIdsMap, parentObjectEntryIds, similarAssetDimension);

		return _getGroupedObjectEntryIdsMap(parentObjectEntryIds);
	}

	private List<SimilarAssetSet> _getPage(
		Pagination pagination, List<SimilarAssetSet> similarAssetSets) {

		if (pagination == null) {
			return similarAssetSets;
		}

		int endPosition = pagination.getEndPosition();
		int startPosition = pagination.getStartPosition();

		if ((endPosition < 0) || (startPosition < 0)) {
			return similarAssetSets;
		}

		List<SimilarAssetSet> pageSimilarAssetSets = new ArrayList<>();

		int position = 0;

		for (SimilarAssetSet similarAssetSet : similarAssetSets) {
			SimilarAsset[] similarAssets = similarAssetSet.getSimilarAssets();

			int setStartPosition = position;

			position += similarAssets.length;

			if (position <= startPosition) {
				continue;
			}

			if (setStartPosition >= endPosition) {
				break;
			}

			SimilarAsset[] pageSimilarAssets = Arrays.copyOfRange(
				similarAssets, Math.max(startPosition - setStartPosition, 0),
				Math.min(endPosition - setStartPosition, similarAssets.length));

			similarAssetSet.setSimilarAssets(() -> pageSimilarAssets);

			pageSimilarAssetSets.add(similarAssetSet);
		}

		return pageSimilarAssetSets;
	}

	private Long _getRootObjectEntryId(
		Long objectEntryId, Map<Long, Long> parentObjectEntryIds) {

		Long rootObjectEntryId = objectEntryId;

		Long parentObjectEntryId = parentObjectEntryIds.get(rootObjectEntryId);

		while (!parentObjectEntryId.equals(rootObjectEntryId)) {
			rootObjectEntryId = parentObjectEntryId;

			parentObjectEntryId = parentObjectEntryIds.get(rootObjectEntryId);
		}

		while (!objectEntryId.equals(rootObjectEntryId)) {
			objectEntryId = parentObjectEntryIds.put(
				objectEntryId, rootObjectEntryId);
		}

		return rootObjectEntryId;
	}

	private Consumer<SearchContext> _getSearchContextConsumer(Long[] groupIds) {
		long[] scopedGroupIds = ArrayUtil.toArray(groupIds);

		return searchContext -> {
			searchContext.setAttribute(
				Field.STATUS, WorkflowConstants.STATUS_APPROVED);
			searchContext.setGroupIds(scopedGroupIds);

			searchContext.setUserId(contextUser.getUserId());
		};
	}

	private Comparator<SimilarAssetSet> _getSimilarAssetSetComparator(
		Sort[] sorts) {

		Comparator<SimilarAssetSet> comparator = null;

		for (Sort sort : sorts) {
			Comparator<SimilarAssetSet> sortComparator = Comparator.comparing(
				SimilarAssetSet::getTitle,
				Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));

			if (Objects.equals(
					sort.getFieldName(), _ENTITY_FIELD_NAME_DATE_MODIFIED)) {

				sortComparator = Comparator.comparing(
					this::_getMaxDateModified,
					Comparator.nullsLast(Comparator.naturalOrder()));
			}

			if (sort.isReverse()) {
				sortComparator = sortComparator.reversed();
			}

			if (comparator == null) {
				comparator = sortComparator;
			}
			else {
				comparator = comparator.thenComparing(sortComparator);
			}
		}

		return comparator;
	}

	private List<SimilarAssetSet> _getSimilarAssetSets(
			Map<Long, ObjectDefinition> objectDefinitionsMap,
			Map<Long, List<Long>> objectEntryIdsMap, Pagination pagination)
		throws Exception {

		List<SimilarAssetSet> similarAssetSets = new ArrayList<>();

		int endPosition = -1;
		int startPosition = -1;

		if (pagination != null) {
			endPosition = pagination.getEndPosition();
			startPosition = pagination.getStartPosition();
		}

		int position = 0;

		for (List<Long> objectEntryIds : objectEntryIdsMap.values()) {
			int setStartPosition = position;

			position += objectEntryIds.size();

			int pageEndIndex = objectEntryIds.size();
			int pageStartIndex = 0;

			if ((endPosition >= 0) && (startPosition >= 0)) {
				if (position <= startPosition) {
					continue;
				}

				if (setStartPosition >= endPosition) {
					break;
				}

				pageEndIndex = Math.min(
					endPosition - setStartPosition, objectEntryIds.size());
				pageStartIndex = Math.max(startPosition - setStartPosition, 0);
			}

			similarAssetSets.add(
				_toSimilarAssetSet(
					objectDefinitionsMap, objectEntryIds, pageEndIndex,
					pageStartIndex));
		}

		return similarAssetSets;
	}

	private Map<Long, List<Long>> _getSortedObjectEntryIdsMap(
		Map<Long, List<Long>> objectEntryIdsMap) {

		List<Long> lowestObjectEntryIds = ListUtil.fromMapKeys(
			objectEntryIdsMap);

		lowestObjectEntryIds.sort(
			Comparator.comparingInt(
				(Long lowestObjectEntryId) -> {
					List<Long> objectEntryIds = objectEntryIdsMap.get(
						lowestObjectEntryId);

					return objectEntryIds.size();
				}
			).reversed(
			).thenComparing(
				Comparator.naturalOrder()
			));

		Map<Long, List<Long>> sortedObjectEntryIdsMap = new LinkedHashMap<>();

		for (Long lowestObjectEntryId : lowestObjectEntryIds) {
			sortedObjectEntryIdsMap.put(
				lowestObjectEntryId,
				objectEntryIdsMap.get(lowestObjectEntryId));
		}

		return sortedObjectEntryIdsMap;
	}

	private long _getTotalCount(List<SimilarAssetSet> similarAssetSets) {
		long totalCount = 0;

		for (SimilarAssetSet similarAssetSet : similarAssetSets) {
			SimilarAsset[] similarAssets = similarAssetSet.getSimilarAssets();

			totalCount += similarAssets.length;
		}

		return totalCount;
	}

	private boolean _hasKeywords(String[] keywords, SimilarAsset similarAsset) {
		String title = StringUtil.toLowerCase(similarAsset.getTitle());

		if (title == null) {
			return false;
		}

		for (String keyword : keywords) {
			if (!title.contains(keyword)) {
				return false;
			}
		}

		return true;
	}

	private void _mergeObjectEntryIds(
		Long objectEntryId1, Long objectEntryId2,
		Map<Long, Long> parentObjectEntryIds) {

		Long rootObjectEntryId1 = _getRootObjectEntryId(
			objectEntryId1, parentObjectEntryIds);
		Long rootObjectEntryId2 = _getRootObjectEntryId(
			objectEntryId2, parentObjectEntryIds);

		if (!rootObjectEntryId1.equals(rootObjectEntryId2)) {
			parentObjectEntryIds.put(rootObjectEntryId1, rootObjectEntryId2);
		}
	}

	private void _mergeObjectEntryIds(
		Map<Long, List<String>> similarAssetsMap,
		Map<Long, Long> parentObjectEntryIds,
		SimilarAssetDimension similarAssetDimension) {

		int minSharedSimilarAssets =
			similarAssetDimension.getMinSharedSimilarAssets();

		Map<String, Long> objectEntryIdsMap = new HashMap<>();

		for (Map.Entry<Long, List<String>> entry :
				similarAssetsMap.entrySet()) {

			List<String> similarAssets = entry.getValue();

			if (similarAssets.size() < minSharedSimilarAssets) {
				continue;
			}

			Long objectEntryId = objectEntryIdsMap.putIfAbsent(
				StringUtil.merge(similarAssets), entry.getKey());

			if (objectEntryId != null) {
				_mergeObjectEntryIds(
					objectEntryId, entry.getKey(), parentObjectEntryIds);
			}
		}
	}

	private void _mergeSimilarObjectEntryIds(
		Map<String, List<Long>> objectEntryIdsMap,
		Map<Long, Long> parentObjectEntryIds,
		SimilarAssetDimension similarAssetDimension) {

		int minSharedSimilarAssets =
			similarAssetDimension.getMinSharedSimilarAssets();

		Map<Long, List<String>> similarAssetsMap = new LinkedHashMap<>();
		Map<Long, Map<Long, Integer>> sharedSimilarAssetCounts =
			new HashMap<>();

		for (Map.Entry<String, List<Long>> entry :
				objectEntryIdsMap.entrySet()) {

			List<Long> objectEntryIds = entry.getValue();

			if (objectEntryIds.size() > _MAX_ASSETS_PER_SIMILAR_ASSET) {
				for (Long objectEntryId : objectEntryIds) {
					List<String> similarAssets =
						similarAssetsMap.computeIfAbsent(
							objectEntryId, key -> new ArrayList<>());

					similarAssets.add(entry.getKey());
				}

				continue;
			}

			for (int i = 0; i < objectEntryIds.size(); i++) {
				for (int j = i + 1; j < objectEntryIds.size(); j++) {
					Long objectEntryId1 = objectEntryIds.get(i);
					Long objectEntryId2 = objectEntryIds.get(j);

					Long rootObjectEntryId1 = _getRootObjectEntryId(
						objectEntryId1, parentObjectEntryIds);
					Long rootObjectEntryId2 = _getRootObjectEntryId(
						objectEntryId2, parentObjectEntryIds);

					if (rootObjectEntryId1.equals(rootObjectEntryId2)) {
						continue;
					}

					Map<Long, Integer> counts =
						sharedSimilarAssetCounts.computeIfAbsent(
							Math.min(objectEntryId1, objectEntryId2),
							objectEntryId -> new HashMap<>());

					int count = counts.merge(
						Math.max(objectEntryId1, objectEntryId2), 1,
						Integer::sum);

					if (count >= minSharedSimilarAssets) {
						_mergeObjectEntryIds(
							objectEntryId1, objectEntryId2,
							parentObjectEntryIds);
					}
				}
			}
		}

		_mergeObjectEntryIds(
			similarAssetsMap, parentObjectEntryIds, similarAssetDimension);
	}

	private List<Document> _searchSharedSimilarAssetDocuments(
		String[] entryClassNames, Long[] groupIds,
		List<String> sharedSimilarAssets,
		SimilarAssetDimension similarAssetDimension) {

		List<Document> documents = new ArrayList<>();

		if (sharedSimilarAssets.isEmpty()) {
			return documents;
		}

		TermsQuery termsQuery = QueriesUtil.terms(
			similarAssetDimension.getFieldName());

		termsQuery.addValues(sharedSimilarAssets.toArray());

		SearchResponse searchResponse = _searcher.search(
			_searchRequestBuilderFactory.builder(
			).addComplexQueryPart(
				_complexQueryPartBuilderFactory.builder(
				).occur(
					"must"
				).query(
					termsQuery
				).build()
			).companyId(
				contextCompany.getCompanyId()
			).emptySearchEnabled(
				true
			).entryClassNames(
				entryClassNames
			).fetchSourceIncludes(
				new String[] {
					"objectEntryId", similarAssetDimension.getFieldName()
				}
			).size(
				_MAX_DOCUMENTS
			).withSearchContext(
				_getSearchContextConsumer(groupIds)
			).build());

		SearchHits searchHits = searchResponse.getSearchHits();

		for (SearchHit searchHit : searchHits.getSearchHits()) {
			documents.add(searchHit.getDocument());
		}

		return documents;
	}

	private List<String> _searchSharedSimilarAssets(
		String[] entryClassNames, Long[] groupIds,
		SimilarAssetDimension similarAssetDimension) {

		List<String> sharedSimilarAssets = new ArrayList<>();

		TermsAggregation termsAggregation = _aggregations.terms(
			_SIMILAR_ASSETS_AGGREGATION_NAME,
			similarAssetDimension.getFieldName());

		String tokenLanguageId = similarAssetDimension.getTokenLanguageId(
			contextAcceptLanguage.getPreferredLanguageId());

		termsAggregation.setMinDocCount(2);
		termsAggregation.setIncludeExcludeClause(
			new IncludeExcludeClauseImpl(tokenLanguageId + "_.*", null));
		termsAggregation.setSize(_MAX_SIMILAR_ASSETS);

		SearchResponse searchResponse = _searcher.search(
			_searchRequestBuilderFactory.builder(
			).addAggregation(
				termsAggregation
			).companyId(
				contextCompany.getCompanyId()
			).emptySearchEnabled(
				true
			).entryClassNames(
				entryClassNames
			).size(
				0
			).withSearchContext(
				_getSearchContextConsumer(groupIds)
			).build());

		TermsAggregationResult termsAggregationResult =
			(TermsAggregationResult)searchResponse.getAggregationResult(
				_SIMILAR_ASSETS_AGGREGATION_NAME);

		if (termsAggregationResult == null) {
			return sharedSimilarAssets;
		}

		for (Bucket bucket : termsAggregationResult.getBuckets()) {
			sharedSimilarAssets.add(bucket.getKey());
		}

		return sharedSimilarAssets;
	}

	private SimilarAssetSet _toSimilarAssetSet(
			Map<Long, ObjectDefinition> objectDefinitionsMap,
			List<Long> objectEntryIds, int pageEndIndex, int pageStartIndex)
		throws Exception {

		List<SimilarAsset> similarAssets = new ArrayList<>();
		List<String> titles = new ArrayList<>();

		for (Long objectEntryId : objectEntryIds) {
			ObjectEntry objectEntry = _objectEntryLocalService.fetchObjectEntry(
				objectEntryId);

			if (objectEntry == null) {
				continue;
			}

			String title = objectEntry.getTitleValue(
				contextAcceptLanguage.getPreferredLanguageId(), true);

			titles.add(title);

			SimilarAsset similarAsset = new SimilarAsset();

			similarAsset.setDateModified(objectEntry::getModifiedDate);
			similarAsset.setId(() -> objectEntryId);
			similarAsset.setItemURL(() -> _getItemURL(objectEntry));
			similarAsset.setTitle(() -> title);

			ObjectDefinition objectDefinition = objectDefinitionsMap.get(
				objectEntry.getObjectDefinitionId());

			if (objectDefinition != null) {
				similarAsset.setContentType(
					() -> objectDefinition.getLabel(
						contextAcceptLanguage.getPreferredLanguageId(), true));
			}

			similarAssets.add(similarAsset);
		}

		SimilarAssetSet similarAssetSet = new SimilarAssetSet();

		List<SimilarAsset> pageSimilarAssets = ListUtil.subList(
			similarAssets, pageStartIndex, pageEndIndex);

		similarAssetSet.setSimilarAssets(
			() -> pageSimilarAssets.toArray(new SimilarAsset[0]));

		similarAssetSet.setSize(objectEntryIds::size);
		similarAssetSet.setTitle(
			() -> SimilarAssetSetTitleUtil.getTitle(titles));

		return similarAssetSet;
	}

	private static final String _ENTITY_FIELD_NAME_DATE_MODIFIED =
		"dateModified";

	private static final String _ENTITY_FIELD_NAME_TITLE = "title";

	private static final int _MAX_ASSETS_PER_SIMILAR_ASSET = 500;

	private static final int _MAX_DOCUMENTS = 10000;

	private static final int _MAX_SIMILAR_ASSETS = 10000;

	private static final String _SIMILAR_ASSETS_AGGREGATION_NAME =
		"similarAssets";

	private static final EntityModel _entityModel =
		() -> EntityModel.toEntityFieldsMap(
			new DateTimeEntityField(
				_ENTITY_FIELD_NAME_DATE_MODIFIED,
				locale -> _ENTITY_FIELD_NAME_DATE_MODIFIED,
				locale -> _ENTITY_FIELD_NAME_DATE_MODIFIED),
			new StringEntityField(
				_ENTITY_FIELD_NAME_TITLE, locale -> _ENTITY_FIELD_NAME_TITLE));

	@Reference
	private Aggregations _aggregations;

	@Reference
	private ComplexQueryPartBuilderFactory _complexQueryPartBuilderFactory;

	@Reference
	private DepotEntryLocalService _depotEntryLocalService;

	@Reference
	private DepotEntryService _depotEntryService;

	@Reference
	private ObjectDefinitionService _objectDefinitionService;

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

	@Reference
	private ObjectEntryService _objectEntryService;

	@Reference
	private Portal _portal;

	@Reference
	private Searcher _searcher;

	@Reference
	private SearchRequestBuilderFactory _searchRequestBuilderFactory;

	private static class IncludeExcludeClauseImpl
		implements IncludeExcludeClause {

		public IncludeExcludeClauseImpl(
			String includeRegex, String excludeRegex) {

			_includeRegex = includeRegex;
			_excludeRegex = excludeRegex;
		}

		@Override
		public String[] getExcludedValues() {
			return null;
		}

		@Override
		public String getExcludeRegex() {
			return _excludeRegex;
		}

		@Override
		public String[] getIncludedValues() {
			return null;
		}

		@Override
		public String getIncludeRegex() {
			return _includeRegex;
		}

		private final String _excludeRegex;
		private final String _includeRegex;

	}

}