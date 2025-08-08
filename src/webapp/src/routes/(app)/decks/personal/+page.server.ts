import type { PageServerLoad, Actions } from './$types';
import { DeckURLs } from '$lib/routes';
import { superValidate } from 'sveltekit-superforms';
import { zod } from 'sveltekit-superforms/adapters';
import { BulkImportSchema } from '$lib/components/bulk-import';
import { fail, redirect } from '@sveltejs/kit';

export const load: PageServerLoad = async ({ request, locals }) => {
	let bulkImportForm = await superValidate(zod(BulkImportSchema));

	console.log(locals);

	if (!locals.user?.isAuthenticated) {
		redirect(302, `/account/signin?redirect=/decks/personal`);
	}

	let decksResponse = await fetch(DeckURLs.personal, {
		method: 'GET',
		headers: {
			cookie: request.headers.get('cookie')!!
		}
	});

	let decks = await decksResponse.json();

	return {
		decks,
		bulkImportForm
	};
};

export const actions: Actions = {
	delete: async ({ request, fetch, params }) => {
		let formData = await request.formData();

		let id = formData.get('id');

		let createResponse = await fetch(DeckURLs.delete(id as string), {
			method: 'DELETE',
			headers: {
				Cookie: request.headers.get('Cookie')!!
			}
		});

		if (!createResponse.ok) {
			console.log('Unable to delete deck');
			return fail(createResponse.status, {
				formData
			});
		}

		let responseData = await createResponse.json();

		redirect(303, '/decks/personal');
	},
	
	toggleVisibility: async ({ request, fetch }) => {
		let formData = await request.formData();

		let id = formData.get('id');
		let visibility = formData.get('visibility');

		let updateResponse = await fetch(DeckURLs.update(id as string), {
			method: 'PATCH',
			headers: {
				Cookie: request.headers.get('Cookie')!!,
				'content-type': 'application/json'
			},
			body: JSON.stringify({ visibility })
		});

		if (!updateResponse.ok) {
			console.log('Unable to update deck visibility');
			return fail(updateResponse.status, {
				formData
			});
		}

		let responseData = await updateResponse.json();

		redirect(303, '/decks/personal');
	}
} satisfies Actions;
