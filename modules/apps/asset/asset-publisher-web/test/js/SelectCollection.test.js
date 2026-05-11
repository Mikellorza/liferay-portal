/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import '@testing-library/jest-dom';
import {fireEvent, render, screen} from '@testing-library/react';
import {openModal, openSelectionModal} from 'frontend-js-components-web';
import React from 'react';

import SelectCollection from '../../src/main/resources/META-INF/resources/js/SelectCollection';

jest.mock('frontend-js-components-web', () => ({
	openModal: jest.fn(),
	openSelectionModal: jest.fn(),
}));

const DEFAULT_PROPS = {
	assetListEntryId: 0,
	clearButtonEnabled: false,
	defaultTitle: 'defaultTitle',
	editAssetListEntryURL:
		'http://localhost/edit?assetListEntryId=__ASSET_LIST_ENTRY_ID__',
	infoListProviderKey: '',
	portletNamespace: 'portletNamespace',
	selectEventName: 'selectEventName',
	title: 'title',
	url: 'url',
};

const renderComponent = (props = {}) =>
	render(<SelectCollection {...DEFAULT_PROPS} {...props} />);

describe('SelectCollection', () => {
	afterEach(() => {
		openModal.mockReset();
		openSelectionModal.mockReset();
	});

	it('does not render the edit collection button when no collection is selected', () => {
		renderComponent();

		expect(
			screen.queryByRole('button', {name: 'edit-collection'})
		).not.toBeInTheDocument();
	});

	it('renders the edit collection button when a collection is selected', () => {
		renderComponent({
			assetListEntryId: 1,
			clearButtonEnabled: true,
			title: 'My Collection',
		});

		expect(
			screen.getByRole('button', {name: 'edit-collection'})
		).toBeInTheDocument();
	});

	it('opens the modal with the resolved editAssetListEntryURL when the edit collection button is clicked', () => {
		renderComponent({
			assetListEntryId: 1,
			clearButtonEnabled: true,
			title: 'My Collection',
		});

		fireEvent.click(screen.getByRole('button', {name: 'edit-collection'}));

		expect(openModal).toHaveBeenCalledWith(
			expect.objectContaining({
				title: 'edit-collection',
				url: 'http://localhost/edit?assetListEntryId=1',
			})
		);
	});

	it('renders the remove collection button when a collection is selected', () => {
		renderComponent({
			assetListEntryId: 1,
			clearButtonEnabled: true,
			title: 'My Collection',
		});

		expect(
			screen.getByRole('button', {name: 'remove-collection'})
		).toBeInTheDocument();
	});

	it('does not render the remove collection button when no collection is selected', () => {
		renderComponent();

		expect(
			screen.queryByRole('button', {name: 'remove-collection'})
		).not.toBeInTheDocument();
	});
});
