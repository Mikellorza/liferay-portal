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
import com.liferay.headless.cms.dto.v1_0.SimilarityClusterResult;
import com.liferay.headless.cms.internal.similarity.SimilarityDimension;
import com.liferay.headless.cms.resource.v1_0.SimilarityClusterResultResource;
import com.liferay.object.constants.ObjectFolderConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectEntryService;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.search.Field;
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
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.searcher.Searcher;
import com.liferay.portal.vulcan.pagination.Pagination;
import com.liferay.portal.vulcan.util.GroupUtil;

import jakarta.ws.rs.core.MultivaluedMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Mikel Lorza
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/similarity-cluster-result.properties",
	scope = ServiceScope.PROTOTYPE,
	service = SimilarityClusterResultResource.class
)
public class SimilarityClusterResultResourceImpl
	extends BaseSimilarityClusterResultResourceImpl {

	@Override
	public EntityModel getEntityModel(MultivaluedMap multivaluedMap) {
		return _entityModel;
	}

	@Override
	public SimilarityClusterResult getSimilarityCluster(
			Long assetLibraryId, String dimension, String search,
			Pagination pagination, Sort[] sorts)
		throws Exception {

		if (!FeatureFlagManagerUtil.isEnabled(
				contextCompany.getCompanyId(), "LPD-17564")) {

			throw new UnsupportedOperationException();
		}

		SimilarityDimension similarityDimension = SimilarityDimension.get(
			dimension);

		List<ObjectDefinition> objectDefinitions = _getCMSObjectDefinitions();

		Long[] groupIds = _getGroupIds(assetLibraryId);

		if ((similarityDimension == null) || ArrayUtil.isEmpty(groupIds) ||
			objectDefinitions.isEmpty()) {

			return _toSimilarityClusterResult(new ArrayList<>(), 0);
		}

		String languageId = contextAcceptLanguage.getPreferredLanguageId();

		String[] entryClassNames = ArrayUtil.toStringArray(
			ListUtil.toList(objectDefinitions, ObjectDefinition::getClassName));

		// The clustering always spans the whole scope, so that cluster names
		// and sizes never depend on the requested page or search. It reads the
		// signatures of the request's language, so a translation is only ever
		// compared against the same translation of other content

		String bandField = similarityDimension.getBandField();

		List<String> sharedBands = _searchSharedBands(
			bandField, entryClassNames, groupIds, languageId);

		List<List<Long>> clusters = _getClusters(
			_searchClusteredDocuments(
				bandField, sharedBands, entryClassNames, groupIds),
			bandField, new HashSet<>(sharedBands));

		Map<Long, ObjectDefinition> objectDefinitionsMap = new HashMap<>();

		for (ObjectDefinition objectDefinition : objectDefinitions) {
			objectDefinitionsMap.put(
				objectDefinition.getObjectDefinitionId(), objectDefinition);
		}

		// Searching and sorting by an asset field need every clustered asset
		// resolved; the default listing does not, so it only resolves the page

		if (Validator.isNull(search) && ArrayUtil.isEmpty(sorts)) {
			return _getDefaultSimilarityClusterResult(
				clusters, entryClassNames, groupIds, languageId,
				objectDefinitionsMap, pagination, similarityDimension);
		}

		List<SimilarityCluster> similarityClusters = _filter(
			_getSimilarityClusters(
				clusters, languageId, objectDefinitionsMap,
				_getSignatures(
					clusters, entryClassNames, groupIds, languageId,
					similarityDimension.getSignatureField()),
				similarityDimension),
			search);

		long totalCount = _getTotalCount(similarityClusters);

		_sort(similarityClusters, sorts);

		List<SimilarityCluster> pageSimilarityClusters = _getPage(
			similarityClusters, pagination);

		_setItemURLs(pageSimilarityClusters);

		return _toSimilarityClusterResult(pageSimilarityClusters, totalCount);
	}

	private List<SimilarityCluster> _filter(
		List<SimilarityCluster> similarityClusters, String search) {

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

				if (_hasKeywords(similarityClusterAsset, keywords)) {
					similarityClusterAssets.add(similarityClusterAsset);
				}
			}

			if (similarityClusterAssets.isEmpty()) {
				continue;
			}

			SimilarityClusterAsset[] similarityClusterAssetsArray =
				similarityClusterAssets.toArray(new SimilarityClusterAsset[0]);

			// A surviving cluster keeps its original name and size, so that
			// the listing still shows what the cluster is and how big it is

			similarityCluster.setSimilarityClusterAssets(
				() -> similarityClusterAssetsArray);

			filteredSimilarityClusters.add(similarityCluster);
		}

		return filteredSimilarityClusters;
	}

	private Long _find(Map<Long, Long> parents, Long objectEntryId) {
		Long parent = parents.get(objectEntryId);

		while (!parent.equals(objectEntryId)) {
			objectEntryId = parent;

			parent = parents.get(objectEntryId);
		}

		return objectEntryId;
	}

	private List<List<Long>> _getClusters(
		List<Document> documents, String bandField, Set<String> sharedBands) {

		Map<Long, Long> parents = new LinkedHashMap<>();
		Map<String, Long> objectEntryIdsByBand = new HashMap<>();

		for (Document document : documents) {
			Long objectEntryId = document.getLong("objectEntryId");

			if (objectEntryId == null) {
				continue;
			}

			parents.putIfAbsent(objectEntryId, objectEntryId);

			for (String band : document.getStrings(bandField)) {
				if (!sharedBands.contains(band)) {
					continue;
				}

				Long otherObjectEntryId = objectEntryIdsByBand.putIfAbsent(
					band, objectEntryId);

				if (otherObjectEntryId != null) {
					_union(parents, otherObjectEntryId, objectEntryId);
				}
			}
		}

		Map<Long, List<Long>> clusters = new LinkedHashMap<>();

		for (Long objectEntryId : parents.keySet()) {
			Long root = _find(parents, objectEntryId);

			List<Long> cluster = clusters.computeIfAbsent(
				root, key -> new ArrayList<>());

			cluster.add(objectEntryId);
		}

		List<List<Long>> similarityClusters = new ArrayList<>();

		for (List<Long> cluster : clusters.values()) {
			if (cluster.size() >= 2) {
				similarityClusters.add(cluster);
			}
		}

		return similarityClusters;
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

	private SimilarityClusterResult _getDefaultSimilarityClusterResult(
			List<List<Long>> clusters, String[] entryClassNames,
			Long[] groupIds, String languageId,
			Map<Long, ObjectDefinition> objectDefinitionsMap,
			Pagination pagination, SimilarityDimension similarityDimension)
		throws Exception {

		long totalCount = 0;

		for (List<Long> cluster : clusters) {
			totalCount += cluster.size();
		}

		// The biggest clusters come first, which the cluster sizes already
		// decide, so the order costs nothing to establish

		List<List<Long>> orderedClusters = new ArrayList<>(clusters);

		orderedClusters.sort(
			Comparator.comparingInt(
				(List<Long> cluster) -> cluster.size()
			).reversed(
			).thenComparing(
				this::_getMinObjectEntryId
			));

		int endPosition = -1;
		int startPosition = -1;

		if (pagination != null) {
			endPosition = pagination.getEndPosition();
			startPosition = pagination.getStartPosition();
		}

		List<List<Long>> pageClusters = new ArrayList<>();
		List<int[]> pageWindows = new ArrayList<>();

		int position = 0;

		for (List<Long> cluster : orderedClusters) {
			int clusterStartPosition = position;

			position += cluster.size();

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
							endPosition - clusterStartPosition, cluster.size())
					});
			}
			else {
				pageWindows.add(new int[] {0, cluster.size()});
			}

			pageClusters.add(cluster);
		}

		// The signatures behind the percentage and the top asset are only read
		// for the clusters the page shows

		List<SimilarityCluster> similarityClusters = _getSimilarityClusters(
			pageClusters, languageId, objectDefinitionsMap,
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
					similarityClusterAssets, pageWindow[0], pageWindow[1]);

			similarityCluster.setSimilarityClusterAssets(
				() -> pageSimilarityClusterAssets);
		}

		_setItemURLs(similarityClusters);

		return _toSimilarityClusterResult(similarityClusters, totalCount);
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

		// The same edit URL the CMS listings link their pencil action to, and
		// only for a user who can update the content, so that the listing hides
		// the action for everyone else

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

	private Long _getMinObjectEntryId(List<Long> cluster) {
		Long minObjectEntryId = null;

		for (Long objectEntryId : cluster) {
			if ((minObjectEntryId == null) ||
				(objectEntryId < minObjectEntryId)) {

				minObjectEntryId = objectEntryId;
			}
		}

		return minObjectEntryId;
	}

	private List<SimilarityCluster> _getPage(
		List<SimilarityCluster> similarityClusters, Pagination pagination) {

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

			// A cluster on the edge of the window is served with only the
			// assets that fall inside it, so that the next page can repeat its
			// heading with the remaining ones

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

	private Map<Long, long[]> _getSignatures(
		List<List<Long>> clusters, String[] entryClassNames, Long[] groupIds,
		String languageId, String signatureField) {

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

		SearchRequestBuilder searchRequestBuilder =
			_searchRequestBuilderFactory.builder();

		long[] scopedGroupIds = _toPrimitiveArray(groupIds);

		searchRequestBuilder.addComplexQueryPart(
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
			searchContext -> {
				searchContext.setAttribute(
					Field.STATUS, WorkflowConstants.STATUS_APPROVED);
				searchContext.setGroupIds(scopedGroupIds);
			}
		);

		SearchResponse searchResponse = _searcher.search(
			searchRequestBuilder.build());

		SearchHits searchHits = searchResponse.getSearchHits();

		for (SearchHit searchHit : searchHits.getSearchHits()) {
			Document document = searchHit.getDocument();

			Long objectEntryId = document.getLong("objectEntryId");

			if (objectEntryId == null) {
				continue;
			}

			long[] signature = _parseSignature(
				document.getStrings(signatureField), languageId);

			if (signature != null) {
				signaturesMap.put(objectEntryId, signature);
			}
		}

		return signaturesMap;
	}

	private double _getSimilarity(long[] signature1, long[] signature2) {
		int matches = 0;

		for (int i = 0; i < _SIGNATURE_SIZE; i++) {
			if (signature1[i] == signature2[i]) {
				matches++;
			}
		}

		return (double)matches / _SIGNATURE_SIZE;
	}

	private Comparator<SimilarityClusterAsset>
		_getSimilarityClusterAssetComparator(Sort sort) {

		Comparator<SimilarityClusterAsset> comparator = null;

		if (Objects.equals(sort.getFieldName(), _DATE_MODIFIED_FIELD_NAME)) {
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

		if (Objects.equals(sort.getFieldName(), _DATE_MODIFIED_FIELD_NAME)) {

			// A cluster is dated by its most recently modified asset

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

			// The biggest clusters are the ones worth reviewing first

			comparator = Comparator.comparing(
				SimilarityCluster::getSize,
				Comparator.nullsLast(Comparator.reverseOrder()));
		}

		return comparator.thenComparing(
			this::_getMinId, Comparator.nullsLast(Comparator.naturalOrder()));
	}

	private List<SimilarityCluster> _getSimilarityClusters(
			List<List<Long>> clusters, String languageId,
			Map<Long, ObjectDefinition> objectDefinitionsMap,
			Map<Long, long[]> signaturesMap,
			SimilarityDimension similarityDimension)
		throws Exception {

		List<SimilarityCluster> similarityClusters = new ArrayList<>();

		for (List<Long> cluster : clusters) {
			Long topObjectEntryId = _getTopObjectEntryId(
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
				SimilarityClusterAsset similarityClusterAsset =
					new SimilarityClusterAsset();

				similarityClusterAsset.setId(() -> objectEntryId);

				boolean topAsset = objectEntryId.equals(topObjectEntryId);

				ObjectEntry objectEntry =
					_objectEntryLocalService.fetchObjectEntry(objectEntryId);

				if (objectEntry != null) {
					String title = objectEntry.getTitleValue(languageId, true);

					titles.add(title);

					if (topAsset) {
						topTitle = title;
					}

					similarityClusterAsset.setDateModified(
						objectEntry::getModifiedDate);
					similarityClusterAsset.setTitle(() -> title);

					ObjectDefinition objectDefinition =
						objectDefinitionsMap.get(
							objectEntry.getObjectDefinitionId());

					if (objectDefinition != null) {
						similarityClusterAsset.setContentType(
							() -> objectDefinition.getLabel(languageId, true));
					}
				}

				similarityClusterAsset.setTopAsset(() -> topAsset);

				if (!topAsset && (topSignature != null)) {
					long[] signature = signaturesMap.get(objectEntryId);

					if (signature != null) {
						double similarityPercent = Math.round(
							_getSimilarity(signature, topSignature) * 100.0);

						similarityClusterAsset.setSimilarityPercent(
							() -> similarityPercent);
					}
				}

				similarityClusterAssets.add(similarityClusterAsset);
			}

			SimilarityClusterAsset[] similarityClusterAssetsArray =
				similarityClusterAssets.toArray(new SimilarityClusterAsset[0]);

			String title = similarityDimension.getTitle(titles, topTitle);

			SimilarityCluster similarityCluster = new SimilarityCluster();

			similarityCluster.setSimilarityClusterAssets(
				() -> similarityClusterAssetsArray);
			similarityCluster.setSize(
				() -> similarityClusterAssetsArray.length);
			similarityCluster.setTitle(() -> title);

			similarityClusters.add(similarityCluster);
		}

		return similarityClusters;
	}

	private String _getTokenPrefix(String languageId) {
		return languageId + StringPool.UNDERLINE;
	}

	private Long _getTopObjectEntryId(
		List<Long> cluster, Map<Long, long[]> signaturesMap) {

		Long topObjectEntryId = null;

		double topMeanSimilarity = -1;

		for (Long objectEntryId : cluster) {
			long[] signature = signaturesMap.get(objectEntryId);

			if (signature == null) {
				continue;
			}

			double totalSimilarity = 0;
			int count = 0;

			for (Long otherObjectEntryId : cluster) {
				if (otherObjectEntryId.equals(objectEntryId)) {
					continue;
				}

				long[] otherSignature = signaturesMap.get(otherObjectEntryId);

				if (otherSignature == null) {
					continue;
				}

				totalSimilarity += _getSimilarity(signature, otherSignature);
				count++;
			}

			double meanSimilarity = 0;

			if (count > 0) {
				meanSimilarity = totalSimilarity / count;
			}

			if (meanSimilarity > topMeanSimilarity) {
				topMeanSimilarity = meanSimilarity;
				topObjectEntryId = objectEntryId;
			}
		}

		return topObjectEntryId;
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
		SimilarityClusterAsset similarityClusterAsset, String[] keywords) {

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

	private long[] _parseSignature(List<String> tokens, String languageId) {
		if (tokens == null) {
			return null;
		}

		long[] signature = new long[_SIGNATURE_SIZE];
		boolean[] filled = new boolean[_SIGNATURE_SIZE];
		int count = 0;

		String prefix = _getTokenPrefix(languageId);

		for (String curToken : tokens) {

			// One document carries the signatures of all its translations, so

			// only the request language's tokens are read

			if (!curToken.startsWith(prefix)) {
				continue;
			}

			String token = curToken.substring(prefix.length());

			int index = token.indexOf('_');

			if ((index <= 1) || (token.charAt(0) != 'p')) {
				continue;
			}

			int position = GetterUtil.getInteger(token.substring(1, index));

			if ((position < 0) || (position >= _SIGNATURE_SIZE) ||
				filled[position]) {

				continue;
			}

			signature[position] = GetterUtil.getLong(
				token.substring(index + 1));
			filled[position] = true;

			count++;
		}

		if (count != _SIGNATURE_SIZE) {
			return null;
		}

		return signature;
	}

	private List<Document> _searchClusteredDocuments(
		String bandField, List<String> sharedBands, String[] entryClassNames,
		Long[] groupIds) {

		List<Document> documents = new ArrayList<>();

		if (sharedBands.isEmpty()) {
			return documents;
		}

		TermsQuery termsQuery = QueriesUtil.terms(bandField);

		termsQuery.addValues(sharedBands.toArray());

		SearchRequestBuilder searchRequestBuilder =
			_searchRequestBuilderFactory.builder();

		long[] scopedGroupIds = _toPrimitiveArray(groupIds);

		searchRequestBuilder.addComplexQueryPart(
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
			new String[] {"objectEntryId", bandField}
		).size(
			_MAX_CLUSTERED_ASSETS
		).withSearchContext(
			searchContext -> {
				searchContext.setAttribute(
					Field.STATUS, WorkflowConstants.STATUS_APPROVED);
				searchContext.setGroupIds(scopedGroupIds);
			}
		);

		SearchResponse searchResponse = _searcher.search(
			searchRequestBuilder.build());

		SearchHits searchHits = searchResponse.getSearchHits();

		for (SearchHit searchHit : searchHits.getSearchHits()) {
			documents.add(searchHit.getDocument());
		}

		return documents;
	}

	private List<String> _searchSharedBands(
		String bandField, String[] entryClassNames, Long[] groupIds,
		String languageId) {

		// A flat aggregation, because a nested one creates a bucket per member

		// too and blows past Elasticsearch's own bucket ceiling once a space

		// holds a few thousand near-duplicates

		TermsAggregation termsAggregation = _aggregations.terms(
			_BANDS_AGGREGATION_NAME, bandField);

		termsAggregation.setMinDocCount(2);

		// Only the request language's bands form buckets, so content is never

		// grouped with a different translation

		termsAggregation.setIncludeExcludeClause(
			new IncludeExcludeClauseImpl(
				_getTokenPrefix(languageId) + ".*", null));

		termsAggregation.setSize(_MAX_BANDS);

		SearchRequestBuilder searchRequestBuilder =
			_searchRequestBuilderFactory.builder();

		long[] scopedGroupIds = _toPrimitiveArray(groupIds);

		searchRequestBuilder.addAggregation(
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
			searchContext -> {
				searchContext.setAttribute(
					Field.STATUS, WorkflowConstants.STATUS_APPROVED);
				searchContext.setGroupIds(scopedGroupIds);
			}
		);

		SearchResponse searchResponse = _searcher.search(
			searchRequestBuilder.build());

		TermsAggregationResult termsAggregationResult =
			(TermsAggregationResult)searchResponse.getAggregationResult(
				_BANDS_AGGREGATION_NAME);

		List<String> sharedBands = new ArrayList<>();

		if (termsAggregationResult == null) {
			return sharedBands;
		}

		for (Bucket bucket : termsAggregationResult.getBuckets()) {
			sharedBands.add(bucket.getKey());
		}

		return sharedBands;
	}

	private void _setItemURLs(List<SimilarityCluster> similarityClusters)
		throws Exception {

		// The edit URL costs a permission check per asset, so it is only built
		// for the assets the response carries, not for every clustered asset

		for (SimilarityCluster similarityCluster : similarityClusters) {
			for (SimilarityClusterAsset similarityClusterAsset :
					similarityCluster.getSimilarityClusterAssets()) {

				ObjectEntry objectEntry =
					_objectEntryLocalService.fetchObjectEntry(
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

	private long[] _toPrimitiveArray(Long[] groupIds) {
		long[] scopedGroupIds = new long[groupIds.length];

		for (int i = 0; i < groupIds.length; i++) {
			scopedGroupIds[i] = groupIds[i];
		}

		return scopedGroupIds;
	}

	private SimilarityClusterResult _toSimilarityClusterResult(
		List<SimilarityCluster> similarityClusters, long totalCount) {

		SimilarityCluster[] similarityClustersArray =
			similarityClusters.toArray(new SimilarityCluster[0]);

		SimilarityClusterResult similarityClusterResult =
			new SimilarityClusterResult();

		similarityClusterResult.setSimilarityClusters(
			() -> similarityClustersArray);
		similarityClusterResult.setTotalCount(() -> totalCount);

		return similarityClusterResult;
	}

	private void _union(
		Map<Long, Long> parents, Long objectEntryId1, Long objectEntryId2) {

		Long root1 = _find(parents, objectEntryId1);
		Long root2 = _find(parents, objectEntryId2);

		if (!root1.equals(root2)) {
			parents.put(root1, root2);
		}
	}

	private static final String _BANDS_AGGREGATION_NAME = "bands";

	private static final String _DATE_MODIFIED_FIELD_NAME = "dateModified";

	private static final int _MAX_BANDS = 10000;

	private static final int _MAX_CLUSTERED_ASSETS = 10000;

	private static final int _SIGNATURE_SIZE = 128;

	private static final String _TITLE_FIELD_NAME = "title";

	// The listing is sorted in memory, over the already resolved assets, so the
	// sortable field names are the asset's own field names

	private static final EntityModel _entityModel =
		() -> EntityModel.toEntityFieldsMap(
			new DateTimeEntityField(
				_DATE_MODIFIED_FIELD_NAME, locale -> _DATE_MODIFIED_FIELD_NAME,
				locale -> _DATE_MODIFIED_FIELD_NAME),
			new StringEntityField(
				_TITLE_FIELD_NAME, locale -> _TITLE_FIELD_NAME));

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