/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {fireEvent, render, screen} from '@testing-library/react';
import {openSimpleInputModal} from 'frontend-js-components-web';
import React from 'react';

import NewCollectionDropDown from '../../../src/main/resources/META-INF/resources/js/components/NewCollectionDropDown';

jest.mock('frontend-js-components-web', () => ({
	openSimpleInputModal: jest.fn(),
}));

const DEFAULT_PROPS = {
	addAssetListEntryURL: 'http://localhost/add-manual',
	addDynamicAssetListEntryURL: 'http://localhost/add-dynamic',
	dynamicTitle: 'Add Dynamic Collection',
	manualTitle: 'Add Manual Collection',
	portletNamespace: 'testNamespace_',
};

function renderDropDown(props = {}) {
	return render(
		<NewCollectionDropDown {...DEFAULT_PROPS} {...props} />
	);
}

describe('NewCollectionDropDown', () => {
	beforeEach(() => {
		Liferay.Language.get.mockImplementation((key) => key);
		openSimpleInputModal.mockClear();
	});

	it('renders the trigger button', () => {
		renderDropDown();

		expect(screen.getByText('new')).toBeInTheDocument();
	});

	it('opens the dropdown when the trigger is clicked', () => {
		renderDropDown();

		fireEvent.click(screen.getByText('new'));

		expect(screen.getByText('manual-collection')).toBeInTheDocument();
		expect(screen.getByText('dynamic-collection')).toBeInTheDocument();
	});

	it('calls openSimpleInputModal with manual URL when manual item is clicked', () => {
		renderDropDown();

		fireEvent.click(screen.getByText('new'));
		fireEvent.click(screen.getByText('manual-collection'));

		expect(openSimpleInputModal).toHaveBeenCalledWith(
			expect.objectContaining({
				dialogTitle: 'Add Manual Collection',
				formSubmitURL: 'http://localhost/add-manual',
				namespace: 'testNamespace_',
			})
		);
	});

	it('calls openSimpleInputModal with dynamic URL when dynamic item is clicked', () => {
		renderDropDown();

		fireEvent.click(screen.getByText('new'));
		fireEvent.click(screen.getByText('dynamic-collection'));

		expect(openSimpleInputModal).toHaveBeenCalledWith(
			expect.objectContaining({
				dialogTitle: 'Add Dynamic Collection',
				formSubmitURL: 'http://localhost/add-dynamic',
				namespace: 'testNamespace_',
			})
		);
	});

	it('calls window.location.reload on form success', () => {
		const reloadMock = jest.fn();
		Object.defineProperty(window, 'location', {
			value: {reload: reloadMock},
			writable: true,
		});

		renderDropDown();

		fireEvent.click(screen.getByText('new'));
		fireEvent.click(screen.getByText('manual-collection'));

		const {onFormSuccess} = openSimpleInputModal.mock.calls[0][0];

		onFormSuccess();

		expect(reloadMock).toHaveBeenCalled();
	});
});
