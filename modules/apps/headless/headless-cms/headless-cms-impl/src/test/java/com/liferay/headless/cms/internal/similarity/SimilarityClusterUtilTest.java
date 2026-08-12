/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.cms.internal.similarity;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Mikel Lorza
 */
public class SimilarityClusterUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testGetClustersChainsAssetsThroughSharedBands() {

		// The first and the last asset share no band at all and still land in
		// one cluster, because each is connected to the one in the middle

		List<List<Long>> clusters = SimilarityClusterUtil.getClusters(
			Arrays.asList(
				_mockDocument(1L, "b1", "b2", "b3"),
				_mockDocument(2L, "b1", "b2", "b3", "b4", "b5", "b6"),
				_mockDocument(3L, "b4", "b5", "b6")),
			_BAND_FIELD, _toSet("b1", "b2", "b3", "b4", "b5", "b6"));

		Assert.assertEquals(clusters.toString(), 1, clusters.size());
		Assert.assertEquals(Arrays.asList(1L, 2L, 3L), clusters.get(0));
	}

	@Test
	public void testGetClustersDropsAssetsSharingNoBand() {
		List<List<Long>> clusters = SimilarityClusterUtil.getClusters(
			Arrays.asList(
				_mockDocument(1L, "b1", "b2", "b3"),
				_mockDocument(2L, "b1", "b2", "b3"), _mockDocument(3L, "b7")),
			_BAND_FIELD, _toSet("b1", "b2", "b3"));

		Assert.assertEquals(clusters.toString(), 1, clusters.size());
		Assert.assertEquals(Arrays.asList(1L, 2L), clusters.get(0));
	}

	@Test
	public void testGetClustersIgnoresBandsThatAreNotShared() {

		// A band the aggregation did not report as shared is held by a single
		// asset in the scope, so grouping on it would invent a cluster

		Assert.assertEquals(
			Collections.emptyList(),
			SimilarityClusterUtil.getClusters(
				Arrays.asList(_mockDocument(1L, "b9"), _mockDocument(2L, "b9")),
				_BAND_FIELD, _toSet("b1")));
	}

	@Test
	public void testGetClustersNeedsMoreThanTwoSharedBands() {

		// One band is what an unrelated pair collides on, and grouping is
		// transitive, so a single band would merge a whole space into one
		// cluster once it holds a few hundred assets

		Assert.assertEquals(
			Collections.emptyList(),
			SimilarityClusterUtil.getClusters(
				Arrays.asList(
					_mockDocument(1L, "b1", "b2"),
					_mockDocument(2L, "b1", "b2")),
				_BAND_FIELD, _toSet("b1", "b2")));
	}

	@Test
	public void testGetMinObjectEntryId() {
		Assert.assertEquals(
			Long.valueOf(2),
			SimilarityClusterUtil.getMinObjectEntryId(
				Arrays.asList(7L, 2L, 5L)));
	}

	@Test
	public void testGetSignatureIgnoresAnotherLanguage() {
		Assert.assertNull(
			SimilarityClusterUtil.getSignature(
				_getSignatureTokens("es_ES", 3), "en_US"));
	}

	@Test
	public void testGetSignatureIsNullWhenIncomplete() {
		List<String> tokens = _getSignatureTokens("en_US", 7);

		tokens.remove(0);

		Assert.assertNull(SimilarityClusterUtil.getSignature(tokens, "en_US"));
	}

	@Test
	public void testGetSignatureReadsTheRequestLanguage() {
		List<String> tokens = _getSignatureTokens("en_US", 3);

		tokens.addAll(_getSignatureTokens("es_ES", 11));

		long[] signature = SimilarityClusterUtil.getSignature(tokens, "en_US");

		Assert.assertEquals(
			Arrays.toString(signature), _SIGNATURE_SIZE, signature.length);

		for (int i = 0; i < _SIGNATURE_SIZE; i++) {
			Assert.assertEquals(3, signature[i]);
		}
	}

	@Test
	public void testGetSimilarityCountsAgreeingPositions() {
		long[] signature1 = new long[_SIGNATURE_SIZE];
		long[] signature2 = new long[_SIGNATURE_SIZE];

		for (int i = 0; i < _SIGNATURE_SIZE; i++) {
			signature1[i] = i;
			signature2[i] = i;
		}

		Assert.assertEquals(
			1.0, SimilarityClusterUtil.getSimilarity(signature1, signature2),
			0.0);

		for (int i = 0; i < (_SIGNATURE_SIZE / 2); i++) {
			signature2[i] = -1;
		}

		Assert.assertEquals(
			0.5, SimilarityClusterUtil.getSimilarity(signature1, signature2),
			0.0);
	}

	@Test
	public void testGetTopObjectEntryIdIsClosestToTheOthers() {

		// The second asset agrees with both of the others, while they agree
		// only with it, so it is the one the cluster is measured against

		Assert.assertEquals(
			Long.valueOf(2),
			SimilarityClusterUtil.getTopObjectEntryId(
				Arrays.asList(1L, 2L, 3L),
				HashMapBuilder.put(
					1L, _getSignature(0, 0)
				).put(
					2L, _getSignature(0, 1)
				).put(
					3L, _getSignature(1, 1)
				).build()));
	}

	@Test
	public void testGetTopObjectEntryIdIsNullWithoutSignatures() {
		Assert.assertNull(
			SimilarityClusterUtil.getTopObjectEntryId(
				Arrays.asList(1L, 2L), new HashMap<>()));
	}

	private long[] _getSignature(long firstHalfValue, long secondHalfValue) {
		long[] signature = new long[_SIGNATURE_SIZE];

		for (int i = 0; i < _SIGNATURE_SIZE; i++) {
			if (i < (_SIGNATURE_SIZE / 2)) {
				signature[i] = firstHalfValue;
			}
			else {
				signature[i] = secondHalfValue;
			}
		}

		return signature;
	}

	private List<String> _getSignatureTokens(String languageId, long value) {
		List<String> tokens = new ArrayList<>();

		for (int i = 0; i < _SIGNATURE_SIZE; i++) {
			tokens.add(StringBundler.concat(languageId, "_p", i, "_", value));
		}

		return tokens;
	}

	private Document _mockDocument(Long objectEntryId, String... bands) {
		Document document = Mockito.mock(Document.class);

		Mockito.when(
			document.getLong("objectEntryId")
		).thenReturn(
			objectEntryId
		);

		Mockito.when(
			document.getStrings(_BAND_FIELD)
		).thenReturn(
			ListUtil.fromArray(bands)
		);

		return document;
	}

	private Set<String> _toSet(String... bands) {
		return new HashSet<>(Arrays.asList(bands));
	}

	// The value is passed to the code under test and to the mock, never
	// asserted, so a fixed name would only suggest that the test pins the
	// production field name. It does not; the index mappings test does

	private static final String _BAND_FIELD = RandomTestUtil.randomString();

	private static final int _SIGNATURE_SIZE = 128;

}