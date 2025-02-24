/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.frontend.data.set.view.cards;

import com.liferay.frontend.data.set.model.FDSLabelTypeItem;
import com.liferay.frontend.data.set.view.FDSView;
import com.liferay.frontend.data.set.view.cards.BaseCardsFDSView;
import com.liferay.portal.kernel.language.Language;
import com.liferay.site.cms.site.initializer.internal.constants.CMSSiteInitializerFDSNames;

import java.util.Locale;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Mikel Lorza
 */
@Component(
	property = "frontend.data.set.name=" + CMSSiteInitializerFDSNames.CONTENT_SECTION,
	service = FDSView.class
)
public class ContentSectionCardsFDSView extends BaseCardsFDSView {

	@Override
	public String getDescription() {
		return "description";
	}

	@Override
	public String getImage() {
		return "embedded.contentUrl";
	}

	@Override
	public String[] getLabels() {
		return new String[] {"embedded.status"};
	}

	@Override
	public FDSLabelTypeItem[] getLabelTypes(Locale locale) {
		return new FDSLabelTypeItem[] {
			new FDSLabelTypeItem("success", _language.get(locale, "approved"))
		};
	}

	@Override
	public String getTitle() {
		return "title";
	}

	@Reference
	private Language _language;

}