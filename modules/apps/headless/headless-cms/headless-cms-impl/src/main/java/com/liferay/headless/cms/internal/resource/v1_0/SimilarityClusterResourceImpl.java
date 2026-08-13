/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.cms.internal.resource.v1_0;

import com.liferay.depot.constants.DepotConstants;
import com.liferay.depot.service.DepotEntryLocalService;
import com.liferay.depot.service.DepotEntryService;
import com.liferay.headless.cms.dto.v1_0.SimilarityCluster;
import com.liferay.headless.cms.dto.v1_0.SimilarityClusterAsset;
import com.liferay.headless.cms.internal.similarity.SimilarityClusterUtil;
import com.liferay.headless.cms.internal.similarity.SimilarityDimension;
import com.liferay.headless.cms.resource.v1_0.SimilarityClusterResource;
import com.liferay.object.constants.ObjectFolderConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectEntryService;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
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
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Mikel Lorza
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/similarity-cluster.properties",
	scope = ServiceScope.PROTOTYPE, service = SimilarityClusterResource.class
)
public class SimilarityClusterResourceImpl
	extends BaseSimilarityClusterResourceImpl {

	@Override
	public EntityModel getEntityModel(MultivaluedMap multivaluedMap) {
		return _entityModel;
	}

	@Override
	public Page<SimilarityCluster> getSimilarityClustersPage(
			Long assetLibraryId, String dimension, String search,
			Pagination pagination, Sort[] sorts)
		throws Exception {

		List<ObjectDefinition> objectDefinitions = _getCMSObjectDefinitions();

		Long[] groupIds = _getGroupIds(assetLibraryId);

		if (ArrayUtil.isEmpty(groupIds) || objectDefinitions.isEmpty()) {
			return Page.of(new ArrayList<>(), pagination, 0);
		}

		SimilarityDimension similarityDimension = SimilarityDimension.get(
			dimension);

		String[] entryClassNames = ArrayUtil.toStringArray(
			ListUtil.toList(objectDefinitions, ObjectDefinition::getClassName));
		String languageId = contextAcceptLanguage.getPreferredLanguageId();

		String similarityKeyField = similarityDimension.getSimilarityKeyField();

		List<String> sharedSimilarityKeys = _searchSharedSimilarityKeys(
			similarityKeyField, entryClassNames, groupIds, languageId);

		Map<Long, List<Long>> objectEntryIdsByClusterId =
			SimilarityClusterUtil.getClusters(
				similarityKeyField,
				_searchClusteredDocuments(
					similarityKeyField, entryClassNames, groupIds,
					sharedSimilarityKeys),
				new HashSet<>(sharedSimilarityKeys));

		Map<Long, ObjectDefinition> objectDefinitionsById = new HashMap<>();

		for (ObjectDefinition objectDefinition : objectDefinitions) {
			objectDefinitionsById.put(
				objectDefinition.getObjectDefinitionId(), objectDefinition);
		}

		if (Validator.isNull(search) && ArrayUtil.isEmpty(sorts)) {
			return _getDefaultSimilarityClustersPage(
				entryClassNames, groupIds, languageId, objectDefinitionsById,
				objectEntryIdsByClusterId, pagination, similarityDimension);
		}

		Map<Long, ObjectEntry> objectEntriesMap = new HashMap<>();

		List<SimilarityCluster> similarityClusters = _filter(
			search,
			_getSimilarityClusters(
				objectEntryIdsByClusterId.values(), languageId,
				objectDefinitionsById, objectEntriesMap,
				_getSignatures(
					objectEntryIdsByClusterId.values(), entryClassNames,
					groupIds, languageId,
					similarityDimension.getSignatureField()),
				similarityDimension));

		long totalCount = _getTotalCount(similarityClusters);

		_sort(similarityClusters, sorts);

		List<SimilarityCluster> pageSimilarityClusters = _getPage(
			pagination, similarityClusters);

		_setItemURLs(objectEntriesMap, pageSimilarityClusters);

		return Page.of(pageSimilarityClusters, pagination, totalCount);
	}

	private List<SimilarityCluster> _filter(
		String search, List<SimilarityCluster> similarityClusters) {

		if (Validator.isNull(search)) {
			return similarityClusters;
		}

		String[] keywords = StringUtil.split(
			StringUtil.toLowerCase(search), ' ');

		if (ArrayUtil.isEmpty(keywords)) {
			return similarityClusters;
		}

		List<SimilarityCluster> filteredSimilarityClusters = new ArrayList<>();

		for (SimilarityCluster similarityCluster : similarityClusters) {
			List<SimilarityClusterAsset> similarityClusterAssets =
				new ArrayList<>();

			for (SimilarityClusterAsset similarityClusterAsset :
					similarityCluster.getSimilarityClusterAssets()) {

				if (_hasKeywords(keywords, similarityClusterAsset)) {
					similarityClusterAssets.add(similarityClusterAsset);
				}
			}

			if (similarityClusterAssets.isEmpty()) {
				continue;
			}

			SimilarityClusterAsset[] similarityClusterAssetsArray =
				similarityClusterAssets.toArray(new SimilarityClusterAsset[0]);

			similarityCluster.setSimilarityClusterAssets(
				() -> similarityClusterAssetsArray);

			filteredSimilarityClusters.add(similarityCluster);
		}

		return filteredSimilarityClusters;
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

	private Page<SimilarityCluster> _getDefaultSimilarityClustersPage(
			String[] entryClassNames, Long[] groupIds, String languageId,
			Map<Long, ObjectDefinition> objectDefinitionsById,
			Map<Long, List<Long>> objectEntryIdsByClusterId,
			Pagination pagination, SimilarityDimension similarityDimension)
		throws Exception {

		long totalCount = 0;

		for (List<Long> objectEntryIds : objectEntryIdsByClusterId.values()) {
			totalCount += objectEntryIds.size();
		}

		int endPosition = -1;
		int startPosition = -1;

		if (pagination != null) {
			endPosition = pagination.getEndPosition();
			startPosition = pagination.getStartPosition();
		}

		List<List<Long>> pageClusters = new ArrayList<>();
		List<int[]> pageWindows = new ArrayList<>();

		int position = 0;

		for (List<Long> objectEntryIds : objectEntryIdsByClusterId.values()) {
			int clusterStartPosition = position;

			position += objectEntryIds.size();

			if ((endPosition >= 0) && (startPosition >= 0)) {
				if (position <= startPosition) {
					continue;
				}

				if (clusterStartPosition >= endPosition) {
					break;
				}

				pageWindows.add(
					new int[] {
						Math.max(startPosition - clusterStartPosition, 0),
						Math.min(
							endPosition - clusterStartPosition,
							objectEntryIds.size())
					});
			}
			else {
				pageWindows.add(new int[] {0, objectEntryIds.size()});
			}

			pageClusters.add(objectEntryIds);
		}

		Map<Long, ObjectEntry> objectEntriesMap = new HashMap<>();

		List<SimilarityCluster> similarityClusters = _getSimilarityClusters(
			pageClusters, languageId, objectDefinitionsById, objectEntriesMap,
			_getSignatures(
				pageClusters, entryClassNames, groupIds, languageId,
				similarityDimension.getSignatureField()),
			similarityDimension);

		Comparator<SimilarityClusterAsset> comparator =
			_getSimilarityClusterAssetComparator(new Sort[0]);

		for (int i = 0; i < similarityClusters.size(); i++) {
			SimilarityCluster similarityCluster = similarityClusters.get(i);

			SimilarityClusterAsset[] similarityClusterAssets =
				similarityCluster.getSimilarityClusterAssets();

			Arrays.sort(similarityClusterAssets, comparator);

			int[] pageWindow = pageWindows.get(i);

			SimilarityClusterAsset[] pageSimilarityClusterAssets =
				Arrays.copyOfRange(
					similarityClusterAssets,
					Math.min(pageWindow[0], similarityClusterAssets.length),
					Math.min(pageWindow[1], similarityClusterAssets.length));

			similarityCluster.setSimilarityClusterAssets(
				() -> pageSimilarityClusterAssets);
		}

		_setItemURLs(objectEntriesMap, similarityClusters);

		return Page.of(similarityClusters, pagination, totalCount);
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

	private Date _getMaxDateModified(SimilarityCluster similarityCluster) {
		Date maxDateModified = null;

		for (SimilarityClusterAsset similarityClusterAsset :
				similarityCluster.getSimilarityClusterAssets()) {

			Date dateModified = similarityClusterAsset.getDateModified();

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

	private Long _getMinId(SimilarityCluster similarityCluster) {
		Long minId = null;

		for (SimilarityClusterAsset similarityClusterAsset :
				similarityCluster.getSimilarityClusterAssets()) {

			Long id = similarityClusterAsset.getId();

			if (id == null) {
				continue;
			}

			if ((minId == null) || (id < minId)) {
				minId = id;
			}
		}

		return minId;
	}

	private List<SimilarityCluster> _getPage(
		Pagination pagination, List<SimilarityCluster> similarityClusters) {

		if (pagination == null) {
			return similarityClusters;
		}

		int endPosition = pagination.getEndPosition();
		int startPosition = pagination.getStartPosition();

		if ((endPosition < 0) || (startPosition < 0)) {
			return similarityClusters;
		}

		List<SimilarityCluster> pageSimilarityClusters = new ArrayList<>();

		int position = 0;

		for (SimilarityCluster similarityCluster : similarityClusters) {
			SimilarityClusterAsset[] similarityClusterAssets =
				similarityCluster.getSimilarityClusterAssets();

			int clusterStartPosition = position;

			position += similarityClusterAssets.length;

			if (position <= startPosition) {
				continue;
			}

			if (clusterStartPosition >= endPosition) {
				break;
			}

			SimilarityClusterAsset[] pageSimilarityClusterAssets =
				Arrays.copyOfRange(
					similarityClusterAssets,
					Math.max(startPosition - clusterStartPosition, 0),
					Math.min(
						endPosition - clusterStartPosition,
						similarityClusterAssets.length));

			similarityCluster.setSimilarityClusterAssets(
				() -> pageSimilarityClusterAssets);

			pageSimilarityClusters.add(similarityCluster);
		}

		return pageSimilarityClusters;
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

	private Map<Long, long[]> _getSignatures(
		Collection<List<Long>> clusters, String[] entryClassNames,
		Long[] groupIds, String languageId, String signatureField) {

		List<String> objectEntryIds = new ArrayList<>();

		for (List<Long> cluster : clusters) {
			for (Long objectEntryId : cluster) {
				objectEntryIds.add(String.valueOf(objectEntryId));
			}
		}

		Map<Long, long[]> signaturesMap = new HashMap<>();

		if (objectEntryIds.isEmpty()) {
			return signaturesMap;
		}

		TermsQuery termsQuery = QueriesUtil.terms("objectEntryId");

		termsQuery.addValues(objectEntryIds.toArray());

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
				new String[] {"objectEntryId", signatureField}
			).size(
				objectEntryIds.size()
			).withSearchContext(
				_getSearchContextConsumer(groupIds)
			).build());

		SearchHits searchHits = searchResponse.getSearchHits();

		for (SearchHit searchHit : searchHits.getSearchHits()) {
			Document document = searchHit.getDocument();

			Long objectEntryId = document.getLong("objectEntryId");

			if (objectEntryId == null) {
				continue;
			}

			long[] signature = SimilarityClusterUtil.getSignature(
				languageId, document.getStrings(signatureField));

			if (signature != null) {
				signaturesMap.put(objectEntryId, signature);
			}
		}

		return signaturesMap;
	}

	private Comparator<SimilarityClusterAsset>
		_getSimilarityClusterAssetComparator(Sort sort) {

		Comparator<SimilarityClusterAsset> comparator = null;

		if (Objects.equals(sort.getFieldName(), _FIELD_NAME_DATE_MODIFIED)) {
			comparator = Comparator.comparing(
				SimilarityClusterAsset::getDateModified,
				Comparator.nullsLast(Comparator.naturalOrder()));
		}
		else {
			comparator = Comparator.comparing(
				SimilarityClusterAsset::getTitle,
				Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
		}

		if (sort.isReverse()) {
			return comparator.reversed();
		}

		return comparator;
	}

	private Comparator<SimilarityClusterAsset>
		_getSimilarityClusterAssetComparator(Sort[] sorts) {

		Comparator<SimilarityClusterAsset> comparator = null;

		if (sorts != null) {
			for (Sort sort : sorts) {
				Comparator<SimilarityClusterAsset> sortComparator =
					_getSimilarityClusterAssetComparator(sort);

				if (comparator == null) {
					comparator = sortComparator;
				}
				else {
					comparator = comparator.thenComparing(sortComparator);
				}
			}
		}

		if (comparator == null) {
			comparator = Comparator.comparing(
				SimilarityClusterAsset::getTitle,
				Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
		}

		return comparator.thenComparing(
			SimilarityClusterAsset::getId,
			Comparator.nullsLast(Comparator.naturalOrder()));
	}

	private Comparator<SimilarityCluster> _getSimilarityClusterComparator(
		Sort sort) {

		Comparator<SimilarityCluster> comparator = null;

		if (Objects.equals(sort.getFieldName(), _FIELD_NAME_DATE_MODIFIED)) {
			comparator = Comparator.comparing(
				this::_getMaxDateModified,
				Comparator.nullsLast(Comparator.naturalOrder()));
		}
		else {
			comparator = Comparator.comparing(
				SimilarityCluster::getTitle,
				Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
		}

		if (sort.isReverse()) {
			return comparator.reversed();
		}

		return comparator;
	}

	private Comparator<SimilarityCluster> _getSimilarityClusterComparator(
		Sort[] sorts) {

		Comparator<SimilarityCluster> comparator = null;

		if (sorts != null) {
			for (Sort sort : sorts) {
				Comparator<SimilarityCluster> sortComparator =
					_getSimilarityClusterComparator(sort);

				if (comparator == null) {
					comparator = sortComparator;
				}
				else {
					comparator = comparator.thenComparing(sortComparator);
				}
			}
		}

		if (comparator == null) {
			comparator = Comparator.comparing(
				SimilarityCluster::getSize,
				Comparator.nullsLast(Comparator.reverseOrder()));
		}

		return comparator.thenComparing(
			this::_getMinId, Comparator.nullsLast(Comparator.naturalOrder()));
	}

	private List<SimilarityCluster> _getSimilarityClusters(
			Collection<List<Long>> clusters, String languageId,
			Map<Long, ObjectDefinition> objectDefinitionsById,
			Map<Long, ObjectEntry> objectEntriesMap,
			Map<Long, long[]> signaturesMap,
			SimilarityDimension similarityDimension)
		throws Exception {

		return unsafeTransform(
			clusters,
			cluster -> _toSimilarityCluster(
				cluster, languageId, objectDefinitionsById, objectEntriesMap,
				signaturesMap, similarityDimension));
	}

	private long _getTotalCount(List<SimilarityCluster> similarityClusters) {
		long totalCount = 0;

		for (SimilarityCluster similarityCluster : similarityClusters) {
			SimilarityClusterAsset[] similarityClusterAssets =
				similarityCluster.getSimilarityClusterAssets();

			totalCount += similarityClusterAssets.length;
		}

		return totalCount;
	}

	private boolean _hasKeywords(
		String[] keywords, SimilarityClusterAsset similarityClusterAsset) {

		String title = similarityClusterAsset.getTitle();

		if (title == null) {
			return false;
		}

		String lowerCaseTitle = StringUtil.toLowerCase(title);

		for (String keyword : keywords) {
			if (!lowerCaseTitle.contains(keyword)) {
				return false;
			}
		}

		return true;
	}

	private List<Document> _searchClusteredDocuments(
		String similarityKeyField, String[] entryClassNames, Long[] groupIds,
		List<String> sharedSimilarityKeys) {

		List<Document> documents = new ArrayList<>();

		if (sharedSimilarityKeys.isEmpty()) {
			return documents;
		}

		TermsQuery termsQuery = QueriesUtil.terms(similarityKeyField);

		termsQuery.addValues(sharedSimilarityKeys.toArray());

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
				new String[] {"objectEntryId", similarityKeyField}
			).size(
				_MAX_CLUSTERED_DOCUMENTS
			).withSearchContext(
				_getSearchContextConsumer(groupIds)
			).build());

		SearchHits searchHits = searchResponse.getSearchHits();

		for (SearchHit searchHit : searchHits.getSearchHits()) {
			documents.add(searchHit.getDocument());
		}

		return documents;
	}

	private List<String> _searchSharedSimilarityKeys(
		String similarityKeyField, String[] entryClassNames, Long[] groupIds,
		String languageId) {

		List<String> sharedSimilarityKeys = new ArrayList<>();

		TermsAggregation termsAggregation = _aggregations.terms(
			_SIMILARITY_KEYS_AGGREGATION_NAME, similarityKeyField);

		termsAggregation.setMinDocCount(2);
		termsAggregation.setIncludeExcludeClause(
			new IncludeExcludeClauseImpl(languageId + "_.*", null));
		termsAggregation.setSize(_MAX_SIMILARITY_KEYS);

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
				_SIMILARITY_KEYS_AGGREGATION_NAME);

		if (termsAggregationResult == null) {
			return sharedSimilarityKeys;
		}

		for (Bucket bucket : termsAggregationResult.getBuckets()) {
			sharedSimilarityKeys.add(bucket.getKey());
		}

		return sharedSimilarityKeys;
	}

	private void _setItemURLs(
			Map<Long, ObjectEntry> objectEntriesMap,
			List<SimilarityCluster> similarityClusters)
		throws Exception {

		for (SimilarityCluster similarityCluster : similarityClusters) {
			for (SimilarityClusterAsset similarityClusterAsset :
					similarityCluster.getSimilarityClusterAssets()) {

				ObjectEntry objectEntry = objectEntriesMap.get(
					GetterUtil.getLong(similarityClusterAsset.getId()));

				if (objectEntry == null) {
					continue;
				}

				String itemURL = _getItemURL(objectEntry);

				if (itemURL != null) {
					similarityClusterAsset.setItemURL(() -> itemURL);
				}
			}
		}
	}

	private void _sort(
		List<SimilarityCluster> similarityClusters, Sort[] sorts) {

		Comparator<SimilarityClusterAsset> similarityClusterAssetComparator =
			_getSimilarityClusterAssetComparator(sorts);

		for (SimilarityCluster similarityCluster : similarityClusters) {
			Arrays.sort(
				similarityCluster.getSimilarityClusterAssets(),
				similarityClusterAssetComparator);
		}

		similarityClusters.sort(_getSimilarityClusterComparator(sorts));
	}

	private SimilarityCluster _toSimilarityCluster(
			List<Long> cluster, String languageId,
			Map<Long, ObjectDefinition> objectDefinitionsById,
			Map<Long, ObjectEntry> objectEntriesMap,
			Map<Long, long[]> signaturesMap,
			SimilarityDimension similarityDimension)
		throws Exception {

		SimilarityCluster similarityCluster = new SimilarityCluster();

		Long topObjectEntryId = SimilarityClusterUtil.getTopObjectEntryId(
			cluster, signaturesMap);

		long[] topSignature = null;

		if (topObjectEntryId != null) {
			topSignature = signaturesMap.get(topObjectEntryId);
		}

		List<SimilarityClusterAsset> similarityClusterAssets =
			new ArrayList<>();
		List<String> titles = new ArrayList<>();

		String topTitle = null;

		for (Long objectEntryId : cluster) {
			ObjectEntry objectEntry = _objectEntryLocalService.fetchObjectEntry(
				objectEntryId);

			if (objectEntry == null) {
				continue;
			}

			objectEntriesMap.put(objectEntryId, objectEntry);

			boolean topAsset = objectEntryId.equals(topObjectEntryId);

			String title = objectEntry.getTitleValue(languageId, true);

			titles.add(title);

			if (topAsset) {
				topTitle = title;
			}

			SimilarityClusterAsset similarityClusterAsset =
				new SimilarityClusterAsset();

			similarityClusterAsset.setDateModified(
				objectEntry::getModifiedDate);
			similarityClusterAsset.setId(() -> objectEntryId);
			similarityClusterAsset.setTitle(() -> title);
			similarityClusterAsset.setTopAsset(() -> topAsset);

			ObjectDefinition objectDefinition = objectDefinitionsById.get(
				objectEntry.getObjectDefinitionId());

			if (objectDefinition != null) {
				similarityClusterAsset.setContentType(
					() -> objectDefinition.getLabel(languageId, true));
			}

			long[] signature = signaturesMap.get(objectEntryId);

			if (!topAsset && (signature != null) && (topSignature != null)) {
				double similarity = SimilarityClusterUtil.getSimilarity(
					signature, topSignature);

				double similarityPercent = Math.round(similarity * 100.0);

				similarityClusterAsset.setSimilarityPercent(
					() -> similarityPercent);
			}

			similarityClusterAssets.add(similarityClusterAsset);
		}

		String title = similarityDimension.getTitle(titles, topTitle);

		SimilarityClusterAsset[] similarityClusterAssetsArray =
			similarityClusterAssets.toArray(new SimilarityClusterAsset[0]);

		similarityCluster.setSimilarityClusterAssets(
			() -> similarityClusterAssetsArray);

		similarityCluster.setSize(cluster::size);
		similarityCluster.setTitle(() -> title);

		return similarityCluster;
	}

	private static final String _FIELD_NAME_DATE_MODIFIED = "dateModified";

	private static final String _FIELD_NAME_TITLE = "title";

	private static final int _MAX_CLUSTERED_DOCUMENTS = 10000;

	private static final int _MAX_SIMILARITY_KEYS = 10000;

	private static final String _SIMILARITY_KEYS_AGGREGATION_NAME =
		"similarityKeys";

	private static final EntityModel _entityModel =
		() -> EntityModel.toEntityFieldsMap(
			new DateTimeEntityField(
				_FIELD_NAME_DATE_MODIFIED, locale -> _FIELD_NAME_DATE_MODIFIED,
				locale -> _FIELD_NAME_DATE_MODIFIED),
			new StringEntityField(
				_FIELD_NAME_TITLE, locale -> _FIELD_NAME_TITLE));

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