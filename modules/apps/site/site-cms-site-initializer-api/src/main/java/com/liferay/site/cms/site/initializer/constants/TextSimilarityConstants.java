/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.constants;

/**
 * The names of the fields the text similarity signature of a CMS content is
 * indexed under. They are shared rather than internal because the bundle that
 * writes them and the bundle that aggregates them are not the same, and a name
 * that only agrees by convention compiles, deploys, and returns no clusters at
 * all.
 *
 * @author Mikel Lorza
 */
public class TextSimilarityConstants {

	public static final String FIELD_NAME_BANDS = "textSimilarityBands";

	public static final String FIELD_NAME_SIGNATURE = "textSimilaritySignature";

}