<script lang="ts">
	import type { PageData } from './$types';
	import type { DeckEntry } from '../../../../app';
	import { string } from 'zod';
	import IvionIcon from '$lib/components/IvionIcon.svelte';
	import CardImage from '$lib/components/CardImage.svelte';
	import type { CollatedDeckList } from './+page.ts';
	import CardTable from './card-table.svelte';
	import LongHeader from './long-header.svelte';
	import { SearchInput } from '$lib/components/ui/search-input';
	import { Plus } from 'lucide-svelte';

	import DeckListDropdown from './deck-list-dropdown.svelte';
	import { PUBLIC_API_BASE_URL } from '$env/static/public';
	import { invalidateAll } from '$app/navigation';

	import { Separator } from '$lib/components/ui/separator';
	import { Button } from '$lib/components/ui/button';

	import { ArchetypeLookup } from './data.ts';

	interface Props {
		data: PageData;
	}

	let { data }: Props = $props();

	let { collatedCards, deckList, user } = $derived(data);

	let canEdit = $derived(deckList.owner === user?.id);
	// console.log(data)

	const modify = async (card: IvionCard, count: number) => {

		console.warn('Modify Called');
		if (card === undefined || count === undefined) {
			return;
		}

		let changeResponse = await fetch(`${PUBLIC_API_BASE_URL}/decks/${deckList.hash}/edit`, {
			method: 'POST',
			credentials: 'include',
			headers: {
				'Content-Type': 'application/json',
				'accept': 'application/json'
			},
			body: JSON.stringify({ cardId: card.id, count: count })
		});

		if (!changeResponse.ok) {
			console.error('Unable to update deck list', changeResponse.statusText);
		} else {
			let body = await changeResponse.json();
			// countObj[card.id] = body.count > 0 ? body.count : undefined;
			// if (body.changeSpec) {
			// 	deck.primarySpec = body.spec;
			// }
			// // await invalidate(`/deck/${deck.hash}`)
			await (invalidateAll());
		}
	};

	type CollatedEntries = {
		key: string;
		deckEntries: DeckEntry[];
		totalCount: number;
	};

	const totalCount = (entries: DeckEntry[]) => {
		let totalCount = 0;
		entries.forEach(entry => {
			totalCount += entry.count;
		});
		return totalCount;
	};

	const getKeys = (cards: CollatedDeckList | undefined) => {
		if (cards !== undefined) {
			let keys = Object.keys(cards);
			return keys.map((key) => {
				return {
					key: key,
					deckEntries: cards[key]!!,
					totalCount: totalCount(cards[key]!!)
				};
			});
		} else {
			return Array.of<CollatedEntries>();
		}
	};

	let iterableCards = $derived(getKeys(collatedCards));
	let traits = $derived(iterableCards.find((it) => it.key === 'Trait'));
	let traitSlots = $derived(traits ? 3 - traits.deckEntries.length : 0);

	const getFirstCard = (cards: DeckEntry[]) => {
		let sorted = cards.toSorted((a, b) => a.card.name.localeCompare(b.card.name));
		let ultimate = sorted.find((value) => value.card.type === 'Ultimate');
		return ultimate !== undefined ? ultimate?.card : sorted[0]?.card;
	};

	const setFirstCard = (card: IvionCard) => {
		firstCard = card;
	};
	let firstCard = $derived(getFirstCard(deckList.list));

	let search = $state('');
</script>

<LongHeader {deckList} />

<div class="flex p-4 bg-neutral-50">
	<div class="flex border-b w-full max-w-7xl mx-auto justify-between">
		{#if canEdit}
			<div class="flex space-x-3 p-2">
				<a href="/deck/{data.slug}/settings">Settings</a>
			</div>
			<div class="pb-3">
				<form method="GET" action="/deck/{data.slug}/search">
					<div class="flex space-x-3 p-2">
						<SearchInput name="q" bind:value={search} />
						<Button size="sm" type="submit">Add a Card</Button>
					</div>

				</form>
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
				{#if (traitSlots > 0)}
					<div class="w-56 rounded-lg bg-neutral-300">
						<a href="/deck/{data.slug}/search?f=Feat">
							<span class="text w-full">ADD A NEW TRAIT</span>
							<Plus class="w-full h-auto p-16" />
						</a>
					</div>
				{/if}
			</div>
		{/if}
		<div class="flex flex-row justify-around">
			{#if iterableCards === undefined || iterableCards.length === 0}
				<div class="prose">
					<p>There is nothing here now. A blank slate, A new hero.</p>
					<p>Add a <a href="/deck/{data.slug}/search?f=feat">Trait</a>, a <a
						href="/deck/{data.slug}/search?q=t:ultimate">Class</a>, or a <a href="/deck/{data.slug}/search">Card</a> to
						continue.</p>
				</div>
			{:else}
				<div class="block w-60">
					<CardImage card={firstCard} />
				</div>
			{/if}

			{#each iterableCards as category}

				<div class="flex-2">
					<span class="ml-4">
						{#if canEdit}
							<a href="/deck/{data.slug}/search?{ArchetypeLookup[category.key]}">
								{category.key} ({category.totalCount})
							</a>
						{:else}
							{category.key} ({category.totalCount})
						{/if}
					</span>
					<Separator />
					<ul>
						{#each category.deckEntries as entry}
							<li
								onmouseover={() => setFirstCard(entry.card)}
								onfocus={() => setFirstCard(entry.card)}
							>
								<div class="flex space-between my-1 px-2">
									<span class="ml-0 mr-4">
										{#if canEdit}
											<input
												class="input-borderless text-end"
												value={entry.count}
												size="2"
												maxlength="2"
												inputmode="numeric"
												type="text"
												pattern={`[0-9]{1,2}`}
											/>
										{:else}
											{entry.count}
										{/if}
									</span>
									<span>{entry.card.name}</span>
									<span class="ml-auto mr-0">
										<DeckListDropdown card={entry.card} count={entry.count} modify={modify} canEdit={canEdit} />
									</span>
								</div>
							</li>
							<Separator />
						{/each}
					</ul>
				</div>
			{/each}
		</div>
	</div>
</div>

<style>
    .input-borderless {
        width: 21px;
        padding: 0;
        border: none;
        border-bottom: 1px solid #00000000;
        border-radius: 0;
        background-color: #00000000;
        cursor: text;
    }
</style>