/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

/// <reference types="react" />

interface Props {
	observer: any;
	onImport: (overwriteStrategy?: OverwriteStrategy) => void;
	onOpenChange: (value: boolean) => void;
}
declare const OPTIONS: readonly [
	{
		readonly label: string;
		readonly value: 'do_not_import';
	},
	{
		readonly label: string;
		readonly value: 'overwrite';
	},
	{
		readonly label: string;
		readonly value: 'keep_both';
	}
];
export declare type OverwriteStrategy = typeof OPTIONS[number]['value'];
declare function ImportOptionsModal({
	observer,
	onImport,
	onOpenChange,
}: Props): JSX.Element;
export default ImportOptionsModal;
