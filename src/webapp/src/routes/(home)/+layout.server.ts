
import type { LayoutServerLoad } from './$types';
import {superValidate } from 'sveltekit-superforms';
import { zod } from 'sveltekit-superforms/adapters';
import { DeckSchema } from '$lib/components/NewDeck';

export const load: LayoutServerLoad = async ({ locals }) => {
	let deckForm = await superValidate(zod(DeckSchema))

	return {
		deckForm
	}
}