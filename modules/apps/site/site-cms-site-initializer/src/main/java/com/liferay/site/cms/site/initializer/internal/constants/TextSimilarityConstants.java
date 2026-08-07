/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.constants;

/**
 * The names of the fields the text similarity signature is indexed under. The
 * reader holds the same names in its own dimension enumeration, so a typo on
 * either side compiles, deploys, and returns no clusters at all. Unifying both
 * sides needs a module they can share, which is tracked as a follow-up.
 *
 * @author Mikel Lorza
 */
public class SimilarityConstants {

	public static final String FIELD_NAME_TEXT_BANDS = "textSimilarityBands";

	public static final String FIELD_NAME_TEXT_SIGNATURE = "textSimilaritySignature";

}