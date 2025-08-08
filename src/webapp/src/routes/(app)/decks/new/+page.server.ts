import type { Actions } from './$types';
import { DeckURLs } from '$lib/routes';
import { superValidate } from 'sveltekit-superforms';
import { zod } from 'sveltekit-superforms/adapters';
import { DeckSchema } from '$lib/components/NewDeck';
import { fail, redirect } from '@sveltejs/kit';

export const actions: Actions = {
	default: async (event) => {

		const form = await superValidate(event, zod(DeckSchema));
		if (!form.valid) {

			console.error("Invalid Form Sent", form)

			return fail(400, {
				form
			});
		}

		let data = form.data;

		let createResponse = await fetch(DeckURLs.create, {
			method: 'POST',
			headers: {
				Cookie: event.request.headers.get('Cookie')!!,
				'Content-Type': 'application/json'
			},
			body: JSON.stringify({ ...data })
		});

		if (!createResponse.ok) {
			console.log('Unable to create deck');
			return fail(500, {
				form
			});
		}

		let responseData = await createResponse.json();

		redirect(303, `/deck/${responseData.hash}`);
	}
};
