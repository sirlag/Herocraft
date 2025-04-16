import type { Actions, PageServerLoad } from './$types.js';
import { type ErrorStatus, message, superValidate } from 'sveltekit-superforms';
import { formSchema } from './schema';
import { zod } from 'sveltekit-superforms/adapters';
import { fail, redirect } from '@sveltejs/kit';
import { AccountURLs } from '$lib/routes.ts'
import Cookie from 'cookie';

export const load: PageServerLoad = async () => {
	return {
		form: await superValidate(zod(formSchema))
	};
};

export const actions: Actions = {
	default: async (event) => {
		const form = await superValidate(event, zod(formSchema));
		if (!form.valid) {
			return fail(400, {
				form
			});
		}

		let data = form.data;

		let loginResponse = await fetch(new URL(AccountURLs.signin), {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json'
			},
			// credentials: 'include',
			body: JSON.stringify({ ...data })
		});

		if (!loginResponse.ok) {

			let errorBody = await loginResponse.json();

			let errorMessage = errorBody.error == 'INVALID_LOGIN'
				? "Incorrect username or password."
					: 'An unknown error occurred. Try again in a few minutes.';

			console.error('Login Failed', loginResponse);

			let errorStatus: ErrorStatus = 500;
			if (loginResponse.status >= 400 && loginResponse.status < 600) {
				// @ts-ignore
				errorStatus = loginResponse.status;
			}

			return message(form, errorMessage, {
				status: errorStatus,
			});
		}

		let cookie = loginResponse.headers.getSetCookie()[0];
		let parsedCookie = Cookie.parse(cookie);

		event.cookies.set('user_session', parsedCookie.user_session, {
			path: parsedCookie.Path,
			maxAge: parseInt(parsedCookie['Max-Age']),
			httpOnly: true
		});

		let destination = event.url.searchParams.get('redirect') || '/';

		redirect(303, destination);
	}
};
