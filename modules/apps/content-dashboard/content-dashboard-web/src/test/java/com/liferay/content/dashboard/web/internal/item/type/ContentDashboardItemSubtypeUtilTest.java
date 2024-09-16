/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.content.dashboard.web.internal.item.type;

import com.liferay.content.dashboard.info.item.ClassNameClassPKInfoItemIdentifier;
import com.liferay.content.dashboard.item.type.ContentDashboardItemSubtype;
import com.liferay.content.dashboard.item.type.ContentDashboardItemSubtypeFactory;
import com.liferay.content.dashboard.item.type.ContentDashboardItemSubtypeFactoryRegistry;
import com.liferay.info.item.InfoItemIdentifier;
import com.liferay.info.item.InfoItemReference;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Cristina González
 */
public class ContentDashboardItemSubtypeUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testToContentDashboardItemSubtypeByClassNameAndClassPK()
		throws PortalException {

		ContentDashboardItemSubtype contentDashboardItemSubtype =
			_getContentDashboardItemSubtype();

		ContentDashboardItemSubtypeFactory contentDashboardItemSubtypeFactory =
			_getContentDashboardItemSubtypeFactory(contentDashboardItemSubtype);

		Assert.assertEquals(
			contentDashboardItemSubtype,
			ContentDashboardItemSubtypeUtil.toContentDashboardItemSubtype(
				_getContentDashboardItemSubtypeFactoryRegistry(
					contentDashboardItemSubtype,
					contentDashboardItemSubtypeFactory),
				contentDashboardItemSubtype.getInfoItemReference()));
	}

	@Test
	public void testToContentDashboardItemSubtypeByClassNameAndClassPKWithoutContentDashboardItemSubtypeFactory() {
		ContentDashboardItemSubtype contentDashboardItemSubtype =
			_getContentDashboardItemSubtype();

		Assert.assertNull(
			ContentDashboardItemSubtypeUtil.toContentDashboardItemSubtype(
				_getContentDashboardItemSubtypeFactoryRegistry(
					contentDashboardItemSubtype, null),
				contentDashboardItemSubtype.getInfoItemReference()));
	}

	@Test
	public void testToContentDashboardItemSubtypeByJSONObjectWithoutContentDashboardItemSubtypeFactory()
		throws JSONException {

		ContentDashboardItemSubtype contentDashboardItemSubtype =
			_getContentDashboardItemSubtype();

		Assert.assertTrue(
			ContentDashboardItemSubtypeUtil.toContentDashboardItemSubtypes(
				_getContentDashboardItemSubtypeFactoryRegistry(
					contentDashboardItemSubtype, null),
				_getContentDashboardItemSubtypesJSONString(
					Arrays.asList(contentDashboardItemSubtype))
			).isEmpty());
	}

	@Test
	public void testToContentDashboardItemSubtypeByStringWithoutContentDashboardItemSubtypeFactory() {
		ContentDashboardItemSubtype contentDashboardItemSubtype =
			_getContentDashboardItemSubtype();

		Assert.assertTrue(
			ContentDashboardItemSubtypeUtil.toContentDashboardItemSubtypes(
				_getContentDashboardItemSubtypeFactoryRegistry(
					contentDashboardItemSubtype, null),
				_getContentDashboardItemSubtypesJSONString(
					Arrays.asList(contentDashboardItemSubtype))
			).isEmpty());
	}

	@Test
	public void testToContentDashboardItemSubtypesByJSONObject()
		throws PortalException {

		ContentDashboardItemSubtype contentDashboardItemSubtype =
			_getContentDashboardItemSubtype();

		ContentDashboardItemSubtypeFactory contentDashboardItemSubtypeFactory =
			_getContentDashboardItemSubtypeFactory(contentDashboardItemSubtype);

		List<ContentDashboardItemSubtype> contentDashboardItemSubtypes =
			ContentDashboardItemSubtypeUtil.toContentDashboardItemSubtypes(
				_getContentDashboardItemSubtypeFactoryRegistry(
					contentDashboardItemSubtype,
					contentDashboardItemSubtypeFactory),
				_getContentDashboardItemSubtypesJSONString(
					Arrays.asList(contentDashboardItemSubtype)));

		Assert.assertEquals(
			contentDashboardItemSubtypes.toString(), 1,
			contentDashboardItemSubtypes.size());
		Assert.assertEquals(
			contentDashboardItemSubtype, contentDashboardItemSubtypes.get(0));
	}

	@Test
	public void testToToContentDashboardItemSubtypesByJSONObjectWithTwoItems()
		throws PortalException {

		ContentDashboardItemSubtype contentDashboardItemSubtype1 =
			_getContentDashboardItemSubtype();

		ContentDashboardItemSubtype contentDashboardItemSubtype2 =
			_getContentDashboardItemSubtype();

		List<ContentDashboardItemSubtype> contentDashboardItemSubtypes =
			ContentDashboardItemSubtypeUtil.toContentDashboardItemSubtypes(
				_getContentDashboardItemSubtypeFactoryRegistry(
					Arrays.asList(
						contentDashboardItemSubtype1,
						contentDashboardItemSubtype2)),
				_getContentDashboardItemSubtypesJSONString(
					Arrays.asList(
						contentDashboardItemSubtype1,
						contentDashboardItemSubtype2)));

		Assert.assertEquals(
			contentDashboardItemSubtypes.toString(), 2,
			contentDashboardItemSubtypes.size());
		Assert.assertEquals(
			contentDashboardItemSubtype1, contentDashboardItemSubtypes.get(0));
		Assert.assertEquals(
			contentDashboardItemSubtype2, contentDashboardItemSubtypes.get(1));
	}

	private ContentDashboardItemSubtype _getContentDashboardItemSubtype() {
		String className = RandomTestUtil.randomString();
		Long classPK = RandomTestUtil.randomLong();

		return new ContentDashboardItemSubtype() {

			@Override
			public String getFullLabel(Locale locale) {
				return null;
			}

			@Override
			public InfoItemReference getInfoItemReference() {
				return new InfoItemReference(
					className,
					new ClassNameClassPKInfoItemIdentifier(className, classPK));
			}

			@Override
			public String getLabel(Locale locale) {
				return null;
			}

			@Override
			public String toJSONString(Locale locale) {
				return JSONUtil.put(
					"className", className
				).put(
					"classPK", classPK
				).toString();
			}

		};
	}

	private ContentDashboardItemSubtypeFactory
			_getContentDashboardItemSubtypeFactory(
				ContentDashboardItemSubtype contentDashboardItemSubtype)
		throws PortalException {

		ContentDashboardItemSubtypeFactory contentDashboardItemSubtypeFactory =
			Mockito.mock(ContentDashboardItemSubtypeFactory.class);

		InfoItemReference infoItemReference =
			contentDashboardItemSubtype.getInfoItemReference();

		InfoItemIdentifier infoItemIdentifier =
			infoItemReference.getInfoItemIdentifier();

		Assert.assertTrue(
			infoItemIdentifier instanceof ClassNameClassPKInfoItemIdentifier);

		ClassNameClassPKInfoItemIdentifier classNameClassPKInfoItemIdentifier =
			(ClassNameClassPKInfoItemIdentifier)
				infoItemReference.getInfoItemIdentifier();

		Mockito.when(
			contentDashboardItemSubtypeFactory.create(
				classNameClassPKInfoItemIdentifier.getClassPK())
		).thenReturn(
			contentDashboardItemSubtype
		);

		return contentDashboardItemSubtypeFactory;
	}

	private ContentDashboardItemSubtypeFactoryRegistry
		_getContentDashboardItemSubtypeFactoryRegistry(
			ContentDashboardItemSubtype contentDashboardItemSubtype,
			ContentDashboardItemSubtypeFactory
				contentDashboardItemSubtypeFactory) {

		ContentDashboardItemSubtypeFactoryRegistry
			contentDashboardItemSubtypeFactoryRegistry = Mockito.mock(
				ContentDashboardItemSubtypeFactoryRegistry.class);

		InfoItemReference infoItemReference =
			contentDashboardItemSubtype.getInfoItemReference();

		Mockito.when(
			contentDashboardItemSubtypeFactoryRegistry.
				getContentDashboardItemSubtypeFactory(
					infoItemReference.getClassName())
		).thenReturn(
			contentDashboardItemSubtypeFactory
		);

		return contentDashboardItemSubtypeFactoryRegistry;
	}

	private ContentDashboardItemSubtypeFactoryRegistry
			_getContentDashboardItemSubtypeFactoryRegistry(
				List<ContentDashboardItemSubtype> contentDashboardItemSubtypes)
		throws PortalException {

		ContentDashboardItemSubtypeFactoryRegistry
			contentDashboardItemSubtypeFactoryRegistry = Mockito.mock(
				ContentDashboardItemSubtypeFactoryRegistry.class);

		for (ContentDashboardItemSubtype contentDashboardItemSubtype :
				contentDashboardItemSubtypes) {

			ContentDashboardItemSubtypeFactory
				contentDashboardItemSubtypeFactory =
					_getContentDashboardItemSubtypeFactory(
						contentDashboardItemSubtype);

			InfoItemReference infoItemReference =
				contentDashboardItemSubtype.getInfoItemReference();

			Mockito.when(
				contentDashboardItemSubtypeFactoryRegistry.
					getContentDashboardItemSubtypeFactory(
						infoItemReference.getClassName())
			).thenReturn(
				contentDashboardItemSubtypeFactory
			);
		}

		return contentDashboardItemSubtypeFactoryRegistry;
	}

	private String _getContentDashboardItemSubtypesJSONString(
		List<ContentDashboardItemSubtype> contentDashboardItemSubtypes) {

		JSONArray jsonArray = JSONFactoryUtil.createJSONArray();

		for (ContentDashboardItemSubtype curContentDashboardItemSubtype :
				contentDashboardItemSubtypes) {

			InfoItemReference curInfoItemReference =
				curContentDashboardItemSubtype.getInfoItemReference();

			if (curInfoItemReference.getInfoItemIdentifier() instanceof
					ClassNameClassPKInfoItemIdentifier) {

				ClassNameClassPKInfoItemIdentifier
					classNameClassPKInfoItemIdentifier =
						(ClassNameClassPKInfoItemIdentifier)
							curInfoItemReference.getInfoItemIdentifier();

				JSONObject jsonObject = _getJSONObject(
					jsonArray,
					classNameClassPKInfoItemIdentifier.getClassName(),
					curInfoItemReference.getClassName());

				if (jsonObject != null) {
					JSONArray classPKsJSONArray = jsonObject.getJSONArray(
						"classPKs");

					if (classPKsJSONArray != null) {
						classPKsJSONArray.put(
							classNameClassPKInfoItemIdentifier.getClassPK());
					}
					else {
						jsonObject.put(
							"classPKs",
							JSONUtil.put(
								classNameClassPKInfoItemIdentifier.
									getClassPK()));
					}
				}
				else {
					jsonArray.put(
						JSONUtil.put(
							"className",
							classNameClassPKInfoItemIdentifier.getClassName()
						).put(
							"classPKs",
							JSONUtil.put(
								String.valueOf(
									classNameClassPKInfoItemIdentifier.
										getClassPK()))
						).put(
							"entryClassName",
							curInfoItemReference.getClassName()
						));
				}
			}
			else {
				jsonArray.put(
					JSONUtil.put(
						"entryClassName", curInfoItemReference.getClassName()));
			}
		}

		return jsonArray.toString();
	}

	private JSONObject _getJSONObject(
		JSONArray jsonArray, String className, String entryClassName) {

		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);

			if (jsonObject == null) {
				continue;
			}

			if (Objects.equals(jsonObject.getString("className"), className) &&
				Objects.equals(
					jsonObject.getString("entryClassName"), entryClassName)) {

				return jsonObject;
			}
		}

		return null;
	}

}