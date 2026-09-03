/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.constants;

/**
 * Shared because the bundle that indexes an asset and the bundle that
 * aggregates it are not the same, and a field name that only agrees by
 * convention compiles, deploys and returns no set at all. One field per
 * dimension, because content is only ever comparable against itself in the
 * same dimension.
 *
 * @author Mikel Lorza
 */
public class SimilarAssetConstants {

	public static final String FIELD_NAME_METADATA = "metadataSimilarAssets";

	public static final String FIELD_NAME_TEXT = "textSimilarAssets";

	public static final String FIELD_NAME_TITLE = "titleSimilarAssets";

	/**
	 * The language a dimension whose elements do not depend on the language
	 * writes its tokens under. Not a language ID, so it can never be confused
	 * with one, and shared because the reader has to ask for the same one
	 * rather than for the request's language.
	 */
	public static final String TOKEN_LANGUAGE_ID_ALL = "all";

}