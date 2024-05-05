import type { PageServerLoad, Actions } from './$types'
import { PUBLIC_API_BASE_URL } from '$env/static/public';
import { superValidate } from 'sveltekit-superforms';
import { zod } from 'sveltekit-superforms/adapters';
import { BulkImportSchema } from '$lib/components/bulk-import';
import { fail, redirect } from '@sveltejs/kit';

export const load: PageServerLoad = async ({request}) => {
	let bulkImportForm = await superValidate(zod(BulkImportSchema))

	console.log("Called load")

	let decksResponse = await fetch(PUBLIC_API_BASE_URL+`/decks/personal`, {
		method: 'GET',
		headers: {
			"cookie": request.headers.get("cookie")!!
		}
	})

	let decks = await decksResponse.json()
	return {
		decks,
		bulkImportForm
	}
}

export const actions: Actions = {

	delete: async ({ request, fetch, params }) => {

		let formData = await request.formData();

		let id = formData.get("id")

		let createResponse = await fetch(`${PUBLIC_API_BASE_URL}/decks/${id}`, {
			method: 'DELETE',
			headers: {
				'Cookie': request.headers.get("Cookie")!!,
			},
		})

		if (!createResponse.ok) {
			console.log("Unable to delete deck")
			return fail(createResponse.status, {
				formData
			})
		}

		let responseData = await createResponse.json()

		return {
			responseData
		}
	}
} satisfies Actions