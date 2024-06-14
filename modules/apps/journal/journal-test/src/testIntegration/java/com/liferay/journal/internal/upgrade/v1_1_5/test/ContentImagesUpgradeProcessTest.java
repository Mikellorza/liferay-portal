/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.journal.internal.upgrade.v1_1_5.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.cache.MultiVMPool;
import com.liferay.portal.kernel.dao.db.DB;
import com.liferay.portal.kernel.dao.db.DBManagerUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.test.log.LogCapture;
import com.liferay.portal.test.log.LoggerTestUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.upgrade.registry.UpgradeStepRegistrator;
import com.liferay.portal.upgrade.test.util.UpgradeTestUtil;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Mikel Lorza
 */
@RunWith(Arquillian.class)
public class ContentImagesUpgradeProcessTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_db = DBManagerUtil.getDB();

		_createJournalArticleTable();
	}

	@Test
	public void testUpgradeProcess() throws Exception {
		_db.runSQL(
			StringBundler.concat(
				"INSERT INTO JournalArticle VALUES ",
				"('01137418-6a99-7a0e-5dcd-633acf2fa1fa',32913,32914,20142,",
				"20115,20155,'Test Test','2024-06-14 07:39:38.391000',",
				"'2024-06-14 07:39:38.406000',0,0,0,'/','32912',1,'<?xml ",
				"version=\\'1.0\\' encoding=\\'UTF-8\\'?><root ",
				"available-locales=\\\"en_US\\\" default-locale=\\\"en_US\\\">",
				"<Title language-id=\\\"en_US\\\">structure D&amp;m ",
				"selecting</Title></root>','structure-d-m-selecting',",
				"'','<?xml version=\\\"1.0\\\"?>\\n\\n<root ",
				"available-locales=\\\"en_US\\\" ",
				"default-locale=\\\"en_US\\\">\\n\t<dynamic-element ",
				"name=\\\"Image42c1\\\" type=\\\"image\\\" ",
				"index-type=\\\"text\\\" ",
				"instance-id=\\\"fjktxaoh\\\">\\n\t\t<dynamic-content ",
				"language-id=\\\"en_US\\\" alt=\\\"\\\" ",
				"name=\\\"image3.jpeg\\\" title=\\\"image3.jpeg\\\" ",
				"type=\\\"document\\\" >",
				"<![CDATA[/documents/20142/0/image3.jpeg",
				"/05f0320c-0076-fa14-d34c-59e9d2b1f8df?t=1718350433776]]>",
				"</dynamic-content>\\n\t</dynamic-element>\\n</root>','32804',",
				"'32848',NULL,'2024-06-14 07:39:00.000000',NULL,NULL,1,0,",
				"32915,NULL,NULL,0,20155,'Test Test',",
				"'2024-06-14 07:39:38.406000')"));

		_runUpgrade();
	}

	private void _createJournalArticleTable() throws Exception {
		_db.runSQL("drop table if exists JournalArticle");

		_db.runSQL(
			StringBundler.concat(
				"create table JournalArticle (uuid_ VARCHAR(75) null,id_ LONG ",
				"not null primary key,resourcePrimKey LONG,groupId LONG,",
				"companyId LONG,userId LONG,userName VARCHAR(75) null,",
				"createDate DATE null,modifiedDate DATE null,folderId LONG,",
				"classNameId LONG,classPK LONG,treePath STRING null,articleId ",
				"VARCHAR(75) null,version DOUBLE,title STRING null,urlTitle ",
				"VARCHAR(150) null,description TEXT null,content TEXT null,",
				"DDMStructureKey VARCHAR(75) null,DDMTemplateKey VARCHAR(75) ",
				"null,layoutUuid VARCHAR(75) null,displayDate DATE null,",
				"expirationDate DATE null,reviewDate DATE null,indexable ",
				"BOOLEAN,smallImage BOOLEAN,smallImageId LONG,smallImageURL ",
				"STRING null,lastPublishDate DATE null,status INTEGER,",
				"statusByUserId LONG,statusByUserName VARCHAR(75) null,",
				"statusDate DATE null)"));
	}

	private void _runUpgrade() throws Exception {
		try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				_CLASS_NAME, LoggerTestUtil.OFF)) {

			UpgradeProcess upgradeProcess = UpgradeTestUtil.getUpgradeStep(
				_upgradeStepRegistrator, _CLASS_NAME);

			upgradeProcess.upgrade();

			_multiVMPool.clear();
		}
	}

	private static final String _CLASS_NAME =
		"com.liferay.journal.internal.upgrade.v1_1_5." +
			"ContentImagesUpgradeProcess";

	private static DB _db;

	@Inject(
		filter = "(&(component.name=com.liferay.journal.internal.upgrade.registry.JournalServiceUpgradeStepRegistrator))"
	)
	private static UpgradeStepRegistrator _upgradeStepRegistrator;

	@Inject
	private MultiVMPool _multiVMPool;

}