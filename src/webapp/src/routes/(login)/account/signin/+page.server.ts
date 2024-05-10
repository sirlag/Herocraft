import type { Actions, PageServerLoad } from './$types.js';
import { superValidate } from "sveltekit-superforms";
import { formSchema } from "./schema";
import { zod } from "sveltekit-superforms/adapters";
import { fail, redirect } from '@sveltejs/kit';
import { PUBLIC_API_BASE_URL } from '$env/static/public';
import Cookie from 'cookie'

export const load: PageServerLoad = async () => {
	return {
		form: await superValidate(zod(formSchema)),
	};
};

export const actions: Actions = {
	default: async (event) => {
		const form = await superValidate(event, zod(formSchema));
		if (!form.valid) {
			return fail(400, {
				form
			})
		}

		let data = form.data

		let loginResponse = await fetch(new URL (PUBLIC_API_BASE_URL+'/login'), {
			method: 'POST',
			headers: {
				"Content-Type": "application/json",
			},
			// credentials: 'include',
			body: JSON.stringify({...data})
		})

		if(!loginResponse.ok) {
			console.log("Not Okay")
			return fail(500, {
				form
			})
		}

		// console.log(loginResponse)
		let cookie = loginResponse.headers.getSetCookie()[0]
		let parsedCookie = Cookie.parse(cookie)

		event.cookies.set("user_session", parsedCookie.user_session, {
			path: parsedCookie.Path,
			maxAge: parseInt(parsedCookie['Max-Age']),
			httpOnly: true,
		});

		let destination  = event.url.searchParams.get("redirect") || "/"

		redirect(303, destination)
	}

}