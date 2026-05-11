/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton from '@clayui/button';
import ClayDropDown from '@clayui/drop-down';
import ClayIcon from '@clayui/icon';
import {openSimpleInputModal} from 'frontend-js-components-web';
import React, {useState} from 'react';

export default function NewCollectionDropDown({
	addAssetListEntryURL,
	addDynamicAssetListEntryURL,
	dynamicTitle,
	manualTitle,
	portletNamespace,
}) {
	const [active, setActive] = useState(false);

	const handleSelect = (url, dialogTitle) => {
		setActive(false);

		openSimpleInputModal({
			dialogTitle,
			formSubmitURL: url,
			mainFieldLabel: Liferay.Language.get('title'),
			mainFieldName: 'title',
			mainFieldPlaceholder: Liferay.Language.get('title'),
			namespace: portletNamespace,
			onFormSuccess: () => window.location.reload(),
		});
	};

	return (
		<ClayDropDown
			active={active}
			onActiveChange={setActive}
			trigger={
				<ClayButton displayType="primary">
					{Liferay.Language.get('new')}

					<ClayIcon
						className="inline-item inline-item-after"
						symbol="caret-bottom"
					/>
				</ClayButton>
			}
		>
			<ClayDropDown.ItemList>
				<ClayDropDown.Item
					onClick={() =>
						handleSelect(addAssetListEntryURL, manualTitle)
					}
				>
					{Liferay.Language.get('manual-collection')}
				</ClayDropDown.Item>

				<ClayDropDown.Item
					onClick={() =>
						handleSelect(
							addDynamicAssetListEntryURL,
							dynamicTitle
						)
					}
				>
					{Liferay.Language.get('dynamic-collection')}
				</ClayDropDown.Item>
			</ClayDropDown.ItemList>
		</ClayDropDown>
	);
}
