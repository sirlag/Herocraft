<script lang="ts">
	import type { PageData } from './$types'
	import SuperDebug from 'sveltekit-superforms';
	import colors from 'tailwindcss/colors';
	import type { DeckEntry } from '../../../../app';
	import { string } from 'zod';
	import IvionIcon from '$lib/components/IvionIcon.svelte';
	import CardImage from '$lib/components/CardImage.svelte';
	import type { CollatedDeckList } from './+page.ts';
	import CardTable from './card-table.svelte';
	import { SearchInput } from '$lib/components/ui/search-input';

	export let data: PageData

	$: ({collatedCards, deckList, user} = data)

	$: canEdit = deckList.owner === user?.id;
	// console.log(data)

	type CollatedEntries = {
		key: string,
		deckEntries: DeckEntry[]
	}

	const getKeys = (cards: (CollatedDeckList | undefined)) => {
		if (cards !== undefined) {
			let keys =  Object.keys(cards)
			return keys.map(key => {
				return {
					key: key,
					deckEntries: cards[key]!!
				}
			})
		} else {
			return Array.of<CollatedEntries>()
		}
	}

	$: iterableCards = getKeys(collatedCards)
	$: traits = iterableCards.find((it) => it.key === 'Trait')

	const getFirstCard = (cards: DeckEntry[]) => {
		let sorted = cards.toSorted((a, b) => a.card.name.localeCompare(b.card.name))
		let ultimate = (sorted.find((value) => value.card.type === "Ultimate"))
		return ultimate !== undefined ? ultimate?.card : sorted[0]
	}

	const setFirstCard = (card: IvionCard) => {
		firstCard = card
	}
	$: firstCard = getFirstCard(deckList.list)
</script>

<div class="w-full bg-neutral-200 flex flex-row justify-start p-4">
		<div class="flex flex-col w-full mx-auto max-w-7xl">
			<div>
				<!-- TODO: Replace this with link to user page -->
				<a class="text-3xl" href="/">{deckList.ownerName}</a>
			</div>
			<div>
				<span class="text-6xl font-bold">{deckList.name}</span>
			</div>
			<div>
				<ul class="flex flex-row space-x-1">
					{#if (deckList.format)}
						<li class="uppercase border rounded-lg px-2 py-0.5 bg-neutral-50">{deckList.format}</li>
					{/if}
					{#if (deckList.visibility && deckList.visibility !== "PUBLIC")}
						<li class="uppercase border rounded-lg px-2 py-0.5 bg-neutral-50">{deckList.visibility}</li>
					{/if}
				</ul>
			</div>
		</div>
</div>

<div class="flex flex p-4 bg-neutral-50">
	<div class="flex border-b w-full max-w-7xl mx-auto justify-between">
		<div class="flex space-x-3 p-2">
			<a href="/deck/{data.slug}/settings">Settings</a>
			<a>Delete</a>
		</div>
		{#if canEdit}
			<div class="pb-3">
				<SearchInput />
			</div>
		{/if}
	</div>
</div>

<div class="flex-1 flex flex-col w-full bg-neutral-50 p-8">
	<div class="flex flex-col w-full max-w-7xl mx-auto space-y-2">
		{#if iterableCards !== undefined}
			<div class="flex flex-row justify-around">
				{#if traits}
					{#each traits.deckEntries as entry}
						<div class="w-56">
							<CardImage card={entry.card} />
						</div>
					{/each}
				{/if}
			</div>
		{/if}
		<div class="flex flex-row justify-around">

			{#if (iterableCards === undefined || iterableCards.length === 0)}
				<div>
					<p>There is nothing here now. A blank slate, A new hero.</p>
					<p>Add a Specialization, a Class, or a Card to continue.</p>
				</div>

			{:else}
				<div class="block w-60">
					<CardImage card={firstCard} />
				</div>
			{/if}

			{#each iterableCards as category}
				<div class="flex-2">
					<CardTable cards={category.deckEntries} category={category.key} mouseOver={(link) => {
							return () => setFirstCard(link)
						}
					} />
				</div>
			{/each}
		</div>

	</div>

</div>