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
 * @author Mikel Lorza
 */
public class SimilarAssetUtil {

	public static String[] getSimilarAssets(String text) {
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

	private static final int _KEYWORD_SEQUENCE_SIZE = 3;

	private static final int _SAMPLES_PER_SIMILAR_ASSET = 4;

	private static final int _SIMILAR_ASSETS = 32;

	private static final Hasher64 _hasher64 = Hashing.murmur3_128();
	private static final SimilarityHashPolicy _similarityHashPolicy =
		SimilarityHashing.superMinHash(
			_SIMILAR_ASSETS * _SAMPLES_PER_SIMILAR_ASSET, _BITS_PER_SAMPLE,
			SuperMinHashVersion.V1);

}