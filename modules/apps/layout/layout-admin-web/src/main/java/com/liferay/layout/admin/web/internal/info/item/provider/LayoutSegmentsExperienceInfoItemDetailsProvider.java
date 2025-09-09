/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.admin.web.internal.info.item.provider;

import com.liferay.info.item.ClassPKInfoItemIdentifier;
import com.liferay.info.item.ERCInfoItemIdentifier;
import com.liferay.info.item.InfoItemClassDetails;
import com.liferay.info.item.InfoItemDetails;
import com.liferay.info.item.InfoItemIdentifier;
import com.liferay.info.item.InfoItemReference;
import com.liferay.info.item.provider.InfoItemDetailsProvider;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.segments.model.SegmentsExperience;

import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Mikel Lorza
 */
@Component(service = InfoItemDetailsProvider.class)
public class LayoutSegmentsExperienceInfoItemDetailsProvider
	implements InfoItemDetailsProvider<SegmentsExperience> {

	@Override
	public InfoItemClassDetails getInfoItemClassDetails() {
		return new InfoItemClassDetails(SegmentsExperience.class.getName());
	}

	@Override
	public InfoItemDetails getInfoItemDetails(
		long groupId,
		Class<? extends InfoItemIdentifier> infoItemIdentifierClass,
		SegmentsExperience segmentsExperience) {

		if (Objects.equals(
				infoItemIdentifierClass, ClassPKInfoItemIdentifier.class)) {

			return new InfoItemDetails(
				getInfoItemClassDetails(),
				new InfoItemReference(
					SegmentsExperience.class.getName(),
					segmentsExperience.getSegmentsExperienceId()));
		}

		if (Objects.equals(
				infoItemIdentifierClass, ERCInfoItemIdentifier.class)) {

			String scopeExternalReferenceCode = _getScopeExternalReferenceCode(
				groupId, segmentsExperience);

			return new InfoItemDetails(
				getInfoItemClassDetails(),
				new InfoItemReference(
					SegmentsExperience.class.getName(),
					new ERCInfoItemIdentifier(
						segmentsExperience.getExternalReferenceCode(),
						scopeExternalReferenceCode)));
		}

		return null;
	}

	@Override
	public InfoItemDetails getInfoItemDetails(
		SegmentsExperience segmentsExperience) {

		return new InfoItemDetails(
			getInfoItemClassDetails(),
			new InfoItemReference(
				SegmentsExperience.class.getName(),
				segmentsExperience.getSegmentsExperienceId()));
	}

	private String _getScopeExternalReferenceCode(
		long groupId, SegmentsExperience segmentsExperience) {

		if (groupId == segmentsExperience.getGroupId()) {
			return null;
		}

		Group group = _groupLocalService.fetchGroup(
			segmentsExperience.getGroupId());

		if (group == null) {
			return null;
		}

		return group.getExternalReferenceCode();
	}

	@Reference
	private GroupLocalService _groupLocalService;

}