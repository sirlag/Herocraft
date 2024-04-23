<script lang="ts">
    import type { PageData } from './$types';
    import ImageListCard from '$lib/components/ImageListCard.svelte';
    import { page } from '$app/stores'
    import { goto } from '$app/navigation';
    import { Button } from '$lib/components/ui/button';

    import { ChevronFirst, ChevronLast, ChevronLeft, ChevronRight } from 'lucide-svelte';

    export let data: PageData
    let displayMode = "images";

    $: pageNum = data.page.page
    $: hasPrevious = pageNum > 1;
    $: hasNext = data.page.hasNext;

    let navigateTo = (pageNum: string) => {
        let query = new URLSearchParams($page.url.searchParams.toString());
        query.set('page', pageNum)
        goto(`?${query.toString()}`);
    }
</script>


<div class="flex flex-row space-x-1">
    <Button
      on:click={() => navigateTo((1).toString())}
      disabled={!hasPrevious}
      size="icon"
      variant="secondary">
        <ChevronFirst class="h-4 w-4"/>
    </Button>
    <Button
      on:click={() => navigateTo((pageNum-1).toString())}
      disabled={!hasPrevious}
      variant="secondary">
        <ChevronLeft class="mr-2 h-4 w-4"/>
        Previous
    </Button>
    <Button
      on:click={() => navigateTo((pageNum+1).toString())}
      disabled={!hasNext}
      class="h-10"
      variant="secondary">
        Next
        <ChevronRight class="ml-3 h-4 w-4"/>
    </Button>
    <Button
      on:click={() => navigateTo((data.page.totalPages).toString())}
      disabled={!hasNext}
      size="icon"
      variant="secondary">
        <ChevronLast class="h-4 w-4"/>
    </Button>
</div>

<div>
    {(data.page.pageSize*(data.page.page-1) +1)} - {(data.page.pageSize*(data.page.page-1)) + data.page.itemCount} of {data.page.totalItems} cards
</div>

{#if (displayMode === 'images')}
    <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4">
        {#each data.cards as card}
            <a href="/card/{card.id}">
                <div class="m-1">
                    <ImageListCard {card} />
        <!--            <p class="text-blue-700"> {card.name} </p>-->
                </div>
            </a>

        {/each}
    </div>
{/if}