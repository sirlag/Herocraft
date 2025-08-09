import type { PageServerLoad } from './$types';
import { DeckURLs } from '$lib/routes';
import { redirect } from '@sveltejs/kit';

export const load: PageServerLoad = async ({ request, url, cookies }) => {
	// Check if user is authenticated by looking for session cookie
	const sessionCookie = cookies.get('user_session');
	if (!sessionCookie) {
		throw redirect(302, '/login');
	}

	const page = url.searchParams.get('page') || '1';
	const size = url.searchParams.get('size') || '20';

	try {
		let decksResponse = await fetch(DeckURLs.liked(parseInt(page), parseInt(size)), {
			method: 'GET',
			headers: {
				cookie: request.headers.get('cookie') || ''
			}
		});

		if (decksResponse.status === 401 || decksResponse.status === 302) {
			throw redirect(302, '/login');
		}

		let paginatedDecks = await decksResponse.json();

		return {
			decks: paginatedDecks.items,
			pagination: {
				totalItems: paginatedDecks.totalItems,
				page: paginatedDecks.page,
				pageSize: paginatedDecks.pageSize,
				totalPages: paginatedDecks.totalPages,
				hasNext: paginatedDecks.hasNext
			}
		};
	} catch (error) {
		if (error instanceof Response && error.status === 302) {
			throw error;
		}
		console.error('Failed to load liked decks:', error);
		return {
			decks: [],
			pagination: null
		};
	}
};