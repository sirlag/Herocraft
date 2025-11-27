<script lang="ts">
	import type { PageData } from './$types';
	import ImageListCard from '$lib/components/ImageListCard.svelte';
	import CardDisplay from '$lib/components/CardDisplay.svelte';
	import CardInfo from '$lib/components/CardInfo.svelte';
	import { Separator } from '$lib/components/ui/separator';
	import { page } from '$app/state';
	import { goto } from '$app/navigation';
	import { Button } from '$lib/components/ui/button';
	import { Input } from '$lib/components/ui/input';
	import {
		ChevronFirst,
		ChevronLast,
		ChevronLeft,
		ChevronRight,
		Search,
		X,
		Grid3x3,
		AlignLeft,
		Maximize2
	} from 'lucide-svelte';

	interface Props {
		data: PageData;
	}

	let { data }: Props = $props();

	// Search state
	let searchQuery = $state(page.url.searchParams.get('q') || '');
	let searchInput = $state(searchQuery);

	// Display mode
	let displayMode = $state<'grid' | 'text' | 'full'>('grid');

	// Sort state
	let sortBy = $state(page.url.searchParams.get('sort') || 'name');

	// Pagination derived state
	let pageNum = $derived(data.page.page);
	let hasPrevious = $derived(pageNum > 1);
	let hasNext = $derived(data.page.hasNext);
	let startItem = $derived(data.page.pageSize * (pageNum - 1) + 1);
	let endItem = $derived(data.page.pageSize * (pageNum - 1) + data.page.itemCount);

	function navigateTo(newPage: number) {
		const query = new URLSearchParams(page.url.searchParams.toString());
		query.set('page', newPage.toString());
		goto(`?${query.toString()}`);
	}

	function handleSortChange(newSort: string) {
		const query = new URLSearchParams(page.url.searchParams.toString());
		query.set('sort', newSort);
		query.set('page', '1'); // Reset to page 1 when changing sort
		goto(`?${query.toString()}`);
	}

	function handleSearch(e: Event) {
		e.preventDefault();
		const query = new URLSearchParams();
		if (searchInput.trim()) {
			query.set('q', searchInput.trim());
		}
		query.set('page', '1');
		goto(`?${query.toString()}`);
	}

	function clearSearch() {
		searchInput = '';
		goto('/cards?page=1');
	}
</script>

<svelte:head>
	<title>{searchQuery ? `Search: ${searchQuery}` : 'All Cards'} // Herocraft</title>
</svelte:head>

<div class="min-h-screen bg-gradient-to-b from-neutral-50 to-neutral-100">
	<!-- Search Header -->
	<div class="border-b bg-white/80 backdrop-blur-sm sticky top-0 z-10 shadow-sm">
		<div class="container max-w-6xl mx-auto px-4 py-4">
			<form onsubmit={handleSearch} class="flex flex-col gap-3 sm:flex-row sm:items-center">
				<div class="flex-1 relative">
					<Search class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground pointer-events-none" />
					<Input
						type="text"
						placeholder="Search cards (e.g., a:Artificer, f:Action, cost>=3)..."
						bind:value={searchInput}
						class="pl-10 pr-10 h-11"
					/>
					{#if searchInput}
						<button
							type="button"
							onclick={clearSearch}
							class="absolute right-3 top-1/2 -translate-y-1/2 h-5 w-5 rounded-full hover:bg-muted flex items-center justify-center"
							aria-label="Clear search"
						>
							<X class="h-3 w-3" />
						</button>
					{/if}
				</div>
				<Button type="submit" class="sm:w-auto w-full">
					<Search class="h-4 w-4 mr-2" />
					Search
				</Button>
			</form>

			{#if searchQuery}
				<div class="mt-3 flex items-center gap-2 text-sm text-muted-foreground">
					<span>Searching for:</span>
					<code class="bg-muted px-2 py-1 rounded text-xs font-mono">{searchQuery}</code>
					<button
						onclick={clearSearch}
						class="text-xs underline hover:text-foreground"
					>
						Clear
					</button>
				</div>
			{/if}
		</div>
	</div>

	<!-- Combined Controls Bar -->
	<div class="border-b bg-white/80 backdrop-blur-sm sticky top-[88px] z-10 shadow-sm">
		<div class="container max-w-6xl mx-auto px-4 py-3">
			<div class="flex flex-col lg:flex-row lg:items-center gap-4">
				<!-- Results count & Sort (left) -->
				<div class="flex flex-col sm:flex-row sm:items-center gap-3 flex-shrink-0">
					<div class="text-sm font-medium text-center sm:text-left">
						{#if data.page.totalItems === 0}
							<span class="text-muted-foreground">No cards found</span>
						{:else}
							<span class="text-foreground">
								{startItem.toLocaleString()}–{endItem.toLocaleString()}
							</span>
							<span class="text-muted-foreground">
								of {data.page.totalItems.toLocaleString()} cards
							</span>
						{/if}
					</div>

					<!-- Sort dropdown -->
					<div class="flex items-center gap-2 justify-center sm:justify-start">
						<span class="text-sm text-muted-foreground">Sort:</span>
						<select
							bind:value={sortBy}
							onchange={(e) => handleSortChange(e.currentTarget.value)}
							class="px-3 py-1.5 text-sm border rounded-md bg-background hover:bg-muted focus:outline-none focus:ring-2 focus:ring-ring"
						>
							<option value="name">Name</option>
							<option value="action">Action</option>
							<option value="power">Power</option>
							<option value="color">Color</option>
						</select>
					</div>
				</div>

				<!-- Pagination (center) -->
				{#if data.page.totalPages > 1}
					<div class="flex items-center justify-center gap-2">
						<!-- First page -->
						<Button
							onclick={() => navigateTo(1)}
							disabled={!hasPrevious}
							size="icon"
							variant="outline"
							class="hidden sm:inline-flex"
						>
							<ChevronFirst class="h-4 w-4" />
						</Button>

						<!-- Previous -->
						<Button
							onclick={() => navigateTo(pageNum - 1)}
							disabled={!hasPrevious}
							variant="outline"
							class="gap-1"
						>
							<ChevronLeft class="h-4 w-4" />
							<span class="hidden sm:inline">Previous</span>
						</Button>

						<!-- Page indicator -->
						<div class="px-4 py-2 text-sm font-medium">
							Page <span class="font-bold">{pageNum}</span> of <span class="font-bold">{data.page.totalPages}</span>
						</div>

						<!-- Next -->
						<Button
							onclick={() => navigateTo(pageNum + 1)}
							disabled={!hasNext}
							variant="outline"
							class="gap-1"
						>
							<span class="hidden sm:inline">Next</span>
							<ChevronRight class="h-4 w-4" />
						</Button>

						<!-- Last page -->
						<Button
							onclick={() => navigateTo(data.page.totalPages)}
							disabled={!hasNext}
							size="icon"
							variant="outline"
							class="hidden sm:inline-flex"
						>
							<ChevronLast class="h-4 w-4" />
						</Button>
					</div>
				{/if}

				<!-- Display mode toggle (right) -->
				<div class="flex items-center justify-center lg:justify-end gap-2 flex-shrink-0">
					<span class="text-sm text-muted-foreground hidden sm:inline">View:</span>
					<div class="inline-flex rounded-md shadow-sm" role="group">
						<button
							type="button"
							onclick={() => displayMode = 'grid'}
							class="px-3 py-1.5 text-sm font-medium border rounded-l-md {displayMode === 'grid' ? 'bg-primary text-primary-foreground' : 'bg-background hover:bg-muted'}"
							aria-label="Grid view"
						>
							<Grid3x3 class="h-4 w-4" />
						</button>
						<button
							type="button"
							onclick={() => displayMode = 'text'}
							class="px-3 py-1.5 text-sm font-medium border border-l-0 {displayMode === 'text' ? 'bg-primary text-primary-foreground' : 'bg-background hover:bg-muted'}"
							aria-label="Text view"
						>
							<AlignLeft class="h-4 w-4" />
						</button>
						<button
							type="button"
							onclick={() => displayMode = 'full'}
							class="px-3 py-1.5 text-sm font-medium border border-l-0 rounded-r-md {displayMode === 'full' ? 'bg-primary text-primary-foreground' : 'bg-background hover:bg-muted'}"
							aria-label="Full view"
						>
							<Maximize2 class="h-4 w-4" />
						</button>
					</div>
				</div>
			</div>
		</div>
	</div>

	<!-- Cards Grid/List -->
	<div class="container max-w-6xl mx-auto px-4 py-6">
		{#if data.cards.length === 0}
			<!-- Empty state -->
			<div class="max-w-md mx-auto border rounded-lg bg-card p-6 text-center">
				<div class="mx-auto w-12 h-12 rounded-full bg-muted flex items-center justify-center mb-4">
					<Search class="h-6 w-6 text-muted-foreground" />
				</div>
				<h3 class="text-lg font-semibold mb-2">No cards found</h3>
				<p class="text-sm text-muted-foreground mb-4">
					{#if searchQuery}
						Try adjusting your search query or <button onclick={clearSearch} class="underline">view all cards</button>.
					{:else}
						There are no cards in the database yet.
					{/if}
				</p>
			</div>
		{:else if displayMode === 'grid'}
			<!-- Grid view -->
			<div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-3">
				{#each data.cards as card (card.id)}
					<div class="transition-transform hover:scale-105">
						<ImageListCard {card} href="/card/{card.id}"/>
					</div>
				{/each}
			</div>
		{:else if displayMode === 'text'}
			<!-- Text-only view -->
			<div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
				{#each data.cards as card (card.id)}
					<CardInfo {card} compact={true} standalone={true} />
				{/each}
			</div>
		{:else if displayMode === 'full'}
			<!-- Full view (embedded card pages) -->
			<div class="space-y-12">
				{#each data.cards as card, idx (card.id)}
					{#if idx > 0}
						<Separator class="my-12" />
					{/if}
					<CardDisplay {card} compact={true} />
				{/each}
			</div>
		{/if}
	</div>
</div>
