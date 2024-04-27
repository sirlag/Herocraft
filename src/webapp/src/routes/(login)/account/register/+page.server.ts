import { superValidate } from 'sveltekit-superforms';
import { formSchema } from './schema.ts';
import { zod } from 'sveltekit-superforms/adapters';
import type { Actions, PageServerLoad } from './$types.js';
import { fail, redirect } from '@sveltejs/kit';
import { PUBLIC_API_BASE_URL } from '$env/static/public';

export const load: PageServerLoad = async () => {
	return {
		form: await superValidate(zod(formSchema))
	}
}

export const actions: Actions = {
	default: async (event) => {
		const form = await superValidate(event, zod(formSchema));
		if (!form.valid) {
			return fail(400, {
				form
			})
		}

		let data = form.data

		let registerResponse = await fetch(new URL(PUBLIC_API_BASE_URL+'/register'), {
			method: 'POST',
			headers: {
				"Content-Type": "application/json",
			},
			body: JSON.stringify({...data})
		})

		if(!registerResponse) {
			console.log("Not Okay")
			return fail(500, {
				form
			})
		}

		redirect(303, "/account/signin")

	}
}