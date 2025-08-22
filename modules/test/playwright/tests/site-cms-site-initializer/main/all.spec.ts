/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';
import {readFileSync} from 'fs';
import path from 'path';

import {dataApiHelpersTest} from '../../../fixtures/dataApiHelpersTest';
import {featureFlagsTest} from '../../../fixtures/featureFlagsTest';
import {loginTest} from '../../../fixtures/loginTest';
import getRandomString from '../../../utils/getRandomString';
import {cmsPagesTest} from './fixtures/cmsPagesTest';

const test = mergeTests(
	cmsPagesTest,
	dataApiHelpersTest,
	featureFlagsTest({
		'LPD-17564': {enabled: true},
		'LPS-179669': {enabled: true},
	}),
	loginTest()
);

test(
	'Can view Share modal for added content',
	{tag: '@LPD-62554'},
	async ({apiHelpers, assetsPage}) => {
		const applicationName = 'cms/knowledge-bases';
		const spaceName = 'Default';

		const file1Title = `title ${getRandomString()}`;

		const objectEntry1 = await apiHelpers.objectEntry.postObjectEntry(
			{
				objectEntryFolderExternalReferenceCode: 'L_CONTENTS',
				title: file1Title,
			},
			applicationName,
			spaceName
		);

		await assetsPage.gotoAll();

		await assetsPage.execItemAction({
			action: 'Share',
			filter: file1Title,
		});

		await expect(assetsPage.modal.title).toContainText(file1Title);

		await apiHelpers.objectEntry.deleteObjectEntry(
			applicationName,
			String(objectEntry1.id)
		);
	}
);

test('Dragging and dropping files into the data set opens upload modal', async ({
	assetsPage,
	page,
}) => {
	await assetsPage.gotoAll();

	const dataSetWrapper = page.locator('div.data-set-wrapper').first();
	const dataTransfer = await page.evaluateHandle(
		(data) => {
			const dt = new DataTransfer();

			const file = new File(
				[data.toString('hex')],
				'file_upload_image_1.jpeg',
				{
					type: 'image/jpg',
				}
			);
			dt.items.add(file);

			return dt;
		},
		readFileSync(
			path.join(__dirname, '/dependencies/file_upload_image_1.jpg')
		)
	);

	await dataSetWrapper.dispatchEvent('dragstart', {dataTransfer});
	await dataSetWrapper.dispatchEvent('dragenter', {dataTransfer});
	await dataSetWrapper.dispatchEvent('dragover', {dataTransfer});

	await dataSetWrapper.dispatchEvent('drop', {dataTransfer});
	await dataSetWrapper.dispatchEvent('dragend', {dataTransfer});

	await expect(assetsPage.modal.container).toBeVisible();

	await expect(assetsPage.modal.title).toContainText('Upload Multiple Files');
	await expect(assetsPage.modal.body).toContainText(
		'file_upload_image_1.jpeg'
	);
});
