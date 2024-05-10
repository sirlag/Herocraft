<script lang="ts">
	import type { Deck } from '../../../../app';
	import { HeroImage } from '$lib/components/image/hero-image/';
	import { ChevronLeft, ChevronsRight } from 'lucide-svelte';
	import { Button } from '$lib/components/ui/button';

	export let deckList: Deck
	export let page: string
</script>

<div class="w-full bg-red-50 relative">
	<div class="deckheader-image-wrapper">
		<HeroImage spec="Huntsman"/>
	</div>
	<div class="flex w-full deckheader justify-center">
		<div class="flex flex-col w-full mx-auto max-w-7xl p-8 z-10">
			<div class="">
				<!-- TODO: Replace this with link to user page -->
				<a class="text-3xl text-white" href="/">{deckList.ownerName}</a>
			</div>
			<div>
				<span class="text-6xl font-bold text-white">
					{#if (page)}
						{page} <ChevronsRight class="inline-block"/>
					{/if}
					{deckList.name}
				</span>
			</div>
			<div>
				<Button class="mt-6 pl-2" variant="outline" href="."><ChevronLeft /> Return to Deck List</Button>
			</div>
		</div>
	</div>
</div>

<style lang="postcss">
	.deckheader-image-wrapper {
			position: absolute;
			top: 0;
			right: 0;
			bottom: 0;
			left: 50%;
			/*overflow: hidden;*/
      width: 50%;
			height: auto;
			overflow-x: hidden;
			overflow-y: hidden;
			display: flex;
			justify-content: flex-end;
	}

	.deckheader {
			background-image: linear-gradient(rgba(97, 33, 213, 0.95), rgba(97, 33, 213, 0.95));
	}

</style>