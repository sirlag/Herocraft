<script lang="ts">
	import type { PageData } from './$types';
	import type { Deck } from '$app';
	import { Button } from '$lib/components/ui/button';
	import { Badge } from '$lib/components/ui/badge';
	import { HeroImage } from '$lib/components/image/hero-image';
	import Time from 'svelte-time';

	interface Props {
		data: PageData;
	}

	let { data }: Props = $props();
	let decks: Deck[] = $derived(data.decks || []);

	// Format the deck format for display
	function formatDeckFormat(format: string): string {
		return format.charAt(0).toUpperCase() + format.slice(1).toLowerCase();
	}
</script>

<svelte:head>
	<title>Public Decks // Herocraft</title>
</svelte:head>

<div class="flex-1 flex flex-col bg-neutral-50 min-h-screen">
	<div class="container mx-auto px-4 py-8">
		<h1 class="text-3xl font-bold text-gray-900 mb-8">Recent Public Decks</h1>
		
		{#if decks.length === 0}
			<div class="text-center py-12">
				<p class="text-gray-500 text-lg">No public decks found.</p>
			</div>
		{:else}
			<div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-6">
				{#each decks as deck (deck.id)}
					<a 
						href="/deck/{deck.hash}" 
						class="block bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow duration-200 cursor-pointer"
					>
						<!-- Hero Image Background with Overlaid Content -->
						<div class="h-48 bg-gray-200 relative overflow-hidden">
							{#if deck.primarySpec}
								<HeroImage spec={deck.primarySpec} />
							{:else}
								<div class="absolute inset-0 flex items-center justify-center">
									<div class="text-gray-400 text-4xl">🃏</div>
								</div>
							{/if}
							<!-- Overlay for better text readability -->
							<div class="absolute inset-0 bg-black bg-opacity-40"></div>
							
							<!-- Overlaid Content -->
							<div class="absolute inset-0 p-4 flex flex-col justify-between text-white">
								<!-- Top Section: Deck Title and Format -->
								<div>
									<h3 class="font-semibold text-lg mb-2 line-clamp-2 text-white drop-shadow-lg" title={deck.name}>
										{deck.name}
									</h3>
									<div class="mb-2">
										<Badge variant="secondary" class="bg-white/90 text-gray-900 hover:bg-white/80">
											{formatDeckFormat(deck.format)}
										</Badge>
									</div>
								</div>
								
								<!-- Bottom Section: Stats and Info -->
								<div class="space-y-2">
									<!-- Stats Row -->
									<div class="flex items-center space-x-4 text-sm">
										<!-- Likes -->
										<div class="flex items-center space-x-1">
											<svg class="w-4 h-4 text-red-400" fill="currentColor" viewBox="0 0 20 20">
												<path fill-rule="evenodd" d="M3.172 5.172a4 4 0 015.656 0L10 6.343l1.172-1.171a4 4 0 115.656 5.656L10 17.657l-6.828-6.829a4 4 0 010-5.656z" clip-rule="evenodd"></path>
											</svg>
											<span class="drop-shadow-sm">{deck.likes}</span>
										</div>
										
										<!-- Views -->
										<div class="flex items-center space-x-1">
											<svg class="w-4 h-4 text-gray-200" fill="none" stroke="currentColor" viewBox="0 0 24 24">
												<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
												<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
											</svg>
											<span class="drop-shadow-sm">{deck.views}</span>
										</div>
									</div>
									
									<!-- Username and Last Updated Row -->
									<div class="text-xs text-gray-200 space-y-1">
										{#if deck.ownerName}
											<div class="drop-shadow-sm">by {deck.ownerName}</div>
										{/if}
										<div class="drop-shadow-sm">
											<Time timestamp={deck.lastModified} relative={true} />
										</div>
									</div>
								</div>
							</div>
						</div>
					</a>
				{/each}
			</div>
		{/if}
	</div>
</div>

<style>
	.line-clamp-2 {
		display: -webkit-box;
		-webkit-line-clamp: 2;
		-webkit-box-orient: vertical;
		overflow: hidden;
	}
</style>