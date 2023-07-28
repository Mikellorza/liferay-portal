/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.segments.web.internal.display.context;

import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.segments.display.context.SegmentsExperienceSelectorDisplayContext;
import com.liferay.segments.display.context.SegmentsExperienceSelectorDisplayContextProvider;
import com.liferay.segments.manager.SegmentsExperienceManager;
import com.liferay.segments.service.SegmentsEntryLocalService;
import com.liferay.segments.service.SegmentsExperienceLocalService;
import com.liferay.segments.service.SegmentsExperimentLocalService;
import com.liferay.segments.service.SegmentsExperimentRelLocalService;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Mikel Lorza
 */
@Component(service = SegmentsExperienceSelectorDisplayContextProvider.class)
public class SegmentsExperienceSelectorDisplayContextProviderImpl
	implements SegmentsExperienceSelectorDisplayContextProvider {

	@Override
	public SegmentsExperienceSelectorDisplayContext
		getSegmentsExperienceSelectorDisplayContext(
			HttpServletRequest httpServletRequest) {

		return new DefaultSegmentsExperienceSelectorDisplayContext(
			httpServletRequest, _jsonFactory, _language, _portal,
			_segmentsEntryLocalService,
			new SegmentsExperienceManager(_segmentsExperienceLocalService),
			_segmentsExperienceLocalService, _segmentsExperimentLocalService,
			_segmentsExperimentRelLocalService);
	}

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private Language _language;

	@Reference
	private Portal _portal;

	@Reference
	private SegmentsEntryLocalService _segmentsEntryLocalService;

	@Reference
	private SegmentsExperienceLocalService _segmentsExperienceLocalService;

	@Reference
	private SegmentsExperimentLocalService _segmentsExperimentLocalService;

	@Reference
	private SegmentsExperimentRelLocalService
		_segmentsExperimentRelLocalService;

}