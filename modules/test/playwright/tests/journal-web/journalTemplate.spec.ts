/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {apiHelpersTest} from '../../fixtures/apiHelpersTest';
import {applicationsMenuPageTest} from '../../fixtures/applicationsMenuPageTest';
import {loginTest} from '../../fixtures/loginTest';
import getRandomString from '../../utils/getRandomString';
import {journalPagesTest} from './fixtures/journalPagesTest';

export const test = mergeTests(
	apiHelpersTest,
	applicationsMenuPageTest,
	journalPagesTest,
	loginTest()
);

const RESERVED_VARIABLES = [
	'Author Email Address',
	'Author ID',
	'Author Job Title',
	'Author Name',
	'Comments',
	'Create Date',
	'Description',
	'Display Date',
	'ID',
	'Modified Date',
	'Small Image URL',
	'Tags',
	'Title',
	'URL Title',
	'Version',
];

test('This is a test for LPS-177690. The tooltip of the back button should be Go to Web Content in the editor of Templates.', async ({
	journalEditTemplatePage,
	journalPage,
	page,
}) => {
	await journalPage.goto();
	await journalEditTemplatePage.goto();

	await expect(page.getByTitle('Go to Web Content')).toBeVisible();
});

test('This is a test for LPS-153976 and LPD-16407. Check Featured image and reserved variables are present', async ({
	journalEditTemplatePage,
	journalPage,
	page,
}) => {
	await journalPage.goto();
	await journalEditTemplatePage.goto();

	// Featured image is present when we are editing a template.

	await expect(page.getByLabel('Image Source')).toBeAttached();

	// View reserved variables list under Journal section in web content template.

	await journalEditTemplatePage.gotoElements();

	for (const reservedVariable of RESERVED_VARIABLES) {
		await expect(
			page.getByRole('button', {exact: true, name: reservedVariable})
		).toBeVisible();
	}
});

test('LPD-19462 This is a test to test templates pagination of a selected structure.', async ({
	journalEditStructurePage,
	journalEditTemplatePage,
	journalPage,
	journalStructurePage,
	journalTemplatePage,
	page,
}) => {
	await journalPage.goto();

	await journalPage.goToStructures();

	await journalStructurePage.goToCreateNewStructure();

	const title1 = getRandomString();

	await journalEditStructurePage.saveNewStructureWithATextField(title1);

	await journalStructurePage.goToCreateNewStructure();

	const title2 = getRandomString();

	await journalEditStructurePage.saveNewStructureWithATextField(title2);

	await journalStructurePage.goto();

	await journalStructurePage.goToJournalStructureAction(
		'Manage Templates',
		title1
	);

	await journalTemplatePage.goToCreateNewTemplate();
	await journalEditTemplatePage.saveNewTemplateWithSelectedStructure(
		getRandomString()
	);

	await journalStructurePage.goto();

	await journalStructurePage.goToJournalStructureAction(
		'Manage Templates',
		title2
	);

	for (let i = 0; i < 10; i++) {
		await journalTemplatePage.goToCreateNewTemplate();
		await journalEditTemplatePage.saveNewTemplateWithSelectedStructure(
			getRandomString()
		);
	}

	await journalTemplatePage.paginate(40);

	await expect(
		page.locator(
			'#_com_liferay_journal_web_portlet_JournalPortlet_ddmTemplatesPageIteratorBottom_ariaPaginationResults'
		)
	).toHaveText(/Showing 1 to 10 of 10 entries./);

	await journalTemplatePage.deleteAllTemplates();

	await journalStructurePage.goto();

	await journalStructurePage.deleteAllStructures();
});
