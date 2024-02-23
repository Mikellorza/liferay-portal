/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Page} from '@playwright/test';

import {ProductMenuPage} from '../../../pages/product-navigation-product-menu/ProductMenu.page';

export class PageTemplatePage {
	readonly page: Page;
	readonly productMenuPage: ProductMenuPage;

	constructor(page: Page) {
		this.page = page;
		this.productMenuPage = new ProductMenuPage(page);
	}

	async goto() {
		await this.productMenuPage.goToPageTemplatesMenuItem();
	}

	async goToDisplayPageTemplates() {
		await this.goto();
		await this.page
			.getByRole('link', {
				name: 'Display Page Templates',
			})
			.click();
	}

	async publishNewDisplayPageTemplate(name: string) {
		await this.page.getByText('New', {exact: true}).click();
		await this.page.getByRole('button', {name: 'Blank'}).click();
		await this.page.getByLabel('Name').fill(name);
		await this.page
			.getByLabel('Content Type')
			.selectOption({label: 'Web Content Article'});
		await this.page
			.getByLabel('Subtype')
			.selectOption({label: 'Basic Web Content'});
		await this.page.getByRole('button', {name: 'Save'}).click();
		await this.page.getByLabel('Publish', {exact: true}).waitFor();
		await this.page.getByLabel('Publish', {exact: true}).click();
	}

	async clickPageTemplateMoreActions(name: string) {
		await this.page
			.locator(
				'#_com_liferay_layout_page_template_admin_web_portlet_LayoutPageTemplatesPortlet_displayPagesSearchContainer .card-page-item'
			)
			.filter({hasText: name})
			.getByLabel('More actions')
			.click();
	}

	async markPageTemplateAsDefault(name: string) {
		await this.clickPageTemplateMoreActions(name);
		await this.page.once('dialog', (dialog) => {
			dialog.accept().catch(() => {});
		});
		await this.page
			.getByRole('menuitem', {name: 'Mark as Default'})
			.click();
	}

	async deleteDisplayPageTemplate(name: string) {
		await this.clickPageTemplateMoreActions(name);
		await this.page.getByRole('menuitem', {name: 'Delete'}).click();
		await this.page.getByRole('button', {name: 'Delete'}).click();
	}
}
