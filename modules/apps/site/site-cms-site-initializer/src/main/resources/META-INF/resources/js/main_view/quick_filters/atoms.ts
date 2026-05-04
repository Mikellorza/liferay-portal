/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {IFDSState} from '@liferay/frontend-data-set-web';
import {State} from '@liferay/frontend-js-state-web';

export const CMS_ALL_FDS_ATOM_ID = 'cmsAllFDSAtom';

const cmsAllFDSAtom = State.atom<IFDSState>(CMS_ALL_FDS_ATOM_ID, {
	filters: [],
	search: {
		query: '',
	},
});

export {cmsAllFDSAtom};
