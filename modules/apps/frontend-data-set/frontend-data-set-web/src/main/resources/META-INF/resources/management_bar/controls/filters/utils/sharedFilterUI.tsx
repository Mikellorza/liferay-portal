/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton from '@clayui/button';
import React from 'react';

export type ActionType = 'add' | 'edit' | 'delete';

export function getActionType(
	selectedData: unknown,
	fromValue: unknown,
	toValue: unknown
): ActionType {
	if (!selectedData) {
		return 'add';
	}

	if (!fromValue && !toValue) {
		return 'delete';
	}

	return 'edit';
}

interface IFilterSubmitButtonProps {
	actionType: ActionType;
	disabled: boolean;
	onClick: () => void;
}

export function FilterSubmitButton({
	actionType,
	disabled,
	onClick,
}: IFilterSubmitButtonProps) {
	return (
		<ClayButton disabled={disabled} onClick={onClick} small>
			{actionType === 'add' && Liferay.Language.get('add-filter')}

			{actionType === 'edit' && Liferay.Language.get('show-results')}

			{actionType === 'delete' && Liferay.Language.get('delete-filter')}
		</ClayButton>
	);
}
