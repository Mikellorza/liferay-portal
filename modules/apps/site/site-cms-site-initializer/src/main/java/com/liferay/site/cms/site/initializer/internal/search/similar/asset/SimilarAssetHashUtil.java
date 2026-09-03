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
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * Reduces a CMS content to the hashes two near duplicates literally share, so
 * that an index which only matches equal values can answer which contents are
 * similar.
 *
 * <p>
 * The text is cut into sequences of three consecutive keywords, each hashed,
 * and the set is condensed into one fixed length fingerprint estimating how
 * much two sets overlap. The fingerprint is then split into 32 hashes: two
 * contents that share one are candidates, and how many they share is what
 * decides whether they are grouped.
 * </p>
 *
 * @author Mikel Lorza
 */
public class SimilarAssetHashUtil {

	public static String[] getHashes(String text) {
		if (text == null) {
			return new String[0];
		}

		Set<String> keywordSequences = _getKeywordSequences(text);

		if (keywordSequences.isEmpty()) {
			return new String[0];
		}

		byte[] similarityHash = _similarityHashPolicy.createHasher(
		).compute(
			ElementHashProvider.ofCollection(
				keywordSequences, _hasher64::hashCharsToLong)
		);

		String[] hashes = new String[_HASHES];

		for (int i = 0; i < _HASHES; i++) {
			StringBundler sb = new StringBundler((_SAMPLES_PER_HASH * 2) + 2);

			sb.append("b");
			sb.append(i);

			for (int j = 0; j < _SAMPLES_PER_HASH; j++) {
				sb.append(StringPool.UNDERLINE);
				sb.append(
					_similarityHashPolicy.getComponent(
						similarityHash, (i * _SAMPLES_PER_HASH) + j));
			}

			hashes[i] = sb.toString();
		}

		return hashes;
	}

	private static Set<String> _getKeywordSequences(String text) {
		Set<String> keywordSequences = new HashSet<>();

		String[] words = StringUtil.toLowerCase(
			text, LocaleUtil.ENGLISH
		).replaceAll(
			"[^\\p{L}\\p{Nd}]+", " "
		).trim(
		).split(
			"\\s+"
		);

		if ((words.length == 1) && words[0].isEmpty()) {
			return keywordSequences;
		}

		if (words.length < _KEYWORD_SEQUENCE_SIZE) {
			for (String word : words) {
				keywordSequences.add(word);
			}

			return keywordSequences;
		}

		for (int i = 0; i <= (words.length - _KEYWORD_SEQUENCE_SIZE); i++) {
			StringBundler sb = new StringBundler();

			for (int j = 0; j < _KEYWORD_SEQUENCE_SIZE; j++) {
				if (j > 0) {
					sb.append(StringPool.SPACE);
				}

				sb.append(words[i + j]);
			}

			keywordSequences.add(sb.toString());
		}

		return keywordSequences;
	}

	private static final int _BITS_PER_SAMPLE = 8;

	private static final int _HASHES = 32;

	private static final int _KEYWORD_SEQUENCE_SIZE = 3;

	private static final int _SAMPLES_PER_HASH = 4;

	private static final Hasher64 _hasher64 = Hashing.murmur3_128();
	private static final SimilarityHashPolicy _similarityHashPolicy =
		SimilarityHashing.superMinHash(
			_HASHES * _SAMPLES_PER_HASH, _BITS_PER_SAMPLE,
			SuperMinHashVersion.V1);

}