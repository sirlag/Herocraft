<script lang="ts">
	import { HeroImage } from '$lib/components/image/hero-image';
	import ShortHeader from '../../short-header.svelte';
	import type { PageData } from './$types';
	import { DeckSettingsForm } from '$lib/components/deck-settings';
	import { Button } from '$lib/components/ui/button';
	import { enhance } from '$app/forms';
	import { SearchInput } from '$lib/components/ui/search-input/index.js';
	import ImageListCard from '$lib/components/ImageListCard.svelte';
	import ImageListEditCard from './image-list-edit-card.svelte'

	export let data: PageData;
	let displayMode = 'images';

	$: ({ deckList: deck, query } = data);
</script>

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
<!--					<a href="/card/{card.id}">-->
							<ImageListEditCard {card} />
							<!--            <p class="text-blue-700"> {card.name} </p>-->
<!--					</a>-->
				{/each}
			</div>
		{/if}
	</div>
</div>