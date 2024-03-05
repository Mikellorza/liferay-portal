/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Locator, Page} from '@playwright/test';

import {JournalPage} from './JournalPage';

export class JournalTemplatePage {
	readonly page: Page;

	readonly journalPage: JournalPage;
	readonly newButton: Locator;

	constructor(page: Page) {
		this.page = page;

		this.newButton = page.getByText('New', {exact: true});
	}

	async goto() {
		await this.journalPage.goToTemplates();
	}

	async goToCreateNewTemplate() {
		await this.newButton.waitFor();

		await this.newButton.click();
	}

	async deleteAllTemplates() {
		await this.page.getByLabel('Select All Items on the Page').check();

		this.page.once('dialog', (dialog) => {
			dialog.accept().catch(() => {});
		});

		await this.page.getByRole('button', {name: 'Delete'}).click();

		await this.newButton.waitFor();
	}

	async paginate(numEntries: number) {
		await this.page.getByText('20 Entries Per Page', {exact: true}).click();

		await this.page
			.getByRole('link', {name: numEntries + ' Entries per Page'})
			.click();
	}
}
