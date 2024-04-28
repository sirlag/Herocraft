import type { Actions } from '../../../../../.svelte-kit/types/src/routes';
import { PUBLIC_API_BASE_URL } from '$env/static/public';
import { superValidate } from 'sveltekit-superforms';
import { zod } from 'sveltekit-superforms/adapters';
import { DeckSchema } from '$lib/components/NewDeck';
import { fail, redirect } from '@sveltejs/kit';

export const actions: Actions = {

	default: async (event) => {

		const form = await superValidate(event, zod(DeckSchema));
		if (!form.valid) {
			return fail(400, {
				form
			})
		}

		let data = form.data

		console.log(data)

		let createResponse = await fetch(PUBLIC_API_BASE_URL + '/deck/new', {
			method: 'POST',
			headers: {
				'Cookie': event.request.headers.get("Cookie")!!,
				'Content-Type': 'application/json',
			},
			body: JSON.stringify({ ...data })
		})

		if (!createResponse.ok) {
			console.log("Unable to create deck")
			return fail(500, {
				form
			})
		}

		let responseData = await createResponse.json()

		redirect(303, `/deck/${responseData.hash}`)
	}
}
