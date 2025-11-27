<script lang="ts">
	import type { PageData } from './$types';
	import CardDisplay from '$lib/components/CardDisplay.svelte';
	import { Separator } from '$lib/components/ui/separator';
	import { PUBLIC_API_BASE_URL } from '$env/static/public';
	import { hasFaces, pickFaceUris } from '$lib/utils/card';
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

	const cardHasFaces = $derived(hasFaces(card));

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

<div class="p-4 pb-8 bg-neutral-50">
	<CardDisplay {card} />
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
