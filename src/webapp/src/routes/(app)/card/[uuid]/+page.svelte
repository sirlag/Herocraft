<script lang="ts">
	import type { PageData } from './$types';
	import CardImage from '$lib/components/CardImage.svelte';
	import ParsedCardText from '$lib/components/CardText/ParsedCardText.svelte';
	import { Separator } from '$lib/components/ui/separator';
	import { PUBLIC_API_BASE_URL } from '$env/static/public';
	import CardStat from '$lib/components/CardStat.svelte';
	// Icons
	import { Download, FileJson2, Bug } from 'lucide-svelte';

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
	};

	const getFace = (faceKey: 'front' | 'back'): IvionCardFaceData | null => {
		if (!card || !card.faces) return null;
		return card.faces.find(f => normalizeFace(f.face) === faceKey) ?? null;
	};

	const hasFaces = $derived(card.layout === 'TRANSFORM');
	const frontFace = $derived(getFace('front'));
	const backFace = $derived(getFace('back'));

	let getSpecialColor = (card: IvionCard) => {
		if (card.format == 'Relic' || card.format == 'Relic Skill') {
			return 'Relic Left';
		} else if (card.extraType == 'Trap') {
			return 'Trap';
		} else return 'None';
	};

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
				return '244,152,165';
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
		return { c1: '', c2: '' };
	};


	let color1 = $derived(card.colorPip1 ? card.colorPip1 : getSpecialColor(card));
	let color2 = $derived(card.colorPip2 ? card.colorPip2 : color1 == 'Relic Left' ? 'Relic Right' : null);


	let { c1, c2 } = $derived(getColorString(color1, color2));

	let colorStyle = $derived(`--color-1:${c1};--color-2:${c2}`);

	// Footer helpers (moved from inline script)
	const sanitize = (s: string): string => s.replace(/[^a-z0-9\-_.()\[\]\s]/gi, '').replace(/\s+/g, ' ').trim();
	const filename = (c: IvionCard, face?: 'front' | 'back') => {
		const base = sanitize(c.name || 'card');
		const facePart = face ? `-${face}` : '';
		const idPart = c.id ? `-${c.id}` : '';
		return `${base}${facePart}${idPart}.png`;
	};

	const getFaceUris = (c: IvionCard, face: 'front' | 'back') => {
		if (!c) return null;
		if (c.layout === 'TRANSFORM' && Array.isArray(c.faces)) {
			const f = c.faces.find((f) => (typeof f.face === 'string' ? f.face.toString().toLowerCase() : '') === face);
			return f?.imageUris ?? null;
		}
		return null;
	};

	const frontUris = $derived(getFaceUris(card, 'front'));
	const backUris = $derived(getFaceUris(card, 'back'));
	const frontPng = $derived(frontUris?.full || card.imageUris?.full || null);
	const backPng = $derived(backUris?.full || null);

	// Build the API JSON URL using the current route param
	const jsonUrl = $derived(`${PUBLIC_API_BASE_URL}/card/${card.id}`);

	// --- File size helpers for downloads ---
	const formatBytes = (bytes: number): string => {
		if (!Number.isFinite(bytes) || bytes < 0) return '';
		const units = ['B', 'KB', 'MB', 'GB', 'TB'];
		let i = 0;
		let v = bytes;
		while (v >= 1024 && i < units.length - 1) {
			v = v / 1024;
			i++;
		}
		// Keep one decimal for KB and above, no decimals for bytes
		const fixed = i === 0 ? Math.round(v).toString() : v.toFixed(v >= 100 ? 0 : 1);
		return `${fixed} ${units[i]}`;
	};

	const fetchContentLength = async (url: string, signal?: AbortSignal): Promise<number | null> => {
		try {
			// Try a HEAD request first
			const head = await fetch(url, { method: 'HEAD', mode: 'cors', redirect: 'follow', signal });
			const cl = head.headers.get('content-length');
			if (cl) {
				const n = Number(cl);
				if (Number.isFinite(n)) return n;
			}
		} catch (_) {
			// ignore and try range request fallback
		}
		try {
			// Fallback: request a single byte using Range and parse Content-Range total size
			const r = await fetch(url, {
				method: 'GET',
				headers: { Range: 'bytes=0-0' },
				mode: 'cors',
				redirect: 'follow',
				signal
			});
			const cr = r.headers.get('content-range');
			// Format: bytes 0-0/12345
			if (cr && /\/(\d+)\s*$/.test(cr)) {
				const m = cr.match(/\/(\d+)\s*$/);
				const total = m && m[1] ? Number(m[1]) : NaN;
				if (Number.isFinite(total)) return total;
			}
			const cl2 = r.headers.get('content-length');
			if (cl2) {
				const n = Number(cl2);
				if (Number.isFinite(n)) return n;
			}
		} catch (_) {
			// give up
		}
		return null;
	};

	let frontSize: number | null = $state(null);
	let backSize: number | null = $state(null);
	let frontLoading: boolean = $state(false);
	let backLoading: boolean = $state(false);

	const withTimeout = (ms: number) => {
		const ctrl = new AbortController();
		const id = setTimeout(() => ctrl.abort('timeout'), ms);
		return { signal: ctrl.signal, clear: () => clearTimeout(id) };
	};

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

	// Force a download even for cross-origin URLs by fetching as Blob
	const forceDownload = async (url: string, fileName: string) => {
		try {
			const res = await fetch(url, { mode: 'cors' });
			if (!res.ok) throw new Error(`Download failed: ${res.status}`);
			const blob = await res.blob();
			const objectUrl = URL.createObjectURL(blob);
			const a = document.createElement('a');
			a.href = objectUrl;
			a.download = fileName;
			document.body.appendChild(a);
			a.click();
			a.remove();
			URL.revokeObjectURL(objectUrl);
		} catch (e) {
			// Fallback: open in a new tab if blob download fails
			try {
				window.open(url, '_blank', 'noopener');
			} catch {
			}
		}
	};

	// --- Unified button styles (same width and color scheme) ---
	const btnBase =
		'w-full px-3 py-2 rounded-md inline-flex items-center gap-2 transition-colors focus:outline-none focus:ring-2 focus:ring-gray-300';
	// Subtler, neutral/outline style
	const btnPrimary = 'bg-white text-gray-700 border border-gray-300 hover:bg-gray-50';
	const btnDisabled = 'bg-gray-50 text-gray-400 border border-gray-200 cursor-not-allowed';
</script>

<svelte:head>
	<title>{card.name} // Herocraft</title>
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
			{#key `${card?.uuid ?? card?.id ?? ''}:${infoSource?.name ?? ''}`}
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
				{#if hasFaces}
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
					on:click={() => forceDownload(frontPng, filename(card, hasFaces ? 'front' : undefined))}
					class={`${btnBase} ${btnPrimary}`}
					title="Download full-resolution PNG"
				>
					<Download class="w-4 h-4" aria-hidden="true" />
					<span>Download PNG {hasFaces ? '(front)' : ''}{frontLoading ? ' · …' : frontSize != null ? ` · ${formatBytes(frontSize)}` : ''}</span>
				</button>
			{:else}
				<button
					class={`${btnBase} ${btnDisabled}`}
					disabled>
					<Download class="w-4 h-4" aria-hidden="true" />
					<span>PNG unavailable</span>
				</button>
			{/if}

			{#if hasFaces && backPng}
				<button
					on:click={() => forceDownload(backPng, filename(card, 'back'))}
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
				<FileJson2 class="w-4 h-4" aria-hidden="true" />
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