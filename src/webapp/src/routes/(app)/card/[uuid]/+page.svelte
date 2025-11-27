<script lang="ts">
	import type { PageData } from './$types';
	import CardImage from '$lib/components/CardImage.svelte';
	import ParsedCardText from '$lib/components/CardText/ParsedCardText.svelte';
	import { Separator } from '$lib/components/ui/separator';
	import { PUBLIC_API_BASE_URL } from '$env/static/public';
	import CardStat from '$lib/components/CardStat.svelte';
	import { normalizeFace, getFace, hasFaces, pickFaceUris } from '$lib/utils/card';
	import { buildColorStyle } from '$lib/utils/colors';
	import {
		buildCardFilename,
		formatBytes,
		fetchContentLength,
		forceDownload,
		withTimeout
	} from '$lib/utils/download';
	// Icons
	import { Download, FileBracesCorner, Bug } from 'lucide-svelte';

	interface Props {
		data: PageData;
	}

	let { data }: Props = $props();

	let card: IvionCard = $derived(data.card);
	let rulings: any[] = $derived(data.rulings ?? []);

	// TODO: Handle Relics, Traps, and Arrows

	const cardHasFaces = $derived(hasFaces(card));
	const frontFace = $derived(getFace(card, 'front'));
	const backFace = $derived(getFace(card, 'back'));

	let colorStyle = $derived(buildColorStyle(card));

	// Build the API JSON URL using the current route param
	const jsonUrl = $derived(`${PUBLIC_API_BASE_URL}/card/${card.id}`);

	// Image URIs for downloads
	const frontUris = $derived(pickFaceUris(card, 'front'));
	const backUris = $derived(pickFaceUris(card, 'back'));
	const frontPng = $derived(frontUris?.full || card.imageUris?.full || null);
	const backPng = $derived(backUris?.full || null);

	// File size state
	let frontSize: number | null = $state(null);
	let backSize: number | null = $state(null);
	let frontLoading: boolean = $state(false);
	let backLoading: boolean = $state(false);

	// Fetch front image size
	$effect(async () => {
		frontSize = null;
		if (!frontPng) return;
		frontLoading = true;
		const t = withTimeout(6000);
		try {
			frontSize = await fetchContentLength(frontPng, t.signal);
		} finally {
			t.clear();
			frontLoading = false;
		}
	});

	// Fetch back image size
	$effect(async () => {
		backSize = null;
		if (!backPng) return;
		backLoading = true;
		const t = withTimeout(6000);
		try {
			backSize = await fetchContentLength(backPng, t.signal);
		} finally {
			t.clear();
			backLoading = false;
		}
	});

	// Unified button styles
	const btnBase =
		'w-full px-3 py-2 rounded-md inline-flex items-center gap-2 transition-colors focus:outline-none focus:ring-2 focus:ring-gray-300';
	const btnPrimary = 'bg-white text-gray-700 border border-gray-300 hover:bg-gray-50';
	const btnDisabled = 'bg-gray-50 text-gray-400 border border-gray-200 cursor-not-allowed';
</script>

<svelte:head>
	<title>{data.seo?.title || `${card.name} // Herocraft`}</title>
</svelte:head>

{#snippet cardInfo(infoSource: IvionCard | IvionCardFaceData | null)}
	{#if infoSource}
		<h1 class="pl-16 pr-4 my-2">{infoSource.name}</h1>
		{#if (infoSource.archetype && infoSource.archetype !== "")}
			<Separator />
			<p class="pl-16 pr-4 my-2">
				{infoSource.archetype} – {infoSource.type}
			</p>
		{/if}
		{#if (infoSource.extraType && infoSource.extraType !== "")}
			<Separator />
			<p class="pl-16 pr-4 my-2">
				{infoSource.extraType}
			</p>
		{/if}
		<Separator />
		<div class="pl-16 pr-4 my2 prose">
			<!-- Always render ParsedCardText; it has its own safe fallback when source is empty.
					 Key by card identity + face to ensure remount when navigating between cards within same route. -->
			{#key `${card?.ivionUUID ?? card?.id ?? ''}:${infoSource?.name ?? ''}`}
				<ParsedCardText source={infoSource?.rulesText ?? ''} />
			{/key}
		</div>
		{#if (infoSource.actionCost || infoSource.powerCost || infoSource.range)}
			<Separator />
			<div class="pl-16 r-4 my-2">
				<div class="flex items-center gap-3">
					{#if infoSource.actionCost !== undefined && infoSource.actionCost !== null}
						<CardStat type="action" value={infoSource.actionCost} size={44} />
					{/if}
					{#if infoSource.powerCost !== undefined && infoSource.powerCost !== null}
						<CardStat type="power" value={infoSource.powerCost} size={44} />
					{/if}
					{#if infoSource.range !== undefined && infoSource.range !== null}
						<CardStat type="range" value={infoSource.range} size={44} />
					{/if}
				</div>
			</div>
		{/if}
		{#if infoSource.flavorText}
			<Separator />
			<p class="pl-16 pr-4 my-2"><i>{infoSource.flavorText}</i></p>
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

			<div class="border border-gray-200  py-4 -ml-8 mt-4 h-[600px] rounded-lg  card-info-shadow card-info-block">
				{#if cardHasFaces}
					{@render cardInfo(frontFace)}
					<Separator />
					{@render cardInfo(backFace)}
				{:else}
					{@render cardInfo(card)}
				{/if}

				{#if card.artist}
					<Separator />
					<p class="pl-16 pr-4 my-2 prose">
						<i>Illustrated by <a href={`/cards?q=artist%3A\"${card.artist}\"`}>{card.artist}</a></i>
					</p>
				{/if}
			</div>
		{/if}
	</div>
</div>


<div>

	<div class="flex flex-col gap-2 p-4 max-w-5xl mx-auto">
		<!-- Data actions header -->
		<h2 class="text-lg font-semibold text-gray-800">Data</h2>
		<div class="flex flex-col gap-2 items-start w-full md:max-w-[33%]">
			{#if frontPng}
				<button
					onclick={() => forceDownload(frontPng, buildCardFilename(card, cardHasFaces ? 'front' : undefined))}
					class={`${btnBase} ${btnPrimary}`}
					title="Download full-resolution PNG"
				>
					<Download class="w-4 h-4" aria-hidden="true" />
					<span>Download PNG {cardHasFaces ? '(front)' : ''}{frontLoading ? ' · …' : frontSize != null ? ` · ${formatBytes(frontSize)}` : ''}</span>
				</button>
			{:else}
				<button
					class={`${btnBase} ${btnDisabled}`}
					disabled>
					<Download class="w-4 h-4" aria-hidden="true" />
					<span>PNG unavailable</span>
				</button>
			{/if}

			{#if cardHasFaces && backPng}
				<button
					onclick={() => forceDownload(backPng, buildCardFilename(card, 'back'))}
					class={`${btnBase} ${btnPrimary}`}
					title="Download full-resolution PNG (back)"
				>
					<Download class="w-4 h-4" aria-hidden="true" />
					<span>Download PNG (back){backLoading ? ' · …' : backSize != null ? ` · ${formatBytes(backSize)}` : ''}</span>
				</button>
			{/if}

			<a
				href={jsonUrl}
				target="_blank"
				rel="noopener noreferrer"
				class={`${btnBase} ${btnPrimary}`}
				title="Open the API URL to view/copy the card JSON"
			>
				<FileBracesCorner class="w-4 h-4" aria-hidden="true" />
				<span>View card JSON</span>
			</a>

			<a
				href="https://discord.gg/uRXTSBckyu"
				target="_blank"
				rel="noopener noreferrer"
				class={`${btnBase} ${btnPrimary}`}
				title="Open Discord to report an issue"
			>
				<Bug class="w-4 h-4" aria-hidden="true" />
				<span>Report card issue</span>
			</a>
		</div>
	</div>
</div>

{#if rulings && rulings.length > 0}
	<Separator />
	<div class="flex w-full px-4 pt-4 pb-12 justify-center bg-neutral-50">
		<div class="flex flex-col max-w-5xl w-full">
			<div class="mt-2 p-4 border border-gray-200 rounded-lg bg-white">
				<h2 class="text-lg font-semibold mb-2">Rulings</h2>
				<ul class="space-y-4">
					{#each rulings as r}
						<li class="border-b last:border-b-0 pb-3">
							<div class="text-sm text-gray-500 flex items-center gap-2">
								<span
									class="inline-block px-2 py-0.5 rounded-full bg-gray-100 border text-gray-700 capitalize">{r.source}</span>
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
        -8px 12px 20px 1px rgba(var(--color-1), .9),
        8px 16px 15px 1px rgba(var(--color-2), .8);
    }
</style>
