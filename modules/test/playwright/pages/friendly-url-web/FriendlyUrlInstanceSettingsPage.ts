/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Locator, Page} from '@playwright/test';

import {InstanceSettingsPage} from '../configuration-admin-web/InstanceSettingsPage';

export class FriendlyUrlInstanceSettingsPage {
	readonly page: Page;
	readonly saveButton: Locator;
	readonly successfullyMessage: Locator;
	readonly instanceSettingsPage: InstanceSettingsPage;

	constructor(page: Page) {
		this.page = page;
		this.saveButton = page.getByRole('button', {name: 'Save'});
		this.successfullyMessage = page.getByText(
			'Success:Your request completed successfully.'
		);
		this.instanceSettingsPage = new InstanceSettingsPage(page);
	}

	async goto() {
		await this.instanceSettingsPage.goToInstanceSetting(
			'SEO',
			'Friendly URL'
		);
	}

	async modifySeparator(inputName: string, value: string) {
		await this.page.locator('input[name="' + inputName + '"]').click();
		await this.page.locator('input[name="' + inputName + '"]').fill(value);
		await this.saveButton.click();
		await this.successfullyMessage.waitFor();
	}

	async resetSeparator(label: string) {
		await this.page
			.getByLabel('URL Separator')
			.locator('div')
			.filter({hasText: label})
			.getByLabel('Reset to Default Value')
			.click();
		await this.saveButton.click();
		await this.successfullyMessage.waitFor();
	}
}
