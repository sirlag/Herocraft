import type { Actions, PageServerLoad } from './$types.js';

import { superValidate } from 'sveltekit-superforms';
import { zod } from 'sveltekit-superforms/adapters';
import { forgotSchema } from './ForgotSchema.ts';
import { fail, redirect } from '@sveltejs/kit';
import { AccountURLs } from '$lib/routes.ts';

export const load: PageServerLoad = async ()=> {
	return {
		form: await superValidate(zod(forgotSchema))
	}
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

		let forgotResponse = await fetch(new URL(AccountURLs.forgotPassword), {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json'
			},
			body: JSON.stringify({...data})
		});


		if (!forgotResponse.ok) {
			console.error("Unable to send password Reset", forgotResponse);
			return fail(500, {
				form
			})
		}

		redirect(303, "/account/forgot/sent")
	}
}