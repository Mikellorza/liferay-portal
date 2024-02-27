/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {APIResponse, expect as baseExpect, mergeTests} from '@playwright/test';

import {apiHelpersTest} from '../../fixtures/apiHelpersTest';
import {applicationsMenuPageTest} from '../../fixtures/applicationsMenuPageTest';
import {featureFlagsTest} from '../../fixtures/featureFlagsTest';
import {loginTest} from '../../fixtures/loginTest';
import getRandomString from '../../utils/getRandomString';
import {journalPagesTest} from './fixtures/journalPagesTest';

export const test = mergeTests(
	apiHelpersTest,
	applicationsMenuPageTest,
	featureFlagsTest({
		'LPD-11253': true,
		'LPD-16469': true,
	}),
	loginTest(),
	journalPagesTest
);

const PERMISSIONS_LOCATORS = [
	'#guest_ACTION_DELETE',
	'#guest_ACTION_PERMISSIONS',
];

const expect = baseExpect.extend({
	toBeSuccessful: (response: APIResponse) => ({
		message: () =>
			response.ok()
				? 'Response is successful'
				: 'Response is not successful',
		pass: response.ok(),
	}),
});

test('LPD-6813: Make prefix URLs configurable', async ({
	friendlyUrlInstanceSettingsPage,
	journalEditArticlePage,
	journalPage,
	page,
	pageTemplatePage,
}) => {
	await journalPage.goto();

	const articleTitle = getRandomString();

	await journalEditArticlePage.publishNewBasicArticle(articleTitle);

	const article = page
		.locator(
			'#_com_liferay_journal_web_portlet_JournalPortlet_articlesSearchContainer .list-group-item'
		)
		.filter({hasText: articleTitle});

	await article.waitFor();

	await pageTemplatePage.goToDisplayPageTemplates();

	const displayPageTemplateName = getRandomString();

	await pageTemplatePage.publishNewDisplayPageTemplate(
		displayPageTemplateName
	);

	await pageTemplatePage.markPageTemplateAsDefault(displayPageTemplateName);

	await friendlyUrlInstanceSettingsPage.goto();

	const urlSeparator = 'content';

	await friendlyUrlInstanceSettingsPage.modifySeparator(
		'_com_liferay_configuration_admin_web_portlet_InstanceSettingsPortlet_com.liferay.journal.model.JournalArticle',
		urlSeparator
	);

	expect(
		await page.request.get('/' + urlSeparator + '/' + articleTitle)
	).toBeSuccessful();

	await friendlyUrlInstanceSettingsPage.goto();

	await friendlyUrlInstanceSettingsPage.resetSeparator(
		'Web Content URL Separator'
	);

	expect(await page.request.get('/w/' + articleTitle)).toBeSuccessful();

	await pageTemplatePage.goToDisplayPageTemplates();

	await pageTemplatePage.deleteDisplayPageTemplate(displayPageTemplateName);

	await journalPage.deleteJournalArticle(articleTitle);
});

test('LPD-17245: Add error message in Translation for concurrent users', async ({
	journalEditArticlePage,
	journalEditArticleTranslationsPage,
	journalPage,
	page,
}) => {
	await journalPage.goto();

	const title = getRandomString();

	await journalEditArticlePage.publishNewBasicArticle(title);

	const article = page
		.locator(
			'#_com_liferay_journal_web_portlet_JournalPortlet_articlesSearchContainer .list-group-item'
		)
		.filter({hasText: title});

	await article.waitFor();

	const editBasicArticleTranslationUrl =
		await journalEditArticleTranslationsPage.editBasicArticleTranslations(
			title,
			''
		);

	await journalEditArticlePage.editAndPublishExistingBasicArticle(title);

	await journalEditArticleTranslationsPage.assertErrorInEditBasicArticleTranslations(
		editBasicArticleTranslationUrl
	);

	await journalPage.deleteJournalArticle(title);
});

test('LPD-17782: This is a test for bulk permissions of web content', async ({
	journalEditArticlePage,
	journalPage,
	page,
}) => {
	await journalPage.goto();

	const title1 = getRandomString();
	const title2 = getRandomString();

	await journalEditArticlePage.publishNewBasicArticle(title1);

	const article1 = page
		.locator(
			'#_com_liferay_journal_web_portlet_JournalPortlet_articlesSearchContainer .list-group-item'
		)
		.filter({hasText: title1});

	await article1.waitFor();

	await journalEditArticlePage.publishNewBasicArticle(title2);

	const article2 = page
		.locator(
			'#_com_liferay_journal_web_portlet_JournalPortlet_articlesSearchContainer .list-group-item'
		)
		.filter({hasText: title2});

	await article2.waitFor();

	await journalPage.setJournalArticlePermissions(
		[article1, article2],
		PERMISSIONS_LOCATORS
	);

	await journalPage.assertJournalArticlePermissions(
		title1,
		PERMISSIONS_LOCATORS
	);
	await journalPage.assertJournalArticlePermissions(
		title2,
		PERMISSIONS_LOCATORS
	);

	await journalPage.deleteJournalArticle(title1);
	await journalPage.deleteJournalArticle(title2);
});
