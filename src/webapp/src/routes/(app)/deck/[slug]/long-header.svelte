<script lang="ts">
	import type { Deck } from '../../../../app';
	import { HeroImage } from '$lib/components/image/hero-image/';
	import Time from 'svelte-time';
	import { Heart, Star, StarOff } from 'lucide-svelte';

	import * as Tooltip from '$lib/components/ui/tooltip';
	import Eye from 'lucide-svelte/icons/eye';
	import { Button } from '$lib/components/ui/button';

	import DeckHeaderItem from './deck-header-item.svelte';

	interface Props {
		deckList: Deck;
		canEdit: boolean;
		user: object;
		likeFunc: (state: boolean) => void;
		favoriteFunc: (state: boolean) => void;
	}


	let { deckList, canEdit, user, likeFunc, favoriteFunc }: Props = $props();

</script>

<Tooltip.Provider delayDuration={350}>
	<div class="w-full bg-red-50 relative">
		<div class="deckheader-image-wrapper">
			<HeroImage spec={deckList.primarySpec} />
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
				<div class="flex text-white gap-10">

					{#if user}
						<DeckHeaderItem
							Icon={Heart}
							hoverStyle="group-hover:stroke-red-500"
							count={deckList.likes}
							label={deckList.liked ? "Click to Unlike" : "Click to Like"}
							action={likeFunc}
							state={deckList.liked || false}
							activeStyle="fill-red-500 stroke-red-500"
						/>
						{:else}
						<DeckHeaderItem Icon={Heart} count={deckList.likes} label="Likes" />
					{/if}

					<DeckHeaderItem Icon={Eye} count={deckList.views} label="Views" />
					{#if canEdit}
						<DeckHeaderItem
							Icon={Star}
							hoverStyle="group-hover:stroke-yellow-300"
							label={deckList.favorite ? "Click to Unfavorite": "Click to Favorite"}
							action={favoriteFunc}
							state={deckList.favorite || false}
							activeStyle="stroke-yellow-300 fill-yellow-300"
						/>
					{/if}

					<Tooltip.Root>
						<Tooltip.Trigger>
							<span class="text-white">Last Updated <Time timestamp={deckList.lastModified} relative={true} /></span>
						</Tooltip.Trigger>
						<Tooltip.Content side="bottom">
							<span>
								Created <Time timestamp={deckList.created} /> (<Time timestamp={deckList.created} relative={true} />)
							</span>
						</Tooltip.Content>
					</Tooltip.Root>
				</div>

			</div>
		</div>
	</div>
</Tooltip.Provider>


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
