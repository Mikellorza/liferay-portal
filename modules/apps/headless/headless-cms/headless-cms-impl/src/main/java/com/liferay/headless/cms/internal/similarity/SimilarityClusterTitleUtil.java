/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.cms.internal.similarity;

import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Names a similarity cluster after the titles of its assets: the longest phrase
 * they share, falling back to the words they all have in common and finally to
 * the representative asset's own title, so a cluster is never left unnamed.
 *
 * <p>
 * Only titles are used, because what two near duplicates share in their body is
 * usually boilerplate and the indexed signatures are not invertible.
 * </p>
 *
 * @author Mikel Lorza
 */
public class SimilarityClusterTitleUtil {

	/**
	 * The representative asset's <code>topTitle</code> sets the word order of
	 * the name. Blank titles carry no signal and are ignored.
	 */
	public static String getTitle(List<String> titles, String topTitle) {
		String representativeTitle = _getRepresentativeTitle(titles, topTitle);

		String[] representativeWords = _getWords(representativeTitle);

		List<String[]> wordsList = TransformUtil.transform(
			titles,
			curTitle -> {
				String[] words = _getWords(curTitle);

				if (ArrayUtil.isEmpty(words)) {
					return null;
				}

				return words;
			});

		String title = _getCommonPhrase(representativeWords, wordsList);

		if (title == null) {
			title = _getCommonWords(representativeWords, wordsList);
		}

		if (title == null) {
			title = representativeTitle;
		}

		return StringUtil.shorten(title, 60);
	}

	private static String _getCommonPhrase(
		String[] representativeWords, List<String[]> wordsList) {

		for (int length = representativeWords.length; length >= 2; length--) {
			int lastStart = representativeWords.length - length;

			for (int start = 0; start <= lastStart; start++) {
				String[] words = ArrayUtil.subset(
					representativeWords, start, start + length);

				if (_isStopWords(words) || !_isCommonWords(words, wordsList)) {
					continue;
				}

				return StringUtil.merge(
					_trimStopWords(words), StringPool.SPACE);
			}
		}

		return null;
	}

	private static String _getCommonWords(
		String[] representativeWords, List<String[]> wordsList) {

		Set<String> commonWords = new LinkedHashSet<>();

		for (String representativeWord : representativeWords) {
			String[] words = {representativeWord};

			if (_isStopWord(representativeWord) ||
				!_isCommonWords(words, wordsList)) {

				continue;
			}

			commonWords.add(representativeWord);
		}

		if (commonWords.isEmpty()) {
			return null;
		}

		return StringUtil.merge(commonWords, StringPool.SPACE);
	}

	private static String _getRepresentativeTitle(
		List<String> titles, String topTitle) {

		if (!Validator.isBlank(topTitle)) {
			return topTitle.trim();
		}

		for (String title : titles) {
			if (!Validator.isBlank(title)) {
				return title.trim();
			}
		}

		return StringPool.BLANK;
	}

	private static String[] _getWords(String title) {
		if (Validator.isBlank(title)) {
			return new String[0];
		}

		String string = title.replaceAll("[^\\p{L}\\p{Nd}]+", StringPool.SPACE);

		return StringUtil.split(string.trim(), ' ');
	}

	private static boolean _hasWords(String[] words, String[] candidateWords) {
		for (int i = 0; i <= (words.length - candidateWords.length); i++) {
			boolean matches = true;

			for (int j = 0; j < candidateWords.length; j++) {
				if (!StringUtil.equalsIgnoreCase(
						words[i + j], candidateWords[j])) {

					matches = false;

					break;
				}
			}

			if (matches) {
				return true;
			}
		}

		return false;
	}

	private static boolean _isCommonWords(
		String[] words, List<String[]> wordsList) {

		for (String[] curWords : wordsList) {
			if (!_hasWords(curWords, words)) {
				return false;
			}
		}

		return true;
	}

	private static boolean _isStopWord(String word) {
		return _stopWords.contains(StringUtil.toLowerCase(word));
	}

	private static boolean _isStopWords(String[] words) {
		for (String word : words) {
			if (!_isStopWord(word)) {
				return false;
			}
		}

		return true;
	}

	private static String[] _trimStopWords(String[] words) {
		int end = words.length;
		int start = 0;

		while ((start < end) && _isStopWord(words[start])) {
			start++;
		}

		while ((end > start) && _isStopWord(words[end - 1])) {
			end--;
		}

		return ArrayUtil.subset(words, start, end);
	}

	// The list is English only, in a feature whose premise is per language
	// comparison, so an English cluster name is trimmed of its function words
	// and every other language keeps them. The name is cosmetic, it never
	// decides what groups with what, so the cost is a longer name rather than a
	// wrong cluster

	private static final Set<String> _stopWords = SetUtil.fromArray(
		"a", "an", "and", "are", "as", "at", "be", "by", "for", "from", "in",
		"is", "it", "of", "on", "or", "the", "to", "with");

}