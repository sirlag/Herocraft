<script lang="ts">
	import ParsedCardText from '$lib/components/CardText/ParsedCardText.svelte';
	import { Separator } from '$lib/components/ui/separator';
	import CardStat from '$lib/components/CardStat.svelte';
	import { getFace, hasFaces } from '$lib/utils/card';
	import { buildColorStyle } from '$lib/utils/colors';

	interface Props {
		card: IvionCard;
		compact?: boolean;
		standalone?: boolean;
	}

	let { card, compact = false, standalone = false }: Props = $props();

	// Padding classes - balanced for standalone, asymmetric for overlapped layout
	const paddingX = standalone ? 'px-6' : 'pl-16 pr-4';

	const cardHasFaces = $derived(hasFaces(card));
	const frontFace = $derived(getFace(card, 'front'));
	const backFace = $derived(getFace(card, 'back'));
	let colorStyle = $derived(buildColorStyle(card));
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

<div class="border border-gray-200 py-4 rounded-lg overflow-y-auto relative" style={colorStyle}>
	<!-- Color indicator bars -->
	<div class="absolute top-0 left-0 right-0 h-1 rounded-t-lg" style="background: linear-gradient(to right, rgba(var(--color-1), 0.8), rgba(var(--color-2), 0.8));"></div>
	<div class="absolute bottom-0 left-0 right-0 h-1 rounded-b-lg" style="background: linear-gradient(to right, rgba(var(--color-1), 0.8), rgba(var(--color-2), 0.8));"></div>
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
