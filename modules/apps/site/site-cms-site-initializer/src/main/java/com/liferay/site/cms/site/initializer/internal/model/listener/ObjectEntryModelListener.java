/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.model.listener;

import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.model.AssetTag;
import com.liferay.asset.kernel.service.AssetEntryLocalService;
import com.liferay.asset.kernel.service.AssetTagLocalService;
import com.liferay.depot.constants.DepotConstants;
import com.liferay.depot.model.DepotEntry;
import com.liferay.depot.service.DepotEntryLocalService;
import com.liferay.object.constants.ObjectDefinitionConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.rest.filter.factory.FilterFactory;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.petra.sql.dsl.expression.Predicate;
import com.liferay.portal.kernel.audit.AuditRouter;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.security.audit.event.generators.util.Attribute;
import com.liferay.portal.security.audit.event.generators.util.AuditMessageBuilder;
import com.liferay.portal.workflow.kaleo.model.KaleoTaskInstanceToken;
import com.liferay.portal.workflow.kaleo.service.KaleoTaskInstanceTokenLocalService;
import com.liferay.site.cms.site.initializer.util.CMSDefaultPermissionUtil;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Stefano Motta
 */
@Component(service = ModelListener.class)
public class ObjectEntryModelListener extends BaseModelListener<ObjectEntry> {

	@Override
	public void onAfterCreate(ObjectEntry objectEntry)
		throws ModelListenerException {

		try {
			if (_isCMSObjectEntry(objectEntry)) {
				_route(objectEntry);

				CMSDefaultPermissionUtil.setObjectEntryResourcePermissions(
					objectEntry, _filterFactory);
			}
		}
		catch (Exception exception) {
			throw new ModelListenerException(exception);
		}
	}

	@Override
	public void onAfterRemove(ObjectEntry objectEntry)
		throws ModelListenerException {

		try {
			if (_isCMSObjectEntry(objectEntry)) {
				_route(objectEntry);
			}
		}
		catch (Exception exception) {
			throw new ModelListenerException(exception);
		}
	}

	@Override
	public void onAfterUpdate(
			ObjectEntry originalObjectEntry, ObjectEntry objectEntry)
		throws ModelListenerException {

		try {
			if (!_isCMSObjectEntry(objectEntry)) {
				return;
			}

			_route(objectEntry);

			if (originalObjectEntry.getObjectEntryFolderId() !=
					objectEntry.getObjectEntryFolderId()) {

				CMSDefaultPermissionUtil.setObjectEntryResourcePermissions(
					objectEntry, _filterFactory);
			}

			Indexer<KaleoTaskInstanceToken> indexer =
				IndexerRegistryUtil.nullSafeGetIndexer(
					KaleoTaskInstanceToken.class);

			for (KaleoTaskInstanceToken kaleoTaskInstanceToken :
					_kaleoTaskInstanceTokenLocalService.
						getKaleoTaskInstanceTokens(
							objectEntry.getModelClassName(),
							objectEntry.getObjectEntryId())) {

				indexer.reindex(kaleoTaskInstanceToken);
			}
		}
		catch (Exception exception) {
			throw new ModelListenerException(exception);
		}
	}

	private boolean _isCMSObjectEntry(ObjectEntry objectEntry)
		throws Exception {

		if (!FeatureFlagManagerUtil.isEnabled(
				objectEntry.getCompanyId(), "LPD-17564") ||
			(objectEntry.getGroupId() == 0)) {

			return false;
		}

		Group group = _groupLocalService.fetchGroup(objectEntry.getGroupId());

		if ((group == null) || !group.isDepot()) {
			return false;
		}

		DepotEntry depotEntry = _depotEntryLocalService.fetchDepotEntry(
			group.getClassPK());

		if ((depotEntry == null) ||
			(depotEntry.getType() != DepotConstants.TYPE_SPACE)) {

			return false;
		}

		return true;
	}

	private void _route(
			AssetTag assetTag, List<Attribute> attributes, String eventType,
			ObjectDefinition taskObjectDefinition)
		throws Exception {

		for (long assetEntryId :
				_assetTagLocalService.getAssetEntryPrimaryKeys(
					assetTag.getTagId())) {

			AssetEntry assetEntry = _assetEntryLocalService.fetchEntry(
				assetEntryId);

			if (!StringUtil.equals(
					assetEntry.getClassName(),
					taskObjectDefinition.getClassName())) {

				continue;
			}

			_auditRouter.route(
				AuditMessageBuilder.buildAuditMessage(
					assetEntry.getClassName(), assetEntry.getClassPK(),
					eventType, attributes));
		}
	}

	private void _route(ObjectEntry objectEntry) throws Exception {
		if (!FeatureFlagManagerUtil.isEnabled(
				objectEntry.getCompanyId(), "LPD-58677")) {

			return;
		}

		ServiceContext serviceContext =
			ServiceContextThreadLocal.getServiceContext();

		if (serviceContext == null) {
			return;
		}

		ObjectDefinition taskObjectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_CMP_TASK", objectEntry.getCompanyId());

		if (taskObjectDefinition == null) {
			return;
		}

		Set<String> newAssetTagNames = SetUtil.fromArray(
			serviceContext.getAssetTagNames());
		Set<String> oldAssetTagNames = SetUtil.fromArray(
			_assetTagLocalService.getTagNames(
				objectEntry.getModelClassName(),
				objectEntry.getObjectEntryId()));

		_route(
			SetUtil.asymmetricDifference(newAssetTagNames, oldAssetTagNames),
			Collections.singletonList(
				new Attribute(objectEntry.getTitleValue())),
			"CMP_ADD_ASSET", taskObjectDefinition);
		_route(
			SetUtil.asymmetricDifference(oldAssetTagNames, newAssetTagNames),
			Collections.singletonList(
				new Attribute(objectEntry.getTitleValue())),
			"CMP_REMOVE_ASSET", taskObjectDefinition);
	}

	private void _route(
			Set<String> assetTagNames, List<Attribute> attributes,
			String eventType, ObjectDefinition taskObjectDefinition)
		throws Exception {

		for (String assetTagName : assetTagNames) {
			if (!StringUtil.startsWith(
					assetTagName,
					taskObjectDefinition.getExternalReferenceCode())) {

				continue;
			}

			for (AssetTag assetTag :
					_assetTagLocalService.search(
						new long[0], assetTagName, QueryUtil.ALL_POS,
						QueryUtil.ALL_POS)) {

				_route(assetTag, attributes, eventType, taskObjectDefinition);
			}
		}
	}

	@Reference
	private AssetEntryLocalService _assetEntryLocalService;

	@Reference
	private AssetTagLocalService _assetTagLocalService;

	@Reference
	private AuditRouter _auditRouter;

	@Reference
	private DepotEntryLocalService _depotEntryLocalService;

	@Reference(
		target = "(filter.factory.key=" + ObjectDefinitionConstants.STORAGE_TYPE_DEFAULT + ")"
	)
	private FilterFactory<Predicate> _filterFactory;

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private KaleoTaskInstanceTokenLocalService
		_kaleoTaskInstanceTokenLocalService;

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

}