/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.cms.internal.usage;

import com.liferay.headless.cms.internal.util.CMSSearchUtil;
import com.liferay.layout.model.LayoutClassedModelUsageTable;
import com.liferay.object.constants.ObjectDefinitionConstants;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.sql.dsl.DSLQueryFactoryUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.ClassNameTable;
import com.liferay.portal.kernel.search.BooleanClause;
import com.liferay.portal.kernel.search.BooleanClauseOccur;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.filter.BooleanFilter;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.search.filter.QueryFilter;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.util.UnicodePropertiesBuilder;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.search.aggregation.Aggregations;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.QueriesUtil;
import com.liferay.portal.search.query.Query;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.searcher.Searcher;
import com.liferay.portal.vulcan.pagination.Pagination;
import com.liferay.site.cms.site.initializer.util.CMSOutboundLinksUtil;
import com.liferay.site.navigation.model.SiteNavigationMenuItem;
import com.liferay.site.navigation.model.SiteNavigationMenuItemTable;
import com.liferay.site.navigation.service.SiteNavigationMenuItemLocalService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Mikel Lorza
 */
public class UnlinkedAssetSearcher {

	public UnlinkedAssetSearcher(
		Aggregations aggregations,
		ObjectEntryLocalService objectEntryLocalService, Searcher searcher,
		SearchRequestBuilderFactory searchRequestBuilderFactory,
		SiteNavigationMenuItemLocalService siteNavigationMenuItemLocalService) {

		_aggregations = aggregations;
		_objectEntryLocalService = objectEntryLocalService;
		_searcher = searcher;
		_searchRequestBuilderFactory = searchRequestBuilderFactory;
		_siteNavigationMenuItemLocalService =
			siteNavigationMenuItemLocalService;
	}

	public long getCount(
		CollectionUsages collectionUsages, long companyId, Long[] groupIds) {

		SearchResponse searchResponse = _searcher.search(
			_getSearchRequestBuilder(
				collectionUsages, companyId, groupIds
			).size(
				0
			).build());

		return searchResponse.getCount();
	}

	public SearchResponse search(
		CollectionUsages collectionUsages, long companyId, Long[] groupIds,
		String languageId, Pagination pagination, String search, Sort[] sorts) {

		SearchRequestBuilder searchRequestBuilder = _getSearchRequestBuilder(
			collectionUsages, companyId, groupIds);

		searchRequestBuilder.addSelectedFieldNames(
			Field.ENTRY_CLASS_PK, "objectDefinitionId", "objectEntryTitle",
			Field.getLocalizedName(languageId, "objectEntryTitle")
		).from(
			pagination.getStartPosition()
		).size(
			pagination.getPageSize()
		);

		if (ArrayUtil.isNotEmpty(sorts)) {
			searchRequestBuilder.withSearchContext(
				searchContext -> searchContext.setSorts(sorts));
		}

		if (search != null) {
			searchRequestBuilder.queryString(search);
		}

		return _searcher.search(searchRequestBuilder.build());
	}

	private void _addLayoutObjectEntryIds(
		long companyId, Set<String> objectEntryIds) {

		List<Long> classPKs = _objectEntryLocalService.dslQuery(
			DSLQueryFactoryUtil.selectDistinct(
				LayoutClassedModelUsageTable.INSTANCE.classPK
			).from(
				LayoutClassedModelUsageTable.INSTANCE
			).where(
				LayoutClassedModelUsageTable.INSTANCE.companyId.eq(
					companyId
				).and(
					LayoutClassedModelUsageTable.INSTANCE.classNameId.in(
						DSLQueryFactoryUtil.select(
							ClassNameTable.INSTANCE.classNameId
						).from(
							ClassNameTable.INSTANCE
						).where(
							ClassNameTable.INSTANCE.value.like(
								ObjectDefinitionConstants.
									CLASS_NAME_PREFIX_CUSTOM_OBJECT_DEFINITION +
										StringPool.PERCENT)
						))
				)
			));

		objectEntryIds.addAll(
			TransformUtil.transform(classPKs, String::valueOf));
	}

	private void _addNavigationMenuExternalReferenceCodes(
		long companyId, Set<String> externalReferenceCodes) {

		List<SiteNavigationMenuItem> siteNavigationMenuItems =
			_siteNavigationMenuItemLocalService.dslQuery(
				DSLQueryFactoryUtil.select(
					SiteNavigationMenuItemTable.INSTANCE
				).from(
					SiteNavigationMenuItemTable.INSTANCE
				).where(
					SiteNavigationMenuItemTable.INSTANCE.companyId.eq(
						companyId
					).and(
						SiteNavigationMenuItemTable.INSTANCE.type.like(
							ObjectDefinitionConstants.
								CLASS_NAME_PREFIX_CUSTOM_OBJECT_DEFINITION +
									StringPool.PERCENT)
					)
				));

		for (SiteNavigationMenuItem siteNavigationMenuItem :
				siteNavigationMenuItems) {

			UnicodeProperties unicodeProperties =
				UnicodePropertiesBuilder.create(
					true
				).fastLoad(
					siteNavigationMenuItem.getTypeSettings()
				).build();

			String externalReferenceCode = unicodeProperties.getProperty(
				"externalReferenceCode");

			if (Validator.isNull(externalReferenceCode)) {
				continue;
			}

			externalReferenceCodes.add(externalReferenceCode);
		}
	}

	private void _addOutboundLinks(
		long companyId, String outboundLinkPrefix, Set<String> outboundLinks) {

		CMSSearchUtil.addBucketKeys(
			includeExcludeClause -> CMSSearchUtil.getTermsAggregationResult(
				_aggregations, "outboundLinks", includeExcludeClause, _searcher,
				_getSearchRequestBuilder(
					CMSSearchUtil.getReferrersBooleanQuery(), companyId),
				10000),
			outboundLinks, outboundLinkPrefix);
	}

	private BooleanQuery _getBaseBooleanQuery() {
		BooleanQuery booleanQuery = QueriesUtil.booleanQuery();

		booleanQuery.addFilterQueryClauses(
			CMSSearchUtil.getTermsQuery("cms_section", "contents", "files"),
			QueriesUtil.term(Field.STATUS, WorkflowConstants.STATUS_APPROVED),
			QueriesUtil.term("rootDescendantNode", false));

		return booleanQuery;
	}

	private List<com.liferay.portal.kernel.search.BooleanQuery> _getBlocks(
		Collection<com.liferay.portal.kernel.search.BooleanQuery>
			booleanQueries) {

		List<com.liferay.portal.kernel.search.BooleanQuery> blocks =
			new ArrayList<>();

		com.liferay.portal.kernel.search.BooleanQuery block = null;
		int count = 0;

		for (com.liferay.portal.kernel.search.BooleanQuery booleanQuery :
				booleanQueries) {

			int clausesCount = _getClausesCount(booleanQuery);

			if ((block == null) || ((count + clausesCount) > 1024)) {
				block = new com.liferay.portal.kernel.search.BooleanQuery();
				count = 1;

				blocks.add(block);
			}

			block.add(booleanQuery, BooleanClauseOccur.SHOULD);

			count += clausesCount;
		}

		return blocks;
	}

	private Set<String> _getCandidateObjectEntryIds(
		Collection<com.liferay.portal.kernel.search.BooleanQuery>
			booleanQueries,
		long companyId, Long[] groupIds) {

		Set<String> candidateObjectEntryIds = null;

		for (com.liferay.portal.kernel.search.BooleanQuery block :
				_getBlocks(booleanQueries)) {

			Set<String> entryClassPKs = CMSSearchUtil.getEntryClassPKs(
				_aggregations, _searcher,
				() -> _getSearchRequestBuilder(
					_getBaseBooleanQuery(), companyId
				).groupIds(
					ArrayUtil.toArray(groupIds)
				).withSearchContext(
					searchContext -> searchContext.setBooleanClauses(
						new BooleanClause[] {
							new BooleanClause<>(
								block, BooleanClauseOccur.MUST_NOT)
						})
				));

			if (candidateObjectEntryIds == null) {
				candidateObjectEntryIds = entryClassPKs;
			}
			else {
				candidateObjectEntryIds.retainAll(entryClassPKs);
			}

			if (candidateObjectEntryIds.isEmpty()) {
				break;
			}
		}

		return candidateObjectEntryIds;
	}

	private int _getClausesCount(Filter filter) {
		if (filter instanceof BooleanFilter) {
			BooleanFilter booleanFilter = (BooleanFilter)filter;

			return 1 + _getClausesCount(booleanFilter.getMustBooleanClauses()) +
				_getClausesCount(booleanFilter.getMustNotBooleanClauses()) +
					_getClausesCount(booleanFilter.getShouldBooleanClauses());
		}

		if (filter instanceof QueryFilter) {
			QueryFilter queryFilter = (QueryFilter)filter;

			return _getClausesCount(queryFilter.getQuery());
		}

		return 1;
	}

	private int _getClausesCount(List<BooleanClause<Filter>> booleanClauses) {
		int count = 0;

		for (BooleanClause<Filter> booleanClause : booleanClauses) {
			count += _getClausesCount(booleanClause.getClause());
		}

		return count;
	}

	private int _getClausesCount(com.liferay.portal.kernel.search.Query query) {
		int count = 1;

		if (query instanceof com.liferay.portal.kernel.search.BooleanQuery) {
			com.liferay.portal.kernel.search.BooleanQuery booleanQuery =
				(com.liferay.portal.kernel.search.BooleanQuery)query;

			for (BooleanClause<com.liferay.portal.kernel.search.Query>
					booleanClause : booleanQuery.clauses()) {

				count += _getClausesCount(booleanClause.getClause());
			}
		}

		if (query.getPreBooleanFilter() != null) {
			count += _getClausesCount(query.getPreBooleanFilter());
		}

		return count;
	}

	private SearchRequestBuilder _getSearchRequestBuilder(
		BooleanQuery booleanQuery, long companyId) {

		return _searchRequestBuilderFactory.builder(
		).companyId(
			companyId
		).emptySearchEnabled(
			true
		).query(
			booleanQuery
		).withSearchContext(
			searchContext -> searchContext.setAttribute(
				Field.STATUS, WorkflowConstants.STATUS_ANY)
		);
	}

	private SearchRequestBuilder _getSearchRequestBuilder(
		CollectionUsages collectionUsages, long companyId, Long[] groupIds) {

		Set<String> objectEntryIds = new HashSet<>(
			collectionUsages.getObjectEntryIds());

		_addLayoutObjectEntryIds(companyId, objectEntryIds);

		Set<String> externalReferenceCodes = new HashSet<>();

		_addNavigationMenuExternalReferenceCodes(
			companyId, externalReferenceCodes);

		Set<String> outboundLinks = new HashSet<>();

		_addOutboundLinks(
			companyId, _OBJECT_ENTRY_EXTERNAL_REFERENCE_CODE_TOKEN_PREFIX,
			outboundLinks);
		_addOutboundLinks(
			companyId, _OBJECT_ENTRY_ID_TOKEN_PREFIX, outboundLinks);

		for (String outboundLink : outboundLinks) {
			if (outboundLink.startsWith(
					_OBJECT_ENTRY_EXTERNAL_REFERENCE_CODE_TOKEN_PREFIX)) {

				externalReferenceCodes.add(
					outboundLink.substring(
						_OBJECT_ENTRY_EXTERNAL_REFERENCE_CODE_TOKEN_PREFIX.
							length()));
			}
			else if (outboundLink.startsWith(_OBJECT_ENTRY_ID_TOKEN_PREFIX)) {
				objectEntryIds.add(
					outboundLink.substring(
						_OBJECT_ENTRY_ID_TOKEN_PREFIX.length()));
			}
		}

		BooleanQuery booleanQuery = _getBaseBooleanQuery();

		List<Query> queries = collectionUsages.getQueries();

		if (!queries.isEmpty()) {
			BooleanQuery collectionsBooleanQuery = QueriesUtil.booleanQuery();

			collectionsBooleanQuery.addShouldQueryClauses(
				queries.toArray(new Query[0]));

			booleanQuery.addMustNotQueryClauses(collectionsBooleanQuery);
		}

		Set<Long> booleanQueriesGroupIds = new HashSet<>(
			collectionUsages.getBooleanQueriesGroupIds());

		if (collectionUsages.isBooleanQueriesAnyGroup()) {
			Collections.addAll(booleanQueriesGroupIds, groupIds);
		}

		booleanQueriesGroupIds.retainAll(Arrays.asList(groupIds));

		if (!booleanQueriesGroupIds.isEmpty()) {
			Set<String> candidateObjectEntryIds = _getCandidateObjectEntryIds(
				collectionUsages.getBooleanQueries(), companyId,
				booleanQueriesGroupIds.toArray(new Long[0]));

			BooleanQuery candidatesBooleanQuery = QueriesUtil.booleanQuery();

			candidatesBooleanQuery.addShouldQueryClauses(
				CMSSearchUtil.getTermsQuery(
					Field.ENTRY_CLASS_PK,
					candidateObjectEntryIds.toArray(new String[0])));

			BooleanQuery otherGroupsBooleanQuery = QueriesUtil.booleanQuery();

			otherGroupsBooleanQuery.addMustNotQueryClauses(
				CMSSearchUtil.getTermsQuery(
					Field.GROUP_ID,
					ArrayUtil.toStringArray(booleanQueriesGroupIds)));

			candidatesBooleanQuery.addShouldQueryClauses(
				otherGroupsBooleanQuery);

			booleanQuery.addFilterQueryClauses(candidatesBooleanQuery);
		}

		booleanQuery.addMustNotQueryClauses(
			CMSSearchUtil.getTermsQuery(
				Field.ENTRY_CLASS_PK, objectEntryIds.toArray(new String[0])),
			CMSSearchUtil.getTermsQuery(
				"externalReferenceCode",
				externalReferenceCodes.toArray(new String[0])));

		return _getSearchRequestBuilder(
			booleanQuery, companyId
		).groupIds(
			ArrayUtil.toArray(groupIds)
		);
	}

	private static final String
		_OBJECT_ENTRY_EXTERNAL_REFERENCE_CODE_TOKEN_PREFIX =
			CMSOutboundLinksUtil.getObjectEntryExternalReferenceCodeToken(
				StringPool.BLANK);

	private static final String _OBJECT_ENTRY_ID_TOKEN_PREFIX =
		StringUtil.replaceLast(
			CMSOutboundLinksUtil.getObjectEntryIdToken(0), '0',
			StringPool.BLANK);

	private final Aggregations _aggregations;
	private final ObjectEntryLocalService _objectEntryLocalService;
	private final SearchRequestBuilderFactory _searchRequestBuilderFactory;
	private final Searcher _searcher;
	private final SiteNavigationMenuItemLocalService
		_siteNavigationMenuItemLocalService;

}