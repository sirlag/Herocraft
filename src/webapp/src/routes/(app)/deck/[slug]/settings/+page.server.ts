import type { Actions } from '../../../../../../.svelte-kit/types/src/routes/(app)/decks/personal/$types';
import { PUBLIC_API_BASE_URL } from '$env/static/public';
import { fail, redirect } from '@sveltejs/kit';
import { DeckSettingsForm } from '$lib/components/deck-settings';
import { deckSettingsSchema } from '$lib/components/deck-settings/schema';
import { zod } from 'sveltekit-superforms/adapters';
import { message, superValidate } from 'sveltekit-superforms';

export const actions: Actions = {
	default: async (event) => {
		let { request, fetch, params } = event;

		const form = await superValidate(event, zod(deckSettingsSchema));

		if (!form.valid) {
			return fail(400, {
				form
			});
		}

		let data = form.data;

		let id = data['id'];

		let createResponse = await fetch(`${PUBLIC_API_BASE_URL}/decks/${id}`, {
			method: 'PUT',
			headers: {
				Cookie: request.headers.get('Cookie')!!,
				'content-type': 'application/json'
			},
			body: JSON.stringify({ ...data })
		});

		if (!createResponse.ok) {
			console.log('Unable to modify deck');
			return fail(createResponse.status, {
				...data
			});
		}

		let responseData = await createResponse.json();

		return message (form, "Updated");
	}
} satisfies Actions;
