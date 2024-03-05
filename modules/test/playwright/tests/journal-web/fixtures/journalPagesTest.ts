/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {test} from '@playwright/test';

import {JournalEditArticlePage} from '../pages/JournalEditArticlePage';
import {JournalEditArticleTranslationsPage} from '../pages/JournalEditArticleTranslationsPage';
import {JournalEditStructurePage} from '../pages/JournalEditStructurePage';
import {JournalEditTemplatePage} from '../pages/JournalEditTemplatePage';
import {JournalPage} from '../pages/JournalPage';
import {JournalStructurePage} from '../pages/JournalStructurePage';
import {JournalTemplatePage} from '../pages/JournalTemplatePage';

const journalPagesTest = test.extend<{
	journalEditArticlePage: JournalEditArticlePage;
	journalEditArticleTranslationsPage: JournalEditArticleTranslationsPage;
	journalEditStructurePage: JournalEditStructurePage;
	journalEditTemplatePage: JournalEditTemplatePage;
	journalPage: JournalPage;
	journalStructurePage: JournalStructurePage;
	journalTemplatePage: JournalTemplatePage;
}>({
	journalEditArticlePage: async ({page}, use) => {
		await use(new JournalEditArticlePage(page));
	},
	journalEditArticleTranslationsPage: async ({page}, use) => {
		await use(new JournalEditArticleTranslationsPage(page));
	},
	journalEditStructurePage: async ({page}, use) => {
		await use(new JournalEditStructurePage(page));
	},
	journalEditTemplatePage: async ({page}, use) => {
		await use(new JournalEditTemplatePage(page));
	},
	journalPage: async ({page}, use) => {
		await use(new JournalPage(page));
	},
	journalStructurePage: async ({page}, use) => {
		await use(new JournalStructurePage(page));
	},
	journalTemplatePage: async ({page}, use) => {
		await use(new JournalTemplatePage(page));
	},
});

export {journalPagesTest};
