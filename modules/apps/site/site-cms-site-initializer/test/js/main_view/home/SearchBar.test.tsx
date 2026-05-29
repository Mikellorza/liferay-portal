/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import '@testing-library/jest-dom';
import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';

import SearchBar from '../../../../src/main/resources/META-INF/resources/js/main_view/home/SearchBar';

jest.mock('@liferay/frontend-data-set-web', () => ({
	decodeFdsConfigParam: (
		fdsConfigParamName: string,
		params: URLSearchParams
	) =>
		params
			.toString()
			.replace(
				new RegExp(`(${fdsConfigParamName}=)([^&]+)`),
				(_, key, value) =>
					key +
					value
						.replace(/%28/g, '(')
						.replace(/%29/g, ')')
						.replace(/%2C/g, ',')
						.replace(/%3A/g, ':')
			),
	getConfigParamName: (id: string) => `${id}_fdsConfig`,
	serializeFDSConfig: jest.fn(({q}) => `(q:${q})`),
}));

describe('SearchBar', () => {
	const searchResultsURL = 'http://localhost:8080/web/cms/all';
	const fdsConfigParam =
		'com.liferay.site.cms.site.initializer-allSection_fdsConfig';

	const originalLocation = window.location;

	let hrefSetter: jest.Mock;

	beforeEach(() => {
		hrefSetter = jest.fn();

		Object.defineProperty(window, 'location', {
			configurable: true,
			value: {
				set href(value: string) {
					hrefSetter(value);
				},
			},
		});
	});

	afterEach(() => {
		Object.defineProperty(window, 'location', {
			configurable: true,
			value: originalLocation,
		});
	});

	it('submits the search query as Rison so FDS applies the filter', async () => {
		render(
			<SearchBar
				searchResultsURL={searchResultsURL}
				userFirstName="Test"
			/>
		);

		await userEvent.type(screen.getByPlaceholderText('search'), 'mikel');

		await userEvent.keyboard('{Enter}');

		expect(hrefSetter).toHaveBeenCalledWith(
			`${searchResultsURL}?${fdsConfigParam}=(q:mikel)`
		);
	});

	it('keeps Rison structural characters decoded in the URL', async () => {
		render(
			<SearchBar
				searchResultsURL={searchResultsURL}
				userFirstName="Test"
			/>
		);

		await userEvent.type(screen.getByPlaceholderText('search'), 'foo');

		await userEvent.keyboard('{Enter}');

		const url = hrefSetter.mock.calls[0][0];

		expect(url).toContain('(q:foo)');
		expect(url).not.toContain('%28');
		expect(url).not.toContain('%29');
		expect(url).not.toContain('%3A');
	});

	it('URL-encodes special characters within the search term', async () => {
		render(
			<SearchBar
				searchResultsURL={searchResultsURL}
				userFirstName="Test"
			/>
		);

		await userEvent.type(screen.getByPlaceholderText('search'), 'a&b');

		await userEvent.keyboard('{Enter}');

		const url = hrefSetter.mock.calls[0][0];

		expect(url).toContain('%26');
	});
});
