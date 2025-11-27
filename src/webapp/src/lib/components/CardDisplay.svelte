<script lang="ts">
	import CardImage from '$lib/components/CardImage.svelte';
	import ParsedCardText from '$lib/components/CardText/ParsedCardText.svelte';
	import { Separator } from '$lib/components/ui/separator';
	import CardStat from '$lib/components/CardStat.svelte';
	import { normalizeFace, getFace, hasFaces } from '$lib/utils/card';
	import { buildColorStyle } from '$lib/utils/colors';

	interface Props {
		card: IvionCard;
		compact?: boolean;
	}

	let { card, compact = false }: Props = $props();

	const cardHasFaces = $derived(hasFaces(card));
	const frontFace = $derived(getFace(card, 'front'));
	const backFace = $derived(getFace(card, 'back'));
	let colorStyle = $derived(buildColorStyle(card));

	// Responsive padding - balanced on mobile, asymmetric on desktop
	const paddingX = 'px-6 lg:pl-16 lg:pr-4';
</script>

{#snippet cardInfo(infoSource: IvionCard | IvionCardFaceData | null)}
	{#if infoSource}
		<h1 class="{paddingX} my-2 {compact ? 'text-xl' : ''}">{infoSource.name}</h1>
		{#if (infoSource.archetype && infoSource.archetype !== "")}
			<Separator />
			<p class="{paddingX} my-2">
				{infoSource.archetype} – {infoSource.type}
			</p>
		{/if}
		{#if (infoSource.extraType && infoSource.extraType !== "")}
			<Separator />
			<p class="{paddingX} my-2">
				{infoSource.extraType}
			</p>
		{/if}
		<Separator />
		<div class="{paddingX} my2 prose {compact ? 'prose-sm' : ''}">
			{#key `${card?.ivionUUID ?? card?.id ?? ''}:${infoSource?.name ?? ''}`}
				<ParsedCardText source={infoSource?.rulesText ?? ''} />
			{/key}
		</div>
		{#if (infoSource.actionCost || infoSource.powerCost || infoSource.range)}
			<Separator />
			<div class="{paddingX} my-2">
				<div class="flex items-center gap-3">
					{#if infoSource.actionCost !== undefined && infoSource.actionCost !== null}
						<CardStat type="action" value={infoSource.actionCost} size={compact ? 36 : 44} />
					{/if}
					{#if infoSource.powerCost !== undefined && infoSource.powerCost !== null}
						<CardStat type="power" value={infoSource.powerCost} size={compact ? 36 : 44} />
					{/if}
					{#if infoSource.range !== undefined && infoSource.range !== null}
						<CardStat type="range" value={infoSource.range} size={compact ? 36 : 44} />
					{/if}
				</div>
			</div>
		{/if}
		{#if infoSource.flavorText}
			<Separator />
			<p class="{paddingX} my-2 {compact ? 'text-sm' : ''}"><i>{infoSource.flavorText}</i></p>
		{/if}
	{/if}
{/snippet}

<div class="flex w-full justify-center" style={colorStyle}>
	<div class="flex flex-col lg:flex-row max-w-5xl w-full gap-4 lg:gap-0">
		{#if card}
			<div class="flex-shrink-0 mx-auto lg:mx-0">
				<div class="w-72 h-[403px] sm:w-80 sm:h-[447px] {compact ? 'lg:w-72 lg:h-[403px]' : 'lg:w-96 lg:h-[538px]'} shadow-lg rounded-lg">
					<CardImage {card} size={compact ? 'normal' : 'large'} />
				</div>
			</div>

			<div class="border border-gray-200 py-4 mx-8 md:mx-24 lg:-ml-8 lg:mr-0 mt-6 lg:mt-4 w-auto lg:w-[500px] {compact ? 'lg:w-[500px]' : 'lg:w-[600px]'} rounded-lg card-info-shadow card-info-block overflow-y-auto">
				{#if cardHasFaces}
					{@render cardInfo(frontFace)}
					<Separator />
					{@render cardInfo(backFace)}
				{:else}
					{@render cardInfo(card)}
				{/if}

				{#if card.artist}
					<Separator />
					<p class="{paddingX} my-2 prose {compact ? 'prose-sm' : ''}">
						<i>Illustrated by <a href={`/cards?q=artist%3A\"${card.artist}\"`}>{card.artist}</a></i>
					</p>
				{/if}
			</div>
		{/if}
	</div>
</div>

<style>
	.card-info-shadow {
		box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1),
		0 1px 2px -1px rgba(0, 0, 0, 0.1),
		-8px 12px 20px 1px rgba(var(--color-1), .9),
		8px 16px 15px 1px rgba(var(--color-2), .8);
	}
</style>
