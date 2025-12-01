import { superValidate } from 'sveltekit-superforms';
import { zod } from 'sveltekit-superforms/adapters';
import { DeckSchema } from '$lib/components/NewDeck';
import type { LayoutServerLoad } from './$types';

export const load: LayoutServerLoad = async () => {
	let deckForm = await superValidate(zod(DeckSchema));
	return {
		deckForm
	};
};
