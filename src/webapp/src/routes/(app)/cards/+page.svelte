<script lang="ts">
	import type { PageData } from './$types';
	import ImageListCard from '$lib/components/ImageListCard.svelte';
	import { page } from '$app/state';
	import { goto } from '$app/navigation';
	import { Button } from '$lib/components/ui/button';

	import { ChevronFirst, ChevronLast, ChevronLeft, ChevronRight } from 'lucide-svelte';

	interface Props {
		data: PageData;
	}

	let { data }: Props = $props();
	let displayMode = 'images';

	let pageNum = $derived(data.page.page);
	let hasPrevious = $derived(pageNum > 1);
	let hasNext = $derived(data.page.hasNext);

	let navigateTo = (pageNum: string) => {
		let query = new URLSearchParams(page.url.searchParams.toString());
		query.set('page', pageNum);
		goto(`?${query.toString()}`);
	};
</script>

<svelte:head>
	<title>Search // Herocraft</title>
</svelte:head>

<div class="w-full py-2 bg-neutral-50">
	<div class="flex flex-row max-w-5xl mx-auto my-0">
		<Button
			onclick={() => navigateTo((1).toString())}
			disabled={!hasPrevious}
			size="icon"
			variant="secondary"
		>
			<ChevronFirst class="h-4 w-4" />
		</Button>
		<Button
			onclick={() => navigateTo((pageNum - 1).toString())}
			disabled={!hasPrevious}
			variant="secondary"
		>
			<ChevronLeft class="mr-2 h-4 w-4" />
			Previous
		</Button>
		<Button
			onclick={() => navigateTo((pageNum + 1).toString())}
			disabled={!hasNext}
			class="h-10"
			variant="secondary"
		>
			Next
			<ChevronRight class="ml-3 h-4 w-4" />
		</Button>
		<Button
			onclick={() => navigateTo(data.page.totalPages.toString())}
			disabled={!hasNext}
			size="icon"
			variant="secondary"
		>
			<ChevronLast class="h-4 w-4" />
		</Button>
	</div>
</div>

<div class="w-full border-t border-b py-2 bg-neutral-100">
	<div class="flex flex-row justify-start max-w-5xl mx-auto">
		<div class="w-max">
			{data.page.pageSize * (data.page.page - 1) + 1} - {data.page.pageSize * (data.page.page - 1) +
				data.page.itemCount} of {data.page.totalItems} cards
		</div>
	</div>
</div>

<div class="bg-neutral-50">
	<div class="max-w-5xl mx-auto pt-4 pb-8">
		{#if displayMode === 'images'}
			<div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 p-1">
				{#each data.cards as card}
						<div class="m-1">
							<ImageListCard {card} href="/card/{card.id}"/>
						</div>
				{/each}
			</div>
		{/if}
	</div>
</div>

<!--</div>-->
