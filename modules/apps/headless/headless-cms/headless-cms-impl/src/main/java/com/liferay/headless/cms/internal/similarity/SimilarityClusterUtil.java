/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.cms.internal.similarity;

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
 * Groups near-duplicate assets from what the index holds, and answers the
 * signature questions a group raises: which of its members is the most
 * representative, and how close each of the others is to it.
 *
 * @author Mikel Lorza
 */
public class SimilarityClusterUtil {

	/**
	 * Returns the assets that share at least one band, grouped so that two
	 * assets are in the same group when a chain of shared bands connects them,
	 * dropping any group of a single asset.
	 */
	public static List<List<Long>> getClusters(
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
			List<Long> cluster = clusters.computeIfAbsent(
				_find(parents, objectEntryId), root -> new ArrayList<>());

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
	 * Returns the signature the given tokens hold for the given language, or
	 * <code>null</code> when they do not hold a whole one, so that an asset
	 * whose signature cannot be read is left without a similarity rather than
	 * given a wrong one.
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
	 * Returns the estimated fraction of text the two signatures share, which is
	 * how often they agree position by position.
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
	 * Returns the member of the cluster closest to all the others, which is the
	 * one the cluster is presented and measured against. Returns
	 * <code>null</code> when no member has a readable signature.
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

	private static final int _SIGNATURE_SIZE = 128;

}