/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.search.spi.index.configuration.contributor;

import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.site.cms.site.initializer.constants.TextSimilarityConstants;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Mikel Lorza
 */
public class CMSTextSimilarityCompanyIndexConfigurationContributorTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	/**
	 * The mappings are a resource, so they cannot hold the field names as
	 * constants. A name that drifts from the constant the writer and the reader
	 * share leaves the field mapped by the catch-all dynamic template, as
	 * analyzed text, and the aggregation silently returns no clusters.
	 */
	@Test
	public void testMappingsDeclareSignatureFields() {
		String mappings = StringUtil.read(
			CMSTextSimilarityCompanyIndexConfigurationContributor.class,
			"dependencies/text-similarity-type-mappings.json");

		Assert.assertTrue(
			mappings,
			mappings.contains(
				"\"" + TextSimilarityConstants.FIELD_NAME_BANDS + "\""));
		Assert.assertTrue(
			mappings,
			mappings.contains(
				"\"" + TextSimilarityConstants.FIELD_NAME_SIGNATURE + "\""));
	}

}