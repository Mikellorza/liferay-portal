/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayForm, {ClayInput} from '@clayui/form';
import React from 'react';

type Props = {
	fields: Array<{
		label: string;
		name: string;
		value: string;
	}>;
	url: string;
};

export default function SeparatorFields({fields, url}: Props) {
	return (
		<>
			{fields.map((field) => (
				<ClayForm.Group key={field.name}>
					<label className="mb-0" htmlFor={field.name}>
						{field.label}
					</label>

					<p className="mb-1 text-secondary">{url}</p>

					<ClayInput.Group>
						<ClayInput.GroupItem prepend shrink>
							<ClayInput.GroupText>/</ClayInput.GroupText>
						</ClayInput.GroupItem>

						<ClayInput.GroupItem append>
							<ClayInput
								defaultValue={field.value}
								id={field.name}
								name={field.name}
							/>
						</ClayInput.GroupItem>
					</ClayInput.Group>
				</ClayForm.Group>
			))}
		</>
	);
}
