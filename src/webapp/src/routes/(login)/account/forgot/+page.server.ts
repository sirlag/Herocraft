import type { Actions, PageServerLoad } from './$types.js';

import { type ErrorStatus, message, superValidate } from 'sveltekit-superforms';
import { zod } from 'sveltekit-superforms/adapters';
import { forgotSchema } from './ForgotSchema.ts';
import { fail, redirect } from '@sveltejs/kit';
import { AccountURLs } from '$lib/routes.ts';

export const load: PageServerLoad = async () => {
	return {
		form: await superValidate(zod(forgotSchema))
	};
};

export const actions: Actions = {
	default: async (event) => {
		const form = await superValidate(event, zod(forgotSchema));
		if (!form.valid) {
			return fail(400, {
				form
			});
		}

		let data = form.data;
		let body = JSON.stringify({ ...data });

		console.log(`Attempting to send ${body} to ${AccountURLs.forgotPassword}`);

		let forgotResponse = await fetch(new URL(AccountURLs.forgotPassword), {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json'
			},
			body: body
		});

		if (!forgotResponse.ok) {

			let errorBody = await forgotResponse.json();

			let errorMessage = errorBody.error == 'USER_NOT_FOUND'
				? 'Unable to Find Requested User'
				: errorBody.error == 'USER_NOT_VERIFIED'
					? 'Unable to Send Password Reset to Request User.\nPlease contact support@herocraft.app'
					: 'An Unknown Error Occurred. Try again in a few minutes.';

			console.error("An error occurred", errorMessage);

			let errorStatus: ErrorStatus = 500;
			if (forgotResponse.status >= 400 && forgotResponse.status < 600) {
				// @ts-ignore
				errorStatus = forgotResponse.status;
			}

			return message(form, errorMessage, {
				status: errorStatus,
			});
		}

		redirect(303, '/account/forgot/sent');
	}

};