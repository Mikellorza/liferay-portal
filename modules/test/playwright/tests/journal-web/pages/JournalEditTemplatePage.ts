/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Locator, Page} from '@playwright/test';

import {JournalPage} from './JournalPage';

export class JournalEditTemplatePage {
	readonly page: Page;

	readonly elementsButton: Locator;
	readonly journalPage: JournalPage;
	readonly saveButton: Locator;
	readonly titlePlaceholder: Locator;

	constructor(page: Page) {
		this.page = page;

		this.elementsButton = page.getByTitle('Elements', {exact: true});
		this.journalPage = new JournalPage(page);
		this.saveButton = page.getByRole('button', {exact: true, name: 'Save'});
		this.titlePlaceholder = page.getByPlaceholder('Untitled Template');
	}

	async goto() {
		await this.journalPage.goToCreateNewTemplate();

		// Do it twice so we decrease flakiness

		await this.journalPage.goto();
		await this.journalPage.goToCreateNewTemplate();
	}

	async gotoElements() {
		await this.elementsButton.click();
	}

	async saveNewTemplateWithSelectedStructure(title: string) {
		await this.titlePlaceholder.waitFor();

		await this.titlePlaceholder.click();

		await this.titlePlaceholder.fill(title);

		await this.saveButton.waitFor();

		await this.saveButton.click();
	}
}
