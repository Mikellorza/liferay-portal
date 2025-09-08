/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {sub} from 'frontend-js-web';

import deleteEntryAction from './deleteEntryAction';

const OBJECT_ENTRY_FOLDER_CLASS_NAME =
	'com.liferay.object.model.ObjectEntryFolder';

export default function handleDeleteAction({
	action,
	event,
	itemData,
	loadData,
}: {
	action: any;
	event: Event;
	itemData: any;
	loadData: () => {};
}) {
	if (action.data.id === 'delete') {
		event?.preventDefault();

		deleteEntryAction({
			bodyHTML:
				itemData.entryClassName === OBJECT_ENTRY_FOLDER_CLASS_NAME
					? sub(
							Liferay.Language.get(
								'delete-folder-confirmation-body'
							),
							itemData.title
						)
					: sub(
							Liferay.Language.get(
								'delete-asset-confirmation-body'
							),
							itemData.title
						),
			deleteAction: itemData.actions.delete,
			loadData,
			successMessage: sub(
				Liferay.Language.get('x-was-successfully-deleted'),
				`<strong>${itemData.title}</strong>`
			),
			title: sub(
				Liferay.Language.get('delete-asset-confirmation-title'),
				itemData.title
			),
		});
	}
}
