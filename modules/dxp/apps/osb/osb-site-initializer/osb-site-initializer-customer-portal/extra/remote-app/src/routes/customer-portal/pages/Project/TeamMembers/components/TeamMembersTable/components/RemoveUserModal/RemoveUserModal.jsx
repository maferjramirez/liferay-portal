/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Button} from '@clayui/core';
import ClayIcon from '@clayui/icon';
import ClayModal from '@clayui/modal';
import classNames from 'classnames';
import {memo} from 'react';
import i18n from '../../../../../../../../../common/I18n';

const RemoveUserModal = ({observer, onClose, onRemove, removing}) => (
	<ClayModal center className="modal-danger" observer={observer}>
		<ClayModal.Header>
			<span className="modal-title-indicator">
				<ClayIcon symbol="exclamation-full" />
			</span>

			{i18n.translate('remove-user')}
		</ClayModal.Header>

		<ClayModal.Body>
			<p className="my-5 text-neutral-10">
				{i18n.translate(
					'are-you-sure-you-want-to-remove-this-team-member-from-the-project'
				)}
			</p>
		</ClayModal.Body>

		<ClayModal.Footer
			first={
				<Button displayType="secondary" onClick={onClose}>
					{i18n.translate('cancel')}
				</Button>
			}
			last={
				<Button
					className={classNames('bg-danger d-flex ml-3', {
						'cp-deactivate-loading': removing,
					})}
					disabled={removing}
					onClick={onRemove}
				>
					{removing ? (
						<>
							<span className="cp-spinner mr-2 mt-1 spinner-border spinner-border-sm" />
							{i18n.translate('removing')}
						</>
					) : (
						`${i18n.translate('remove')}`
					)}
				</Button>
			}
		/>
	</ClayModal>
);

export default memo(RemoveUserModal);
