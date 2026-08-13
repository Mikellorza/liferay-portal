/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.cms.internal.similarity;

import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.search.document.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Mikel Lorza
 */
public class SimilarityClusterUtil {

	public static final String FIELD_NAME_OBJECT_ENTRY_ID = "objectEntryId";

	public static List<List<Long>> getClusters(
		String bandField, List<Document> documents, Set<String> sharedBands) {

		Map<Long, Long> parents = new LinkedHashMap<>();
		Map<String, List<Long>> objectEntryIdsByBand = new HashMap<>();

		for (Document document : documents) {
			Long objectEntryId = document.getLong(FIELD_NAME_OBJECT_ENTRY_ID);

			if (objectEntryId == null) {
				continue;
			}

			parents.putIfAbsent(objectEntryId, objectEntryId);

			for (String band : document.getStrings(bandField)) {
				if (!sharedBands.contains(band)) {
					continue;
				}

				List<Long> bandObjectEntryIds =
					objectEntryIdsByBand.computeIfAbsent(
						band, key -> new ArrayList<>());

				bandObjectEntryIds.add(objectEntryId);
			}
		}

		_union(objectEntryIdsByBand, parents);

		Map<Long, List<Long>> clusters = new LinkedHashMap<>();

		for (Long objectEntryId : parents.keySet()) {
			List<Long> cluster = clusters.computeIfAbsent(
				_find(objectEntryId, parents), root -> new ArrayList<>());

			cluster.add(objectEntryId);
		}

		return TransformUtil.transform(
			clusters.values(),
			cluster -> {
				if (cluster.size() < 2) {
					return null;
				}

				return cluster;
			});
	}

	public static Long getMinObjectEntryId(List<Long> cluster) {
		Long minObjectEntryId = null;

		for (Long objectEntryId : cluster) {
			if ((minObjectEntryId == null) ||
				(objectEntryId < minObjectEntryId)) {

				minObjectEntryId = objectEntryId;
			}
		}

		return minObjectEntryId;
	}

	public static long[] getSignature(String languageId, List<String> tokens) {
		if (tokens == null) {
			return null;
		}

		boolean[] filled = new boolean[_SIGNATURE_SIZE];
		long[] signature = new long[_SIGNATURE_SIZE];

		int count = 0;
		String prefix = getTokenPrefix(languageId);

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

	public static String getTokenPrefix(String languageId) {
		return languageId + StringPool.UNDERLINE;
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

	private static void _union(
		Long objectEntryId1, Long objectEntryId2, Map<Long, Long> parents) {

		Long root1 = _find(objectEntryId1, parents);
		Long root2 = _find(objectEntryId2, parents);

		if (!root1.equals(root2)) {
			parents.put(root1, root2);
		}
	}

	private static void _union(
		Map<String, List<Long>> objectEntryIdsByBand, Map<Long, Long> parents) {

		Map<Long, Map<Long, Integer>> sharedBandCounts = new HashMap<>();

		for (List<Long> objectEntryIds : objectEntryIdsByBand.values()) {
			for (int i = 0; i < objectEntryIds.size(); i++) {
				for (int j = i + 1; j < objectEntryIds.size(); j++) {
					Long objectEntryId1 = objectEntryIds.get(i);
					Long objectEntryId2 = objectEntryIds.get(j);

					// Assets already connected need no further evidence

					Long root1 = _find(objectEntryId1, parents);
					Long root2 = _find(objectEntryId2, parents);

					if (root1.equals(root2)) {
						continue;
					}

					Map<Long, Integer> counts =
						sharedBandCounts.computeIfAbsent(
							Math.min(objectEntryId1, objectEntryId2),
							key -> new HashMap<>());

					int count = counts.merge(
						Math.max(objectEntryId1, objectEntryId2), 1,
						Integer::sum);

					if (count >= _MIN_SHARED_BANDS) {
						_union(objectEntryId1, objectEntryId2, parents);
					}
				}
			}
		}
	}

	// Grouping is transitive, so one shared band, which 0.54% of unrelated
	// pairs reach, collapses a space of a few hundred assets into one cluster.
	// Three is reached by 0.008% of unrelated pairs and by every near duplicate

	private static final int _MIN_SHARED_BANDS = 3;

	private static final int _SIGNATURE_SIZE = 128;

}