/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.cms.internal.similarity;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.search.document.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Mikel Lorza
 */
public class SimilarityClusterUtil {

	public static Map<Long, List<Long>> getClusters(
		String similarityKeyField, List<Document> documents,
		Set<String> sharedSimilarityKeys) {

		Map<Long, Long> parents = new LinkedHashMap<>();
		Map<String, List<Long>> objectEntryIdsBySimilarityKey = new HashMap<>();

		for (Document document : documents) {
			Long objectEntryId = document.getLong("objectEntryId");

			if (objectEntryId == null) {
				continue;
			}

			parents.putIfAbsent(objectEntryId, objectEntryId);

			for (String similarityKey :
					document.getStrings(similarityKeyField)) {

				if (!sharedSimilarityKeys.contains(similarityKey)) {
					continue;
				}

				List<Long> similarityKeyObjectEntryIds =
					objectEntryIdsBySimilarityKey.computeIfAbsent(
						similarityKey, key -> new ArrayList<>());

				similarityKeyObjectEntryIds.add(objectEntryId);
			}
		}

		_union(objectEntryIdsBySimilarityKey, parents);

		return _getObjectEntryIdsByClusterId(parents);
	}

	public static long[] getSignature(String languageId, List<String> tokens) {
		if (tokens == null) {
			return null;
		}

		boolean[] filled = new boolean[_SIGNATURE_SIZE];
		long[] signature = new long[_SIGNATURE_SIZE];

		int count = 0;
		String prefix = _getTokenPrefix(languageId);

		for (String curToken : tokens) {
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

			filled[position] = true;
			signature[position] = GetterUtil.getLong(
				token.substring(index + 1));

			count++;
		}

		if (count != _SIGNATURE_SIZE) {
			return null;
		}

		return signature;
	}

	public static double getSimilarity(long[] signature1, long[] signature2) {
		int matches = 0;

		for (int i = 0; i < _SIGNATURE_SIZE; i++) {
			if (signature1[i] == signature2[i]) {
				matches++;
			}
		}

		return (double)matches / _SIGNATURE_SIZE;
	}

	public static Long getTopObjectEntryId(
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

				totalSimilarity += getSimilarity(signature, otherSignature);

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

	private static Long _find(Long objectEntryId, Map<Long, Long> parents) {
		Long parent = parents.get(objectEntryId);

		while (!parent.equals(objectEntryId)) {
			objectEntryId = parent;

			parent = parents.get(objectEntryId);
		}

		return objectEntryId;
	}

	private static Map<Long, List<Long>> _getObjectEntryIdsByClusterId(
		Map<Long, Long> parents) {

		Map<Long, List<Long>> objectEntryIdsByRoot = new LinkedHashMap<>();

		for (Long objectEntryId : parents.keySet()) {
			List<Long> rootObjectEntryIds =
				objectEntryIdsByRoot.computeIfAbsent(
					_find(objectEntryId, parents), root -> new ArrayList<>());

			rootObjectEntryIds.add(objectEntryId);
		}

		Map<Long, List<Long>> objectEntryIdsByClusterId = new HashMap<>();

		for (List<Long> objectEntryIds : objectEntryIdsByRoot.values()) {
			if (objectEntryIds.size() < 2) {
				continue;
			}

			Collections.sort(objectEntryIds);

			objectEntryIdsByClusterId.put(
				objectEntryIds.get(0), objectEntryIds);
		}

		return _getSortedObjectEntryIdsByClusterId(objectEntryIdsByClusterId);
	}

	private static Map<Long, List<Long>> _getSortedObjectEntryIdsByClusterId(
		Map<Long, List<Long>> objectEntryIdsByClusterId) {

		List<Long> clusterIds = ListUtil.fromMapKeys(objectEntryIdsByClusterId);

		clusterIds.sort(
			Comparator.comparingInt(
				(Long clusterId) -> {
					List<Long> objectEntryIds = objectEntryIdsByClusterId.get(
						clusterId);

					return objectEntryIds.size();
				}
			).reversed(
			).thenComparing(
				Comparator.naturalOrder()
			));

		Map<Long, List<Long>> sortedObjectEntryIdsByClusterId =
			new LinkedHashMap<>();

		for (Long clusterId : clusterIds) {
			sortedObjectEntryIdsByClusterId.put(
				clusterId, objectEntryIdsByClusterId.get(clusterId));
		}

		return sortedObjectEntryIdsByClusterId;
	}

	private static String _getTokenPrefix(String languageId) {
		return languageId + StringPool.UNDERLINE;
	}

	private static void _union(
		Long objectEntryId1, Long objectEntryId2, Map<Long, Long> parents) {

		Long root1 = _find(objectEntryId1, parents);
		Long root2 = _find(objectEntryId2, parents);

		if (!root1.equals(root2)) {
			parents.put(root1, root2);
		}
	}

	private static void _union(
		Map<String, List<Long>> objectEntryIdsBySimilarityKey,
		Map<Long, Long> parents) {

		Map<Long, Map<Long, Integer>> sharedSimilarityKeyCounts =
			new HashMap<>();

		for (List<Long> objectEntryIds :
				objectEntryIdsBySimilarityKey.values()) {

			for (int i = 0; i < objectEntryIds.size(); i++) {
				for (int j = i + 1; j < objectEntryIds.size(); j++) {
					Long objectEntryId1 = objectEntryIds.get(i);
					Long objectEntryId2 = objectEntryIds.get(j);

					Long root1 = _find(objectEntryId1, parents);
					Long root2 = _find(objectEntryId2, parents);

					if (root1.equals(root2)) {
						continue;
					}

					Map<Long, Integer> counts =
						sharedSimilarityKeyCounts.computeIfAbsent(
							Math.min(objectEntryId1, objectEntryId2),
							key -> new HashMap<>());

					int count = counts.merge(
						Math.max(objectEntryId1, objectEntryId2), 1,
						Integer::sum);

					if (count >= _MIN_SHARED_SIMILARITY_KEYS) {
						_union(objectEntryId1, objectEntryId2, parents);
					}
				}
			}
		}
	}

	private static final int _MIN_SHARED_SIMILARITY_KEYS = 3;

	private static final int _SIGNATURE_SIZE = 128;

}