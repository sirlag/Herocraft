<script lang="ts">
	import type { PageData } from './$types';
	import CardImage from '$lib/components/CardImage.svelte';
	import ParsedCardText from '$lib/components/CardText/ParsedCardText.svelte';
	import { Separator } from '$lib/components/ui/separator';

	interface Props {
		data: PageData;
	}

	let { data }: Props = $props();

	let card: IvionCard = $derived(data.card);

	// TODO: Handle Relics, Traps, and Arrows

	let getColorValue = (color: String) => {
		switch (color) {
			case 'Red':
				return '212,55,46';
			case 'Green':
				return '115,171,98';
			case 'Blue':
				return '89,176,217';
			case 'Gray':
				return '116,134,136';
			case 'White':
				return '231,230,225';
			case 'Black':
				return '0,0,0';
		}
	};

	let getColorString = (color1: String | null, color2: String | null) => {
		if (color1 && color2) {
			return { c1: getColorValue(color1), c2: getColorValue(color2) };
		}
		if (color1) {
			let cs = getColorValue(color1);
			return { c1: cs, c2: cs };
		}
		return {c1:"", c2: ""}
	};


	let { c1, c2 } = $derived(getColorString(card.colorPip1, card.colorPip2));


	let colorStyle = $derived(`--color-1:${c1};--color-2:${c2}`);

	console.log($inspect(colorStyle))


</script>

<div class="flex w-full p-4 pb-8 justify-center bg-neutral-50">
	<div class="flex flex-row  max-w-5xl flex-wrap lg:flex-nowrap" style={colorStyle}>
		{#if card}
			<div class="">
				<div class="w-96 h-[538px] shadow-lg rounded-lg">
					<CardImage {card} size="large" />
				</div>
			</div>

			<div  class="border border-gray-200 min-w-96 py-4 -ml-8 mt-4 h-[600px] rounded-lg  card-info-shadow">
				<h1 class="pl-16 pr-4">{card.name}</h1>
				{#if (card.archetype && card.archetype !== "")}
					<Separator />
					<p class="pl-16 pr-4">
						{card.archetype} – {card.type}
					</p>
				{/if}
				{#if (card.extraType && card.extraType !== "")}
					<Separator />
					<p class="pl-16 pr-4">
						{card.extraType}
					</p>
				{/if}
				{#if card?.rulesText}
					<Separator />
					<div class="pl-16 pr-4 prose">
						<ParsedCardText source={card?.rulesText} />
					</div>
				{/if}
				<Separator />
				<div class="pl-16 pr-4">
					{#if card.range}
						<div>Range = {card.range}</div>
					{/if}
					{#if card.actionCost}
						<div>Action = {card.actionCost}</div>
					{/if}
					{#if card.powerCost}
						<div>Power = {card.powerCost}</div>
					{/if}
				</div>
				{#if card.flavorText}
					<Separator />
					<p class="pl-16 pr-4"><i>{card.flavorText}</i></p>
				{/if}
			</div>
		{/if}
	</div>
</div>

<style>

		.card-info-shadow {
        box-shadow: 0px 1px 3px 0px rgba(0, 0, 0, 0.1),
        0px 1px 2px -1px rgba(0, 0, 0, 0.1),
        -8px 12px 20px 1px rgba(var(--color-1),.9),
        8px 16px 15px 1px rgba(var(--color-2),.8);
		}

    /*.shadow-black-red {*/

    /*}*/

    /*.shadow-black {*/
    /*    box-shadow: 0px 1px 3px 0px rgba(0, 0, 0, 0.1),*/
    /*    0px 1px 2px -1px rgba(0, 0, 0, 0.1),*/
    /*    -8px 6px 20px 1px rgba(0, 0, 0, .9),*/
    /*    8px 8px 15px 1px rgba(0, 0, 0, .8);*/
    /*}*/

    /*.shadow-blue {*/
    /*    box-shadow: 0px 1px 3px 0px rgba(0, 0, 0, 0.1),*/
    /*    0px 1px 2px -1px rgba(0, 0, 0, 0.1),*/
    /*    -8px 6px 20px 1px rgba(89, 176, 217, .9),*/
    /*    8px 8px 15px 1px rgba(89, 176, 217, .8);*/
    /*}*/

</style>