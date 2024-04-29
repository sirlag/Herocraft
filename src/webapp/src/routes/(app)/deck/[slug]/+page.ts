import type { PageLoad } from './$types';
import { PUBLIC_API_BASE_URL } from '$env/static/public';
import type { Deck, DeckEntry } from '../../../../app';

export type CollatedDeckList = Partial<Record<PropertyKey, DeckEntry[]>>

export const load : PageLoad = async ({params, fetch}) => {
		let deckListResponse = await fetch(PUBLIC_API_BASE_URL+`/deck/${params.slug}`, {
			method: 'GET'
		})

		let deckList: Deck = await deckListResponse.json()

		let collatedCards: CollatedDeckList = Object.groupBy(deckList.list, ({ card } ) => {
			return card.archetype === '' || card.type === 'Ultimate' ? `Trait`.valueOf() : card.archetype!!
		})

		return {
			deckList,
			collatedCards
		}
}