import type { Actions } from './$types';
import { BulkImportSchema } from '$lib/components/bulk-import';
import { zod } from 'sveltekit-superforms/adapters';
import { message, superValidate } from 'sveltekit-superforms';
import { fail, redirect } from '@sveltejs/kit';
import { PUBLIC_API_BASE_URL } from '$env/static/public';
import type { C2Array } from '../../../../app';

export const actions: Actions = {

	default: async (event) => {

		const {fetch} = event;

		const form = await superValidate(event, zod(BulkImportSchema));

		if (!form.valid) {
			return fail(400, {
				form
			})
		}

		let data = form.data

		let array: C2Array = JSON.parse(await form.data.deckBuilderSave.text())
		let deckLists = array.data
		let decks = deckLists.map(it => { return {
			name: it[0][0],
			deck: it[1][0],
			specialization: it[2][0],
			class1: it[3][0],
			class2: it[4][0],
			trait1: it[5][0],
			trait2: it[6][0],
		}})

		let includedDecks = decks.filter((it) => data.decks.includes(it.name))

		let importFormat = includedDecks.map( ({name, trait1, trait2, deck: rawDeck, specialization}) => {
			let deck: C2Array = JSON.parse(rawDeck)
			let cards: any[][][] = deck.data;
			return {
				name,
				list: cards.map((it) => {
					return {
						name: it[2][0],
						uuid: it[18][0]
					}
				}),
				trait1,
				trait2,
				specialization
			}

		})

		let uniqueDecks = importFormat.map((it) => Array.from(new Set(it.list.map((it) => it.name))))

		let uniqueCards = uniqueDecks.map(((it, deckIndex) => {
			return {
				name: importFormat[deckIndex].name,
				list: it.map((card) => {

					let foundCard = importFormat[deckIndex].list.filter((rawCard) => rawCard.name === card)

					return {
						name: foundCard[0].name,
						uuid: foundCard[0].uuid,
						count: foundCard.length
					}
				}),
				traits: [
					importFormat[deckIndex].trait1,
					importFormat[deckIndex].trait2
				],
				specialization: importFormat[deckIndex].specialization,
			}
		}))

		let json = JSON.stringify({
			defaultVisibility: data.defaultVisibility,
			decks: uniqueCards
		})
		console.log(json)

		let createResponse = await fetch(PUBLIC_API_BASE_URL + '/decks/import', {
			method: 'POST',
			headers: {
				'Cookie': event.request.headers.get("Cookie")!!,
				'Content-Type': 'application/json',
			},
			body: json,
		})

		return message(form, "Decks Imported")

	}
}