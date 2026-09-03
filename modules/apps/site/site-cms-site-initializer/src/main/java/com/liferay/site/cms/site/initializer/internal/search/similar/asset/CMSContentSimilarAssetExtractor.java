/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.search.similar.asset;

import com.liferay.object.model.ObjectEntry;

import java.util.Set;

/**
 * Turns a CMS content into the set of elements one dimension compares it by,
 * and names the field that set is indexed under. The elements are what makes a
 * dimension a dimension: prose is compared by sequences of keywords, titles by
 * character n grams, and metadata by the categories and tags themselves.
 *
 * @author Mikel Lorza
 */
public interface CMSContentSimilarAssetExtractor {

	public Set<String> getElements(
			ObjectEntry objectEntry, String tokenLanguageId)
		throws Exception;

	public String getFieldName();

	/**
	 * Returns the languages the content is indexed under, which a dimension
	 * whose elements do not depend on the language answers with the single
	 * value that stands for all of them.
	 */
	public Set<String> getTokenLanguageIds(ObjectEntry objectEntry)
		throws Exception;

}