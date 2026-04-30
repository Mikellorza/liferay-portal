/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.test.rule;

import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.portal.util.PortalImpl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * @author Mikel Lorza
 */
public class DeletableSystemGroupTestRuleTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		PortalUtil portalUtil = new PortalUtil();

		portalUtil.setPortal(new PortalImpl());
	}

	@Test
	public void testAnnotateClassWithDeletableSystemGroup() throws Throwable {
		Assert.assertTrue(PortalUtil.isSystemGroup(GroupConstants.CMS));

		_evaluate(
			AnnotatedClass.class,
			() -> Assert.assertFalse(
				PortalUtil.isSystemGroup(GroupConstants.CMS)));

		Assert.assertTrue(PortalUtil.isSystemGroup(GroupConstants.CMS));
	}

	@Test
	public void testWithoutAnnotation() throws Throwable {
		Assert.assertTrue(PortalUtil.isSystemGroup(GroupConstants.CMS));

		_evaluate(
			UnannotatedClass.class,
			() -> Assert.assertTrue(
				PortalUtil.isSystemGroup(GroupConstants.CMS)));

		Assert.assertTrue(PortalUtil.isSystemGroup(GroupConstants.CMS));
	}

	private void _evaluate(Class<?> testClass, ThrowingRunnable assertions)
		throws Throwable {

		Statement statement = DeletableSystemGroupTestRule.INSTANCE.apply(
			new Statement() {

				@Override
				public void evaluate() throws Throwable {
					assertions.run();
				}

			},
			Description.createSuiteDescription(testClass));

		statement.evaluate();
	}

	@DeletableSystemGroup(groupKeys = GroupConstants.CMS)
	private static class AnnotatedClass {
	}

	private static class UnannotatedClass {
	}

	private interface ThrowingRunnable {

		public void run() throws Throwable;

	}

}