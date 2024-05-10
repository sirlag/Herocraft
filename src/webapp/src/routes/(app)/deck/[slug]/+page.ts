import type { PageLoad } from './$types';
import type { DeckEntry } from '../../../../app';

export type CollatedDeckList = Partial<Record<PropertyKey, DeckEntry[]>>

export const load : PageLoad = async ({params, parent, fetch}) => {

		let parentData = await parent()
		let deckList = parentData.deckList

		let collatedCards: CollatedDeckList = Object.groupBy(deckList.list, ({ card } ) => {
			return card.archetype === '' || card.type === 'Ultimate' ? `Trait`.valueOf() : card.archetype!!
		})

		return {
			collatedCards,
			slug: params.slug
		}
}