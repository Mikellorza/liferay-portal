/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import React from 'react';

import propsTransformer from '../../src/main/resources/META-INF/resources/js/ManagementToolbarPropsTransformer';

jest.mock(
	'../../src/main/resources/META-INF/resources/js/openDeleteAssetEntryListModal',
	() => jest.fn()
);

jest.mock(
	'../../src/main/resources/META-INF/resources/js/components/NewCollectionDropDown',
	() => function MockNewCollectionDropDown() {}
);

const CREATION_MENU = {
	primaryItems: [
		{
			data: {
				action: 'addAssetListEntry',
				addAssetListEntryURL: 'http://localhost/add-manual',
				portletNamespace: 'itemNamespace_',
				title: 'Add Manual Collection',
			},
			href: '#',
			label: 'manual-collection',
		},
		{
			data: {
				action: 'addAssetListEntry',
				addAssetListEntryURL: 'http://localhost/add-dynamic',
				portletNamespace: 'itemNamespace_',
				title: 'Add Dynamic Collection',
			},
			href: '#',
			label: 'dynamic-collection',
		},
	],
};

describe('ManagementToolbarPropsTransformer', () => {
	describe('contentRight', () => {
		it('renders NewCollectionDropDown when both creation URLs are present', () => {
			const result = propsTransformer({
				creationMenu: CREATION_MENU,
				portletNamespace: 'defaultNamespace_',
			});

			expect(React.isValidElement(result.contentRight)).toBe(true);
		});

		it('passes the manual URL to NewCollectionDropDown', () => {
			const result = propsTransformer({
				creationMenu: CREATION_MENU,
				portletNamespace: 'defaultNamespace_',
			});

			expect(result.contentRight.props.addAssetListEntryURL).toBe(
				'http://localhost/add-manual'
			);
		});

		it('passes the dynamic URL to NewCollectionDropDown', () => {
			const result = propsTransformer({
				creationMenu: CREATION_MENU,
				portletNamespace: 'defaultNamespace_',
			});

			expect(result.contentRight.props.addDynamicAssetListEntryURL).toBe(
				'http://localhost/add-dynamic'
			);
		});

		it('passes portletNamespace from item data when available', () => {
			const result = propsTransformer({
				creationMenu: CREATION_MENU,
				portletNamespace: 'defaultNamespace_',
			});

			expect(result.contentRight.props.portletNamespace).toBe(
				'itemNamespace_'
			);
		});

		it('falls back to transformer portletNamespace when item data has none', () => {
			const menuWithoutNamespace = {
				primaryItems: [
					{
						data: {
							addAssetListEntryURL: 'http://localhost/add-manual',
						},
					},
					{
						data: {
							addAssetListEntryURL:
								'http://localhost/add-dynamic',
						},
					},
				],
			};

			const result = propsTransformer({
				creationMenu: menuWithoutNamespace,
				portletNamespace: 'defaultNamespace_',
			});

			expect(result.contentRight.props.portletNamespace).toBe(
				'defaultNamespace_'
			);
		});

		it('is null when creation menu has no items', () => {
			const result = propsTransformer({
				creationMenu: {primaryItems: []},
				portletNamespace: 'defaultNamespace_',
			});

			expect(result.contentRight).toBeNull();
		});

		it('is null when creation menu is absent', () => {
			const result = propsTransformer({
				portletNamespace: 'defaultNamespace_',
			});

			expect(result.contentRight).toBeNull();
		});
	});

	describe('creationMenu', () => {
		it('is always null so the built-in toolbar button is hidden', () => {
			const result = propsTransformer({
				creationMenu: CREATION_MENU,
				portletNamespace: 'defaultNamespace_',
			});

			expect(result.creationMenu).toBeNull();
		});
	});
});
