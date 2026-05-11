/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import React from 'react';

import openDeleteAssetEntryListModal from './openDeleteAssetEntryListModal';
import NewCollectionDropDown from './components/NewCollectionDropDown';

export default function propsTransformer({portletNamespace, ...otherProps}) {
	const {creationMenu, ...rest} = otherProps;

	const [manualItem, dynamicItem] =
		creationMenu?.primaryItems ?? [];

	const addAssetListEntryURL = manualItem?.data?.addAssetListEntryURL;
	const addDynamicAssetListEntryURL =
		dynamicItem?.data?.addAssetListEntryURL;

	const contentRight =
		addAssetListEntryURL && addDynamicAssetListEntryURL
			? React.createElement(NewCollectionDropDown, {
					addAssetListEntryURL,
					addDynamicAssetListEntryURL,
					dynamicTitle: dynamicItem?.data?.title,
					manualTitle: manualItem?.data?.title,
					portletNamespace:
						manualItem?.data?.portletNamespace || portletNamespace,
				})
			: null;

	return {
		...rest,
		contentRight,
		creationMenu: null,
		onActionButtonClick(event, {item}) {
			if (item?.data?.action === 'deleteSelectedAssetListEntries') {
				openDeleteAssetEntryListModal({
					multiple: true,
					onDelete: () => {
						const form = document.getElementById(
							`${portletNamespace}fm`
						);

						if (form) {
							submitForm(form);
						}
					},
				});
			}
		},
	};
}
