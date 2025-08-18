<script lang="ts">
	import type { PageData } from './$types';
	import type { Deck } from '$app';
	import { DeckCard } from '$lib/components/deck-card';
	import { DeckNavigation } from '$lib/components/deck-navigation';
	import { Button } from '$lib/components/ui/button';
	import { DeckURLs } from '$lib/routes';

	interface Props {
		data: PageData;
	}

	let { data }: Props = $props();
	let allDecks: Deck[] = $state(data.decks || []);
	let pagination = $state(data.pagination);
	let loading = $state(false);

	// Update allDecks when data changes (for initial load)
	$effect(() => {
		if (data.decks && data.pagination?.page === 1) {
			allDecks = [...data.decks];
			pagination = { ...data.pagination };
		}
	});

	async function loadMore() {
		if (!pagination?.hasNext || loading) return;
		
		loading = true;
		try {
			const nextPage = pagination.page + 1;
			const response = await fetch(DeckURLs.liked(nextPage, pagination.pageSize), {
				credentials: 'include'
			});
			const paginatedDecks = await response.json();
			
			// Append new decks to existing ones
			allDecks = [...allDecks, ...paginatedDecks.items];
			pagination = {
				totalItems: paginatedDecks.totalItems,
				page: paginatedDecks.page,
				pageSize: paginatedDecks.pageSize,
				totalPages: paginatedDecks.totalPages,
				hasNext: paginatedDecks.hasNext
			};
		} catch (error) {
			console.error('Failed to load more decks:', error);
		} finally {
			loading = false;
		}
	}
</script>

<svelte:head>
	<title>Liked Decks // Herocraft</title>
</svelte:head>

<div class="flex-1 flex flex-col bg-neutral-50 min-h-screen">
	<div class="container mx-auto px-4 py-8">
		<h1 class="text-3xl font-bold text-gray-900 mb-4">Liked Decks</h1>
		
		<DeckNavigation isLoggedIn={true} />
		
		{#if pagination}
			<div class="mb-6 text-gray-600">
				Showing {allDecks.length} of {pagination.totalItems} liked decks
			</div>
		{/if}
		
		{#if allDecks.length === 0}
			<div class="text-center py-12">
				<p class="text-gray-500 text-lg">You haven't liked any decks yet.</p>
				<p class="text-gray-400 mt-2">Explore public decks and like the ones you enjoy!</p>
			</div>
		{:else}
			<div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-6 mb-8">
				{#each allDecks as deck (deck.id)}
					<DeckCard {deck} />
				{/each}
			</div>
			
			{#if pagination?.hasNext}
				<div class="flex justify-center">
					<Button 
						onclick={loadMore} 
						disabled={loading}
						class="px-8 py-2"
					>
						{loading ? 'Loading...' : 'Load More'}
					</Button>
				</div>
			{:else if pagination && allDecks.length > 0}
				<div class="flex justify-center py-8">
					<p class="text-gray-500 text-lg italic">
						You have reached the end of your liked decks. Discover more to like!
					</p>
				</div>
			{/if}
		{/if}
	</div>
</div>