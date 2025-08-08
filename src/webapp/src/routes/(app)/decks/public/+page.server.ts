import type { PageServerLoad } from './$types';
import { DeckURLs } from '$lib/routes';

export const load: PageServerLoad = async ({ request, url, cookies }) => {
	const page = url.searchParams.get('page') || '1';
	const size = url.searchParams.get('size') || '20';
	
	// Check if user is logged in
	const sessionCookie = cookies.get('user_session');
	const isLoggedIn = !!sessionCookie;
	
	let decksResponse = await fetch(DeckURLs.public(parseInt(page), parseInt(size)), {
		method: 'GET',
		headers: {
			cookie: request.headers.get('cookie') || ''
		}
	});

	let paginatedDecks = await decksResponse.json();

	return {
		decks: paginatedDecks.items,
		pagination: {
			totalItems: paginatedDecks.totalItems,
			page: paginatedDecks.page,
			pageSize: paginatedDecks.pageSize,
			totalPages: paginatedDecks.totalPages,
			hasNext: paginatedDecks.hasNext
		},
		isLoggedIn
	};
};