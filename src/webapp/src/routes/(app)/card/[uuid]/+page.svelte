<script lang="ts">
	import type { PageData } from './types/src/routes';
	import CardImage from '$lib/components/CardImage.svelte';
	import CardText from '$lib/components/CardText/CardText.svelte';
	import ParsedCardText from '$lib/components/CardText/ParsedCardText.svelte';
	import { Separator } from '$lib/components/ui/separator';

	export let data: PageData
</script>

<div class="flex flex-row">
	<CardImage card={data.card}/>
	<div class="border">
		<h1 class="pl-4">{data.card.name}</h1>
		<Separator />
		<p class="pl-4">{data.card.archetype} – {data.card.type}</p>
		<Separator />
		<ParsedCardText source={data.card.rulesText}/>
		<Separator />
		<div>
			<div>Range = {data.card.range} </div>
			<div>Action = {data.card.actionCost}</div>
			<div>Power = {data.card.powerCost}</div>
		</div>
		{#if data.card.flavorText}
			<Separator />
			<p>{data.card.flavorText}</p>
		{/if}

	</div>
</div>

<style lang="postcss">
	text {
			padding-left: 4rem;
	}
</style>