import type { PageServerLoad } from './$types'
import { PUBLIC_API_BASE_URL } from '$env/static/public';
import { superValidate } from 'sveltekit-superforms';
import { zod } from 'sveltekit-superforms/adapters';
import { BulkImportSchema } from '$lib/components/bulk-import';

export const load: PageServerLoad = async ({request}) => {
	let bulkImportForm = await superValidate(zod(BulkImportSchema))

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