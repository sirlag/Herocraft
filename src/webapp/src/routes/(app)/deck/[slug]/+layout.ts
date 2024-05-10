import { PUBLIC_API_BASE_URL } from '$env/static/public';
import type { Deck } from '../../../../app';
import type { LayoutLoad } from './$types';


export const load: LayoutLoad = async ({params, fetch}) => {
	let deckListResponse = await fetch(PUBLIC_API_BASE_URL+`/deck/${params.slug}`, {
		method: 'GET'
	})

	let deckList: Deck = await deckListResponse.json()

	return {
		deckList
	}
}

