<script lang="ts">
    import type { PageData } from './$types';
    import ImageListCard from '$lib/components/ImageListCard.svelte';

    export let data: PageData
    let displayMode = "images";
</script>

<div>
    <select bind:value={displayMode}>
        <option value="images">Images</option>
        <option value="list">List</option>
        <option value="textonly">Text Only</option>
        <option value="full">Full</option>
    </select>

    <a href="/cards?page=1">left</a>
    <a href="/cards?page=2">right</a>
</div>

<h1>Herocrafter</h1>
{#if (displayMode === 'images')}
    <div class="grid grid-cols-4">
        {#each data.items as card}
            <div class="m-1 rounded-lg overflow-hidden">
                <ImageListCard {card} />
    <!--            <p class="text-blue-700"> {card.name} </p>-->
            </div>
        {/each}
    </div>
{/if}