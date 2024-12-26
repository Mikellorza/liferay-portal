/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {apiHelpersTest} from '../../fixtures/apiHelpersTest';
import {isolatedSiteTest} from '../../fixtures/isolatedSiteTest';
import {loginTest} from '../../fixtures/loginTest';
import getBasicWebContentStructureId from '../../utils/structured-content/getBasicWebContentStructureId';
import {journalPagesTest} from './fixtures/journalPagesTest';
import getDataStructureDefinition from './utils/getDataStructureDefinition';
import addApprovedStructuredContent
	from "../../utils/structured-content/addApprovedStructuredContent";
import getRandomString from "../../utils/getRandomString";

export const test = mergeTests(
	apiHelpersTest,
	isolatedSiteTest,
	journalPagesTest,
	loginTest()
);

test(
	'Table view displays folders and articles correctly',
	{
		tag: '@LPD-42429',
	},
	async ({apiHelpers, journalPage, page, site}) => {
		const basicWebContentStructureId =
			await getBasicWebContentStructureId(apiHelpers);

		await apiHelpers.jsonWebServicesJournal.addWebContent({
			ddmStructureId: basicWebContentStructureId,
			groupId: site.id,
			titleMap: {en_US: 'First Web content'},
		});

		await apiHelpers.jsonWebServicesJournal.addFolder({
			groupId: site.id,
		});

		await journalPage.goto(site.friendlyUrlPath);

		await journalPage.changeView('table');

		await expect(page.getByRole('cell', {name: 'Title'})).toBeVisible();

		await expect(
			page.getByRole('cell', {name: 'Description'})
		).toBeVisible();

		await expect(page.getByRole('cell', {name: 'Author'})).toBeVisible();

		await expect(page.getByRole('cell', {name: 'Status'})).toBeVisible();

		await expect(page.getByRole('cell', {name: 'Type'})).toBeVisible();

		await expect(
			page.getByRole('cell', {name: 'Modified Date'})
		).toBeVisible();

		await expect(
			page.getByRole('cell', {name: 'Display Date'})
		).toBeVisible();

		await expect(
			page.getByRole('cell', {name: 'Create Date'})
		).toBeVisible();
	}
);

test(
	'After clicking on Clear (filter by structure) you can see all the web contents',
	{
		tag: '@LPS-191026',
	},
	async ({apiHelpers, journalEditArticlePage, journalPage, page, site}) => {
		const basicWebContentStructureId =
			await getBasicWebContentStructureId(apiHelpers);

		await apiHelpers.jsonWebServicesJournal.addWebContent({
			ddmStructureId: basicWebContentStructureId,
			groupId: site.id,
			titleMap: {en_US: 'First Web content'},
		});

		const structureName = 'Structure Test';

		const dataDefinition = getDataStructureDefinition({
			defaultLanguageId: 'en_US',
			fields: [{name: 'Text', repeatable: false}],
			name: structureName,
		});

		await apiHelpers.dataEngine.createStructure(site.id, dataDefinition);

		await journalEditArticlePage.goto({
			siteUrl: site.friendlyUrlPath,
			structureName,
		});

		await journalEditArticlePage.createArticleForStructure({
			structureName,
			title: 'Second Web Content',
		});

		await journalPage.goto(site.friendlyUrlPath);

		await expect(
			page.getByRole('link', {name: 'First Web content'})
		).toBeVisible();

		await expect(
			page.getByRole('link', {name: 'Second Web content'})
		).toBeVisible();

		await page.getByLabel('Filter', {exact: true}).click();

		await page.getByRole('menuitem', {name: 'Structures'}).click();

		const structuresFrame = await page.frameLocator(
			'iframe[title="Structures"]'
		);

		await structuresFrame
			.getByLabel('Reverse Order Direction: Currently Descending')
			.waitFor();

		await structuresFrame
			.getByRole('cell', {name: 'Basic Web Content'})
			.click();

		await expect(
			page.getByRole('link', {name: 'Second Web content'})
		).toBeHidden();

		await page
			.getByLabel('Clear 1 Result for Structures: Basic Web Content')
			.click();

		await expect(
			page.getByRole('link', {name: 'Second Web content'})
		).toBeVisible();
	}
);

test(
	'When pagination with highlighted structures, the selected structure is lost.',
	{
		tag: '@LPD-45163',
	},
	async ({apiHelpers, journalEditArticlePage, journalPage, page, site}) => {
		await addApprovedStructuredContent({
			apiHelpers,
			contentStructureId: await getBasicWebContentStructureId(apiHelpers),
			siteId: site.id,
			title: getRandomString(),
		});

		const structureName = 'Highlighted structure Test';

		const dataDefinition = getDataStructureDefinition({
			defaultLanguageId: 'en_US',
			fields: [{name: 'Text', repeatable: false}],
			name: structureName,
		});

		const structure = await apiHelpers.dataEngine.createStructure(
			site.id,
			dataDefinition
		);

		for (let i = 0; i < 5; i++) {
			await addApprovedStructuredContent({
				apiHelpers,
				contentStructureId: Number(structure.id),
				siteId: site.id,
				title: getRandomString(),
			});
		}

		await journalPage.goto(site.friendlyUrlPath);

		await page.getByLabel('Options').click();
		await page.getByRole('menuitem', { name: 'Configuration' }).click();

		await page.getByLabel('Select Highlighted Structures').click();

		const iframeLocator = page.frameLocator('iframe[title="Select Structures"]');

		await iframeLocator.getByLabel(structureName).check();

		await page.getByRole('button', { name: 'Add' }).click();

		const saveButtonLocator = await page.getByRole('button', { name: 'Save' });

		await saveButtonLocator.click();
		await saveButtonLocator.click();

		await journalPage.goto(site.friendlyUrlPath);

		await page.getByRole('menuitem', { name: structureName }).click();

		await page.getByLabel('Items per Page').click();

		await page.getByRole('option', { name: '4 Entries per Page' }).click();

		await expect(page.getByText('Showing 1 to 4 of 5 entries.')).toBeVisible();
	}
);
