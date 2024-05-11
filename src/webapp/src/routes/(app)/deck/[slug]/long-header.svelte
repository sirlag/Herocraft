<script lang="ts">
	import type { Deck } from '../../../../app';
	import { HeroImage } from '$lib/components/image/hero-image/';
	import Time from 'svelte-time';

	export let deckList: Deck;
</script>

<div class="w-full bg-red-50 relative">
	<div class="deckheader-image-wrapper">
		<HeroImage spec="{deckList.primarySpec}" />
	</div>
	<div class="flex w-full deckheader justify-center">
		<div class="flex flex-col w-full mx-auto max-w-7xl p-8 z-10">
			<div class="">
				<!-- TODO: Replace this with link to user page -->
				<a class="text-3xl text-white" href="/">{deckList.ownerName}</a>
			</div>
			<div class="pb-4">
				<span class="text-6xl font-bold text-white">
					{deckList.name}
				</span>
			</div>
			<div class="pb-4">
				<ul class="flex flex-row space-x-1">
					{#if deckList.format}
						<li class="uppercase border rounded-lg px-2 py-0.5 bg-neutral-50">{deckList.format}</li>
					{/if}
					{#if deckList.visibility && deckList.visibility !== 'PUBLIC'}
						<li class="uppercase border rounded-lg px-2 py-0.5 bg-neutral-50">
							{deckList.visibility}
						</li>
					{/if}
				</ul>
			</div>
			<div>
				<span class="text-white">Last Updated <Time timestamp={deckList.lastModified} relative="{true}"/></span>
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
