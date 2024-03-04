/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Locator, Page} from '@playwright/test';

import {JournalPage} from './JournalPage';

export class JournalEditStructurePage {
	readonly page: Page;

	readonly journalPage: JournalPage;
	readonly fieldsTab: Locator;
	readonly saveButton: Locator;
	readonly titlePlaceholder: Locator;

	constructor(page: Page) {
		this.page = page;

		this.journalPage = new JournalPage(page);
		this.fieldsTab = page.getByRole('tab', {exact: true, name: 'Fields'});
		this.saveButton = page.getByRole('button', {name: 'Save'});
		this.titlePlaceholder = page.getByPlaceholder('Untitled Structure');
	}

	async goto() {
		await this.journalPage.goToCreateNewStructure();

		await this.fieldsTab.waitFor();
	}

	async saveNewStructureWithATextField(title: string) {
		await this.fieldsTab.waitFor();

		await this.titlePlaceholder.fill(title);

		await this.page
			.getByRole('button', {
				name: 'Press enter to add Text field. Text Single line or multi-line text area.',
			})
			.dblclick();

		await this.saveButton.waitFor();

		await this.saveButton.click();
	}
}
