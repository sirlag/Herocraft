<script lang="ts">
	import type { PageData } from './$types';
	import CardImage from '$lib/components/CardImage.svelte';
	import ParsedCardText from '$lib/components/CardText/ParsedCardText.svelte';
	import { Separator } from '$lib/components/ui/separator';

	export let data: PageData

	$: card = data.card
</script>

<div class="flex w-full p-4 pb-8 justify-center bg-neutral-50">
	<div class="flex flex-row max-w-5xl">
		<CardImage card={card}/>
		<div class="border">
			<h1 class="pl-4">{card.name}</h1>
			<Separator />
			<p class="pl-4">{card.archetype} – {card.type}</p>
			{#if card.rulesText}
				<Separator />
				<ParsedCardText source={card.rulesText}/>
			{/if}
			<Separator />
			<div class="pl-4">
				{#if card.range} <div>Range = {card.range} </div> {/if}
				{#if card.actionCost} <div>Action = {card.actionCost}</div> {/if}
				{#if card.powerCost} <div>Power = {card.powerCost}</div> {/if}
			</div>
			{#if card.flavorText}
				<Separator />
				<p class="pl-4">{card.flavorText}</p>
			{/if}

		</div>
	</div>
</div>
