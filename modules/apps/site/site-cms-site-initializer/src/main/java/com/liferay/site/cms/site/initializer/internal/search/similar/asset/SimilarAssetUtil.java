/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.search.similar.asset;

import com.dynatrace.hash4j.hashing.Hasher64;
import com.dynatrace.hash4j.hashing.Hashing;
import com.dynatrace.hash4j.similarity.ElementHashProvider;
import com.dynatrace.hash4j.similarity.SimilarityHashPolicy;
import com.dynatrace.hash4j.similarity.SimilarityHashing;
import com.dynatrace.hash4j.similarity.SuperMinHashVersion;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;

import java.util.Set;

/**
 * Reduces the set of elements a dimension compares content by to the tokens
 * two near duplicates literally share, so that an index that only matches
 * equal values can answer which contents are similar.
 *
 * <p>
 * What the elements are is the caller's decision, and it is the one thing that
 * differs per dimension: sequences of keywords for prose, character n grams
 * for titles, categories and tags for metadata.
 * </p>
 *
 * @author Mikel Lorza
 */
public class SimilarAssetUtil {

	/**
	 * Returns an empty array for an empty set, so content that yields no
	 * element stays out of grouping rather than grouping with everything else
	 * that also yields none.
	 */
	public static String[] getSimilarAssets(Set<String> elements) {
		if ((elements == null) || elements.isEmpty()) {
			return new String[0];
		}

		byte[] similarityHash = _similarityHashPolicy.createHasher(
		).compute(
			ElementHashProvider.ofCollection(
				elements, _hasher64::hashCharsToLong)
		);

		String[] similarAssets = new String[_SIMILAR_ASSETS];

		for (int i = 0; i < _SIMILAR_ASSETS; i++) {
			StringBundler sb = new StringBundler(
				(_SAMPLES_PER_SIMILAR_ASSET * 2) + 2);

			sb.append("b");
			sb.append(i);

			for (int j = 0; j < _SAMPLES_PER_SIMILAR_ASSET; j++) {
				sb.append(StringPool.UNDERLINE);
				sb.append(
					_similarityHashPolicy.getComponent(
						similarityHash, (i * _SAMPLES_PER_SIMILAR_ASSET) + j));
			}

			similarAssets[i] = sb.toString();
		}

		return similarAssets;
	}

	private static final int _BITS_PER_SAMPLE = 8;

	private static final int _SAMPLES_PER_SIMILAR_ASSET = 4;

	private static final int _SIMILAR_ASSETS = 32;

	private static final Hasher64 _hasher64 = Hashing.murmur3_128();
	private static final SimilarityHashPolicy _similarityHashPolicy =
		SimilarityHashing.superMinHash(
			_SIMILAR_ASSETS * _SAMPLES_PER_SIMILAR_ASSET, _BITS_PER_SAMPLE,
			SuperMinHashVersion.V1);

}