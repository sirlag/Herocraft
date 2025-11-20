import type { Actions, PageServerLoad } from './$types.js';
import { resetPasswordSchema } from './PasswordResetSchema.ts';
import { superValidate } from 'sveltekit-superforms';
import { zod } from 'sveltekit-superforms/adapters';
import { fail, redirect } from '@sveltejs/kit';
import { AccountURLs } from '$lib/routes.ts';

export const load: PageServerLoad = async () => {
	return {
		form: await superValidate(zod(resetPasswordSchema)),
	}
}

export const actions: Actions = {
	default: async (event) => {
		const form = await superValidate(event, zod(resetPasswordSchema));
		let token = event.params['token']
		if (!form.valid) {
			return fail(400, {
				form
			});
		}

		let data = form.data;

		let resetResponse = await fetch(new URL(AccountURLs.resetPassword), {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json'
			},
			body: JSON.stringify({password: data.password, token } )
		})

		if (!resetResponse.ok) {
			console.error('Unable to reset password');
			return fail(500, {
				form
			});
		}

  // In SvelteKit, redirects must be thrown from actions
  throw redirect(303, '/account/reset/success')

	}
}