/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.exportimport.kernel.lar.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.exportimport.kernel.lar.DataLevel;
import com.liferay.exportimport.kernel.lar.PortletDataHandler;
import com.liferay.exportimport.test.util.lar.BasePortletDataHandlerTestCase;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portlet.PortletPreferencesImpl;

import javax.portlet.PortletPreferences;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

/**
 * @author Mikel Lorza
 */
@RunWith(Arquillian.class)
public class DummyPortletPortletDataHandlerTest
	extends BasePortletDataHandlerTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Override
	@Test
	public void testExportImportData() throws Exception {
		initContext();

		PortletPreferences portletPreferences = new PortletPreferencesImpl();

		portletDataContext.setEndDate(getEndDate());

		String exportData = portletDataHandler.exportData(
			portletDataContext, portletId, portletPreferences);

		Assert.assertNotNull(exportData);

		portletDataHandler.importData(
			portletDataContext, portletId, portletPreferences, exportData);

		Bundle bundle = FrameworkUtil.getBundle(
			DummyPortletPortletDataHandlerTest.class);

		BundleContext bundleContext = bundle.getBundleContext();

		bundleContext.ungetService(
			bundleContext.getServiceReference(
				"com.liferay.wiki.internal.exportimport.data.handler.WikiAdminPortletDataHandler"));

		Assert.assertNull(
			portletDataHandler.exportData(
				portletDataContext, portletId, portletPreferences));

		Assert.assertNull(
			portletDataHandler.importData(
				portletDataContext, portletId, portletPreferences, null));
	}

	@Override
	protected void addStagedModels() throws Exception {
	}

	@Override
	protected DataLevel getDataLevel() {
		return DataLevel.SITE;
	}

	@Override
	protected String getPortletId() {
		return "com_liferay_wiki_web_portlet_WikiAdminPortlet";
	}

	@Override
	protected boolean isDataPortalLevel() {
		return false;
	}

	@Override
	protected boolean isDataPortletInstanceLevel() {
		return false;
	}

	@Override
	protected boolean isDataSiteLevel() {
		return true;
	}

	@Override
	protected boolean isDisplayPortlet() {
		return false;
	}

	@Inject(
		filter = "javax.portlet.name=com_liferay_wiki_web_portlet_WikiAdminPortlet"
	)
	PortletDataHandler _portletDataHandler;

}