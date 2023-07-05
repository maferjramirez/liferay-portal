/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {zodResolver} from '@hookform/resolvers/zod';
import {z} from 'zod';

const zodSchema = {
	accountCreator: z.object({
		agreetOTermsAndConditions: z.boolean(),
		companyName: z.string(),
		emailAddress: z.string().email(),
		extension: z.string().optional(),
		familyName: z.string(),
		givenName: z.string(),
		industry: z.any(),
		phone: z.string().optional(),
		phoneNumber: z.string(),
	}),

	newCustomer: z.object({
		accountBriefs: z.any().optional(),
		alternateName: z.string().optional(),
		currentPassword: z.string().optional(),
		emailAddress: z.string().email(),
		familyName: z.string(),
		givenName: z.string(),
		id: z.number().optional(),
		image: z.string().optional(),
		imageBlob: z.any().optional(),
		isCustomerAccount: z.boolean().optional(),
		isPublisherAccount: z.boolean().optional(),
		newsSubscription: z.boolean(),
		password: z.string().optional(),
	}),
};

export {zodResolver};

export default zodSchema;
