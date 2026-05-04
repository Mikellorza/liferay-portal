/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton from '@clayui/button';
import ClayIcon from '@clayui/icon';
import ClayLayout from '@clayui/layout';
import ClaySticker from '@clayui/sticker';
import {IBaseFilterState, IFDSState} from '@liferay/frontend-data-set-web';
import {useLiferayState} from '@liferay/frontend-js-state-web/react';
import classNames from 'classnames';
import React, {useCallback, useEffect, useRef, useState} from 'react';

import {cmsAllFDSAtom} from './atoms';

import './CMSAllQuickFilters.scss';

const STATUS_APPROVED = 0;
const STATUS_DRAFT = 2;
const STATUS_EXPIRED = 3;

const EXPIRING_SOON_DAYS = 7;

const QUICK_FILTER_TYPES = {
	EXPIRED: 'expired',
	EXPIRING_SOON: 'expiringSoon',
	IN_DRAFT: 'inDraft',
	REVIEW_DATE_OVERDUE: 'reviewDateOverdue',
};

const STATUS_FILTER_ID = 'status';
const DATE_EXPIRATION_FILTER_ID = 'dateExpiration';
const DATE_REVIEW_FILTER_ID = 'dateReview';

interface QuickFilterCounts {
	expired: number;
	expiringSoon: number;
	inDraft: number;
	reviewDateOverdue: number;
}

function toDatePart(date: Date) {
	return {
		day: date.getDate(),
		month: date.getMonth() + 1,
		year: date.getFullYear(),
	};
}

function clearedFilter(filter: IBaseFilterState): IBaseFilterState {
	return {
		...filter,
		active: false,
		selectedData: {
			exclude: false,
			selectedItems: [],
		},
	};
}

function QuickFilterButton({
	active,
	count,
	displayType,
	icon,
	label,
	onClick,
}: {
	active: boolean;
	count: number;
	displayType:
		| 'secondary'
		| 'success'
		| 'warning'
		| 'danger'
		| 'info'
		| 'unstyled';
	icon: string;
	label: string;
	onClick: () => void;
}) {
	return (
		<ClayButton
			className={classNames('quick-filter-button', {active})}
			displayType="secondary"
			onClick={onClick}
		>
			<div className="align-items-center d-flex">
				<ClaySticker
					className="rounded"
					displayType={displayType}
					size="lg"
				>
					<ClayIcon symbol={icon} />
				</ClaySticker>

				<div className="ml-2">
					<div className="text-dark">{count || 0}</div>

					<div className="text-3 text-secondary text-weight-normal">
						{label}
					</div>
				</div>
			</div>
		</ClayButton>
	);
}

export default function CMSAllQuickFilters({
	quickFilterCounts,
}: {
	quickFilterCounts?: QuickFilterCounts;
}) {
	const [activeQuickFilter, setActiveQuickFilter] = useState<string | null>(
		null
	);

	const [allFDSState, setAllFDSState] =
		useLiferayState<IFDSState>(cmsAllFDSAtom);

	const isQuickFilterChangeRef = useRef(false);

	const counts: QuickFilterCounts = {
		expired: quickFilterCounts?.expired ?? 0,
		expiringSoon: quickFilterCounts?.expiringSoon ?? 0,
		inDraft: quickFilterCounts?.inDraft ?? 0,
		reviewDateOverdue: quickFilterCounts?.reviewDateOverdue ?? 0,
	};

	const handleInDraftClick = useCallback(() => {
		setActiveQuickFilter(QUICK_FILTER_TYPES.IN_DRAFT);

		setAllFDSState({
			...allFDSState,
			filters: allFDSState.filters.map((filter: IBaseFilterState) => {
				if (filter.id === STATUS_FILTER_ID) {
					return {
						...filter,
						active: true,
						selectedData: {
							exclude: false,
							selectedItems: [
								{
									label: Liferay.Language.get('draft'),
									value: STATUS_DRAFT,
								},
							],
						},
					};
				}

				return clearedFilter(filter);
			}),
		});

		isQuickFilterChangeRef.current = true;
	}, [allFDSState, setAllFDSState]);

	const handleExpiringSoonClick = useCallback(() => {
		setActiveQuickFilter(QUICK_FILTER_TYPES.EXPIRING_SOON);

		const now = new Date();

		const threshold = new Date();

		threshold.setDate(now.getDate() + EXPIRING_SOON_DAYS);

		setAllFDSState({
			...allFDSState,
			filters: allFDSState.filters.map((filter: IBaseFilterState) => {
				if (filter.id === STATUS_FILTER_ID) {
					return {
						...filter,
						active: true,
						selectedData: {
							exclude: false,
							selectedItems: [
								{
									label: Liferay.Language.get('approved'),
									value: STATUS_APPROVED,
								},
							],
						},
					};
				}

				if (filter.id === DATE_EXPIRATION_FILTER_ID) {
					return {
						...filter,
						active: true,
						selectedData: {
							exclude: false,
							from: toDatePart(now),
							to: toDatePart(threshold),
						},
					};
				}

				return clearedFilter(filter);
			}),
		});

		isQuickFilterChangeRef.current = true;
	}, [allFDSState, setAllFDSState]);

	const handleExpiredClick = useCallback(() => {
		setActiveQuickFilter(QUICK_FILTER_TYPES.EXPIRED);

		setAllFDSState({
			...allFDSState,
			filters: allFDSState.filters.map((filter: IBaseFilterState) => {
				if (filter.id === STATUS_FILTER_ID) {
					return {
						...filter,
						active: true,
						selectedData: {
							exclude: false,
							selectedItems: [
								{
									label: Liferay.Language.get('expired'),
									value: STATUS_EXPIRED,
								},
							],
						},
					};
				}

				return clearedFilter(filter);
			}),
		});

		isQuickFilterChangeRef.current = true;
	}, [allFDSState, setAllFDSState]);

	const handleReviewDateOverdueClick = useCallback(() => {
		setActiveQuickFilter(QUICK_FILTER_TYPES.REVIEW_DATE_OVERDUE);

		const now = new Date();

		setAllFDSState({
			...allFDSState,
			filters: allFDSState.filters.map((filter: IBaseFilterState) => {
				if (filter.id === DATE_REVIEW_FILTER_ID) {
					return {
						...filter,
						active: true,
						selectedData: {
							exclude: false,
							from: null,
							to: toDatePart(now),
						},
					};
				}

				return clearedFilter(filter);
			}),
		});

		isQuickFilterChangeRef.current = true;
	}, [allFDSState, setAllFDSState]);

	useEffect(() => {
		if (isQuickFilterChangeRef.current) {
			isQuickFilterChangeRef.current = false;

			return;
		}

		setActiveQuickFilter(null);
	}, [allFDSState.filters]);

	return (
		<div className="lfr-cms__all-quick-filters-container">
			<ClayLayout.ContainerFluid
				className="c-pb-4 c-pt-2 c-px-4"
				size={false}
			>
				<ClayLayout.Row>
					<ClayLayout.Col className="c-px-2" size={3}>
						<QuickFilterButton
							active={
								activeQuickFilter ===
								QUICK_FILTER_TYPES.IN_DRAFT
							}
							count={counts.inDraft}
							displayType="secondary"
							icon="pencil"
							label={Liferay.Language.get('in-draft')}
							onClick={handleInDraftClick}
						/>
					</ClayLayout.Col>

					<ClayLayout.Col className="c-px-2" size={3}>
						<QuickFilterButton
							active={
								activeQuickFilter ===
								QUICK_FILTER_TYPES.EXPIRING_SOON
							}
							count={counts.expiringSoon}
							displayType="warning"
							icon="flag-full"
							label={Liferay.Language.get('expiring-soon')}
							onClick={handleExpiringSoonClick}
						/>
					</ClayLayout.Col>

					<ClayLayout.Col className="c-px-2" size={3}>
						<QuickFilterButton
							active={
								activeQuickFilter === QUICK_FILTER_TYPES.EXPIRED
							}
							count={counts.expired}
							displayType="danger"
							icon="warning-full"
							label={Liferay.Language.get('expired')}
							onClick={handleExpiredClick}
						/>
					</ClayLayout.Col>

					<ClayLayout.Col className="c-px-2" size={3}>
						<QuickFilterButton
							active={
								activeQuickFilter ===
								QUICK_FILTER_TYPES.REVIEW_DATE_OVERDUE
							}
							count={counts.reviewDateOverdue}
							displayType="info"
							icon="date-time"
							label={Liferay.Language.get('review-date-overdue')}
							onClick={handleReviewDateOverdueClick}
						/>
					</ClayLayout.Col>
				</ClayLayout.Row>
			</ClayLayout.ContainerFluid>
		</div>
	);
}
