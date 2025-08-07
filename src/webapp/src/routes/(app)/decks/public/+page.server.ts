import type { PageServerLoad } from './$types';
import { PUBLIC_API_BASE_URL } from '$env/static/public';

export const load: PageServerLoad = async ({ request }) => {
	let decksResponse = await fetch(PUBLIC_API_BASE_URL + `/decks/public`, {
		method: 'GET',
		headers: {
			cookie: request.headers.get('cookie') || ''
		}
	});

	let decks = await decksResponse.json();

	return {
		decks
	};
};