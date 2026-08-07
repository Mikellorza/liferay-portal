/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.search.spi.model.index.contributor;

import com.liferay.object.constants.ObjectDefinitionConstants;
import com.liferay.object.model.ObjectEntry;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.HtmlParser;
import com.liferay.portal.search.spi.model.index.contributor.ModelDocumentContributor;
import com.liferay.site.cms.site.initializer.internal.search.similarity.CMSContentTextSimilarityTextExtractor;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * @author Mikel Lorza
 */
@Component(service = {})
public class CMSContentTextSimilarityContributorRegistrar {

	@Activate
	protected void activate(BundleContext bundleContext)
		throws InvalidSyntaxException {

		_bundleContext = bundleContext;

		_modelDocumentContributor =
			new CMSContentTextSimilarityModelDocumentContributor(
				new CMSContentTextSimilarityTextExtractor(_htmlParser));

		_serviceRegistration = bundleContext.registerService(
			(Class<ModelDocumentContributor<?>>)
				(Class<?>)ModelDocumentContributor.class,
			_modelDocumentContributor,
			HashMapDictionaryBuilder.<String, Object>put(
				"indexer.class.name", ObjectEntry.class.getName()
			).build());

		_serviceTracker = new ServiceTracker<>(
			bundleContext,
			bundleContext.createFilter(
				StringBundler.concat(
					"(&(objectClass=", Indexer.class.getName(),
					")(indexer.class.name=",
					ObjectDefinitionConstants.
						CLASS_NAME_PREFIX_CUSTOM_OBJECT_DEFINITION,
					"*))")),
			new IndexerServiceTrackerCustomizer());

		_serviceTracker.open();
	}

	@Deactivate
	protected void deactivate() {
		if (_serviceTracker != null) {
			_serviceTracker.close();
		}

		if (_serviceRegistration != null) {
			_serviceRegistration.unregister();
		}
	}

	private BundleContext _bundleContext;

	@Reference
	private HtmlParser _htmlParser;

	private CMSContentTextSimilarityModelDocumentContributor
		_modelDocumentContributor;
	private ServiceRegistration<ModelDocumentContributor<?>>
		_serviceRegistration;
	private ServiceTracker
		<Indexer<?>, ServiceRegistration<ModelDocumentContributor<?>>>
			_serviceTracker;

	private class IndexerServiceTrackerCustomizer
		implements ServiceTrackerCustomizer
			<Indexer<?>, ServiceRegistration<ModelDocumentContributor<?>>> {

		@Override
		public ServiceRegistration<ModelDocumentContributor<?>> addingService(
			ServiceReference<Indexer<?>> serviceReference) {

			Object className = serviceReference.getProperty(
				"indexer.class.name");

			if (className == null) {
				return null;
			}

			return _bundleContext.registerService(
				(Class<ModelDocumentContributor<?>>)
					(Class<?>)ModelDocumentContributor.class,
				_modelDocumentContributor,
				HashMapDictionaryBuilder.<String, Object>put(
					"indexer.class.name", className
				).build());
		}

		@Override
		public void modifiedService(
			ServiceReference<Indexer<?>> serviceReference,
			ServiceRegistration<ModelDocumentContributor<?>>
				serviceRegistration) {
		}

		@Override
		public void removedService(
			ServiceReference<Indexer<?>> serviceReference,
			ServiceRegistration<ModelDocumentContributor<?>>
				serviceRegistration) {

			if (serviceRegistration != null) {
				serviceRegistration.unregister();
			}

			_bundleContext.ungetService(serviceReference);
		}

	}

}