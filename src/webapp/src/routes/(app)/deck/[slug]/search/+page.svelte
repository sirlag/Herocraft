<script lang="ts">
	import { HeroImage } from '$lib/components/image/hero-image';
	import ShortHeader from '../short-header.svelte';
	import type { PageData } from './$types';
	import { DeckSettingsForm } from '$lib/components/deck-settings';
	import { Button } from '$lib/components/ui/button';
	import { enhance } from '$app/forms';
	import { SearchInput } from '$lib/components/ui/search-input';
	import ImageListCard from '$lib/components/ImageListCard.svelte';
	import ImageListEditCard from './image-list-edit-card.svelte'
	import { PUBLIC_API_BASE_URL } from '$env/static/public';
	import { invalidate, invalidateAll } from '$app/navigation';

	export let data: PageData;
	let displayMode = 'images';

	$: ({ deckList: deck, countObj, query } = data);

	const modify = async (card: IvionCard, change: number) => {

		let currentCount = countObj[card.id];
		let newCount = currentCount ? currentCount + change : change > 0 ? change : undefined;

		if (newCount === undefined) {
			return
		}

		let changeResponse = await fetch(`${PUBLIC_API_BASE_URL}/decks/${deck.hash}/edit`, {
			method: 'POST',
			credentials: 'include',
			headers: {
				'Content-Type': 'application/json',
				'accept': 'application/json',
			},
			body: JSON.stringify({cardId: card.id, count: newCount})
		})

		if (!changeResponse.ok) {

		} else {
			let body = await changeResponse.json();
			countObj[card.id] = body.count > 0 ? body.count : undefined;
			// await invalidate(`/deck/${deck.hash}`)
			await(invalidateAll())
		}

		console.log(countObj[card.id]);
	}
</script>

<svelte:head>
	<title>{deck.name} // Herocraft</title>
</svelte:head>

<ShortHeader page="Search" deckList={deck} />

<div class="bg-neutral-50 h-full flex justify-center">
	<div class="max-w-7xl w-full h-full py-4 px-8">
			<SearchInput value="{query}"/>
	</div>
</div>

<div class="bg-neutral-50">
	<div class="max-w-5xl mx-auto pt-4 pb-8">
		{#if displayMode === 'images'}
			<div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4">
				{#each data.cards as card}
					<ImageListEditCard {card} count={countObj[card.id]} {modify} />
				{/each}
			</div>
		{/if}
	</div>
</div>