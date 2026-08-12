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
 * Groups near duplicate assets from what the index holds, and answers the
 * signature questions a group raises: which member is the most representative,
 * and how close each of the others is to it.
 *
 * @author Mikel Lorza
 */
public class SimilarityClusterUtil {

	public static final String FIELD_NAME_OBJECT_ENTRY_ID = "objectEntryId";

	/**
	 * Groups the assets a chain of shared bands connects, dropping any group of
	 * a single asset.
	 *
	 * <p>
	 * Two assets are connected only once they share {@link #_MIN_SHARED_BANDS}
	 * bands, not one. One band is enough evidence for a pair and nowhere near
	 * enough for a corpus: grouping is transitive, so with a rate <code>p</code>
	 * of unrelated pairs sharing a band, a space of <code>N</code> assets grows
	 * about <code>p * N * N / 2</code> accidental connections, and the moment
	 * that averages more than one per asset every group merges into one. The
	 * distribution is what makes the threshold work — measured over a million
	 * unrelated title pairs, 0.54% share one band but 0.008% share three, while
	 * a title and the same title with a typo share at least four. So three
	 * carries the near duplicates and moves the collapse from about 180 assets
	 * to about 12,000.
	 * </p>
	 */
	public static List<List<Long>> getClusters(
		List<Document> documents, String bandField, Set<String> sharedBands) {

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
				_find(parents, objectEntryId), root -> new ArrayList<>());

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

	/**
	 * Returns <code>null</code> unless the tokens hold a whole signature, so an
	 * asset is left without a similarity rather than given a wrong one.
	 */
	public static long[] getSignature(List<String> tokens, String languageId) {
		if (tokens == null) {
			return null;
		}

		long[] signature = new long[_SIGNATURE_SIZE];
		boolean[] filled = new boolean[_SIGNATURE_SIZE];
		int count = 0;

		String prefix = getTokenPrefix(languageId);

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

	/**
	 * Returns the estimated fraction of text the two signatures share.
	 */
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

	/**
	 * Returns the member closest to all the others, or <code>null</code> when
	 * none has a readable signature.
	 */
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

	private static Long _find(Map<Long, Long> parents, Long objectEntryId) {
		Long parent = parents.get(objectEntryId);

		while (!parent.equals(objectEntryId)) {
			objectEntryId = parent;

			parent = parents.get(objectEntryId);
		}

		return objectEntryId;
	}

	private static void _union(
		Map<Long, Long> parents, Long objectEntryId1, Long objectEntryId2) {

		Long root1 = _find(parents, objectEntryId1);
		Long root2 = _find(parents, objectEntryId2);

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

					// Assets already connected need no further evidence, which
					// is what keeps a band shared by hundreds of identical
					// assets from counting every one of its pairs again for
					// every band they have in common

					Long root1 = _find(parents, objectEntryId1);
					Long root2 = _find(parents, objectEntryId2);

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
						_union(parents, objectEntryId1, objectEntryId2);
					}
				}
			}
		}
	}

	private static final int _MIN_SHARED_BANDS = 3;

	private static final int _SIGNATURE_SIZE = 128;

}