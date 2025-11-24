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
    let rulings: any[] = $derived(data.rulings ?? []);

	// TODO: Handle Relics, Traps, and Arrows

	const normalizeFace = (face: CardFace | string | undefined | null): 'front' | 'back' | null => {
		if (!face) return null;
		const v = face.toLowerCase();
		return v === 'front' ? 'front' : v === 'back' ? 'back' : null;
	}

	const getFace = (faceKey: 'front' | 'back'): IvionCardFaceData | null => {
		if (!card || !card.faces) return null;
		return card.faces.find(f => normalizeFace(f.face) === faceKey) ?? null;
	}

	const hasFaces = $derived(card.layout === "TRANSFORM");
	const frontFace = $derived(getFace('front'));
	const backFace = $derived(getFace('back'));

	let getSpecialColor = (card: IvionCard) => {
		if (card.format == "Relic" || card.format == "Relic Skill") {
			return "Relic Left"
		}
		else if (card.extraType == "Trap") {
			return "Trap"
		}

		else return "None"
	}

	let getColorValue = (color: String) => {
		switch (color) {
			// Default Cases
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
			// Special Cases
			case 'Relic Left':
				return '254,182,22';
			case 'Relic Right':
				return '248,152,41';
			case 'Trap':
				return '244,152,165'
			case 'Arrow':
				return '76,58,84';
			case 'None':
				return '79,78,73';
		}
	};

	// RELIC LEFT rgb(254, 182, 22)
	// RELIC RIGHT rgb(248, 150, 41)
	// TRAP rgb(244, 152, 165)
	// arrow rgb(76, 58, 84)
	// NONE rgb(79, 78, 73)

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


	let color1 = $derived(card.colorPip1 ? card.colorPip1 : getSpecialColor(card));
	let color2 = $derived(card.colorPip2 ? card.colorPip2 : color1 == "Relic Left" ? "Relic Right" : null)


	let { c1, c2 } = $derived(getColorString(color1, color2));


	let colorStyle = $derived(`--color-1:${c1};--color-2:${c2}`);

</script>

<svelte:head>
	<title>{card.name} // Search // Herocraft</title>
</svelte:head>

{#snippet cardInfo(infoSource: IvionCard | IvionCardFaceData | null)}
	{#if infoSource}
		<h1 class="pl-16 pr-4">{infoSource.name}</h1>
		{#if (infoSource.archetype && infoSource.archetype !== "")}
			<Separator />
			<p class="pl-16 pr-4">
				{infoSource.archetype} – {infoSource.type}
			</p>
		{/if}
		{#if (infoSource.extraType && infoSource.extraType !== "")}
			<Separator />
			<p class="pl-16 pr-4">
				{infoSource.extraType}
			</p>
		{/if}
  <Separator />
        <div class="pl-16 pr-4 prose">
            <!-- Always render ParsedCardText; it has its own safe fallback when source is empty.
                 Key by card identity + face to ensure remount when navigating between cards within same route. -->
            {#key `${card?.uuid ?? card?.id ?? ''}:${infoSource?.name ?? ''}`}
                <ParsedCardText source={infoSource?.rulesText ?? ''} />
            {/key}
        </div>
		<Separator />
		<div class="pl-16 pr-4">
			{#if infoSource.range}
				<div>Range = {infoSource.range}</div>
			{/if}
			{#if infoSource.actionCost}
				<div>Action = {infoSource.actionCost}</div>
			{/if}
			{#if infoSource.powerCost}
				<div>Power = {infoSource.powerCost}</div>
			{/if}
		</div>
		{#if infoSource.flavorText}
			<Separator />
			<p class="pl-16 pr-4"><i>{card.flavorText}</i></p>
		{/if}
	{/if}
{/snippet}

<div class="flex w-full p-4 pb-8 justify-center bg-neutral-50">
	<div class="flex flex-row  max-w-5xl flex-wrap lg:flex-nowrap" style={colorStyle}>
		{#if card}
			<div class="">
				<div class="md:w-96 md:h-[538px] shadow-lg rounded-lg">
					<CardImage {card} size="large" />
				</div>
			</div>

			<div  class="border border-gray-200  py-4 -ml-8 mt-4 h-[600px] rounded-lg  card-info-shadow">
				{#if hasFaces}
					{@render cardInfo(frontFace)}
					<Separator />
					{@render cardInfo(backFace)}
				{:else}
					{@render cardInfo(card)}
				{/if}

				{#if card.artist}
					<Separator />
					<p class="pl-16 pr-4 prose">
						<i>Illustrated by <a href={`/cards?q=artist%3A\"${card.artist}\"`}>{card.artist}</a></i>
					</p>
				{/if}
			</div>
		{/if}
	</div>
</div>

{#if rulings && rulings.length > 0}
    <div class="flex w-full px-4 pb-12 justify-center bg-neutral-50">
        <div class="flex flex-col max-w-5xl w-full">
            <div class="mt-2 p-4 border border-gray-200 rounded-lg bg-white">
                <h2 class="text-lg font-semibold mb-2">Rulings</h2>
                <ul class="space-y-4">
                    {#each rulings as r}
                        <li class="border-b last:border-b-0 pb-3">
                            <div class="text-sm text-gray-500 flex items-center gap-2">
                                <span class="inline-block px-2 py-0.5 rounded-full bg-gray-100 border text-gray-700 capitalize">{r.source}</span>
                                {#if r.sourceUrl}
                                    <a class="text-blue-600 hover:underline" href={r.sourceUrl} target="_blank" rel="noopener noreferrer">source</a>
                                {/if}
                                {#if r.publishedAt}
                                    <span>{new Date(r.publishedAt).toLocaleDateString()}</span>
                                {/if}
                                <span class="ml-auto text-xs uppercase tracking-wide text-gray-400">{r.style}</span>
                            </div>

                            {#if r.style === 'RULING'}
                                {#if r.comment}
                                    <p class="mt-2 whitespace-pre-wrap">{r.comment}</p>
                                {/if}
                            {:else}
                                <div class="mt-2 space-y-1">
                                    {#if r.question}
                                        <div>
                                            <span class="font-medium">Q:</span>
                                            <span class="whitespace-pre-wrap"> {r.question}</span>
                                        </div>
                                    {/if}
                                    {#if r.answer}
                                        <div>
                                            <span class="font-medium">A:</span>
                                            <span class="whitespace-pre-wrap"> {r.answer}</span>
                                        </div>
                                    {/if}
                                </div>
                            {/if}
                        </li>
                    {/each}
                </ul>
            </div>
        </div>
    </div>
{/if}

<style>
		.card-info-shadow {
        box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1),
        0 1px 2px -1px rgba(0, 0, 0, 0.1),
        -8px 12px 20px 1px rgba(var(--color-1),.9),
        8px 16px 15px 1px rgba(var(--color-2),.8);
		}
</style>