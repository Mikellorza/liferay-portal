/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Locator, Page} from '@playwright/test';

import {clickAndExpectToBeVisible} from '../../../utils/clickAndExpectToBeVisible';
import {JournalPage} from './JournalPage';

export class JournalStructurePage {
	readonly page: Page;

	readonly journalPage: JournalPage;
	readonly fieldsTab: Locator;
	readonly newButton: Locator;
	readonly saveButton: Locator;
	readonly selectAllItems: Locator;
	readonly titlePlaceholder: Locator;

	constructor(page: Page) {
		this.page = page;

		this.journalPage = new JournalPage(page);
		this.fieldsTab = page.getByRole('tab', {exact: true, name: 'Fields'});
		this.newButton = page.getByText('New', {exact: true});
		this.saveButton = page.getByRole('button', {name: 'Save'});
		this.selectAllItems = page.getByLabel('Select All Items on the Page');
		this.titlePlaceholder = page.getByPlaceholder('Untitled Structure');
	}

	async goto() {
		await this.journalPage.goToStructures();
	}

	async goToCreateNewStructure() {
		await this.newButton.waitFor();

		await this.newButton.click();
	}

	async goToJournalStructureAction(action: string, title: string) {
		await this.page
			.getByRole('row', {name: title})
			.getByLabel('Show Actions')
			.waitFor();

		await clickAndExpectToBeVisible({
			autoClick: true,
			target: this.page.getByRole('menuitem', {
				exact: true,
				name: action,
			}),
			trigger: this.page
				.getByRole('row', {name: title})
				.getByLabel('Show Actions'),
		});
	}

	async deleteAllStructures() {
		await this.selectAllItems.waitFor();

		await this.selectAllItems.check();

		this.page.once('dialog', (dialog) => {
			dialog.accept().catch(() => {});
		});

		await this.page.getByRole('button', {name: 'Delete'}).click();

		await this.newButton.waitFor();
	}
}
