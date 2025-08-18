<script lang="ts">
	import * as Command from '$lib/components/ui/command/index.ts';
	import { PUBLIC_API_BASE_URL } from '$env/static/public';
	
	// Expose a bindable open prop so parent can toggle the palette (Svelte 5 runes)
	type Props = { open?: boolean; onNewDeck?: () => void };
	let { open = $bindable(false), onNewDeck }: Props = $props();

	// Internal state for command palette
	type CardSummary = { id: string; name: string };
	let cmdQuery: string = $state('');
	let cardResults: CardSummary[] = $state([]);
	let cardSearchLoading: boolean = $state(false);
	const cardSearchCache: Map<string, CardSummary[]> = new Map();
	let debounceHandle: ReturnType<typeof setTimeout> | null = null;
	let lastIssuedQuery: string = '';

	// Helper for local filtering of static commands/links
	function matchesQuery(text: string): boolean {
		const q = (cmdQuery ?? '').trim().toLowerCase();
		if (!q) return true;
		return (text ?? '').toLowerCase().includes(q);
	}

	// Visibility flags
	let actionsHasAny: boolean = $state(true);
	let linksHasAny: boolean = $state(true);
	let showCardGroup: boolean = $state(false);

	$effect(() => {
		const q = (cmdQuery ?? '').trim();
		actionsHasAny = matchesQuery('new deck');
		linksHasAny =
			matchesQuery('View to Your Decks\n') ||
			matchesQuery('View All Decks') ||
			matchesQuery('View Decks You Liked') ||
			matchesQuery('View Preconstructed Decks');
		showCardGroup = cardSearchLoading || q.length >= 2;
	});

	$effect(() => {
		// Debounced remote card search with min query length and cache
		if (debounceHandle) clearTimeout(debounceHandle);
		if (!cmdQuery || cmdQuery.trim().length < 2) {
			cardResults = [];
			cardSearchLoading = false;
			return;
		}
		const q = cmdQuery.trim();
		debounceHandle = setTimeout(async () => {
			lastIssuedQuery = q;
			// Dedupe strictly by non-empty id, and drop entries without id or name
			const dedupeById = (arr: CardSummary[]) => {
				const seen = new Set<string>();
				const out: CardSummary[] = [];
				for (const c of arr) {
					if (!c) continue;
					const id = (c.id ?? '').trim();
					const name = c.name?.trim();
					if (!id || !name) continue;
					if (seen.has(id)) continue;
					seen.add(id);
					out.push({ id, name });
				}
				return out;
			};
			if (cardSearchCache.has(q)) {
				if (cmdQuery.trim() === q) {
					const cached = cardSearchCache.get(q)!;
					cardResults = dedupeById(cached);
				}
				cardSearchLoading = false;
				return;
			}
			cardSearchLoading = true;
			try {
				const res = await fetch(PUBLIC_API_BASE_URL + `/cards?q=${encodeURIComponent(q)}`, {
					headers: { Accept: 'application/json' }
				});
				if (!res.ok) throw new Error(`Failed to search cards: ${res.status}`);
				const data = await res.json();
				const raw = Array.isArray(data)
					? data
					: Array.isArray(data?.items)
						? data.items
						: [];
				const items: CardSummary[] = raw
					.map((c: any) => {
						const idRaw = c?.id ?? c?.uuid ?? c?.cardId;
						const nameRaw = c?.name ?? c?.title ?? c?.cardName ?? '';
						const id = typeof idRaw === 'string' ? idRaw : idRaw != null ? String(idRaw) : '';
						const name = String(nameRaw);
						return { id, name } as CardSummary;
					})
					.filter((c) => c.id && c.id.trim().length > 0 && c.name && c.name.trim().length > 0);
				const unique = dedupeById(items);
				cardSearchCache.set(q, unique);
				if (cmdQuery.trim() === q && lastIssuedQuery === q) {
					cardResults = unique;
				}
			} catch (err) {
				console.error(err);
				if (cmdQuery.trim() === q) {
					cardResults = [];
				}
			} finally {
				if (cmdQuery.trim() === q) {
					cardSearchLoading = false;
				}
			}
		}, 350);
	});

	$effect(() => {
		// When the command dialog closes, reset results to avoid stale items
		if (!open) {
			cmdQuery = '';
			cardResults = [];
			cardSearchLoading = false;
		}
	});

	function closePalette() {
		open = false;
	}

	function closeThenOpenNewDeck() {
		// Close the palette and release focus before opening a new dialog
		closePalette();
		const active = (document.activeElement as HTMLElement | null);
		active?.blur?.();
		// Wait for dialog close animation (~200ms) before opening the next dialog
		setTimeout(() => onNewDeck?.(), 250);
	}

	function handleListClick(e: MouseEvent) {
		const t = e.target as HTMLElement | null;
		if (t && t.closest('a[href]')) {
			closePalette();
		}
	}
	function handleWindowKeydown(e: KeyboardEvent) {
		if (!open) return;
		if (e.key === 'Enter') {
			const active = document.querySelector('[data-slot="command-item"][aria-selected="true"]') as HTMLElement | null;
			if (active) {
				const text = active.textContent?.toLowerCase() ?? '';
				if (text.includes('new deck')) {
					e.preventDefault();
					closeThenOpenNewDeck();
				}
			}
		}
	}
</script>

<svelte:window on:keydown={handleWindowKeydown} />

<Command.Dialog bind:open shouldFilter={false}>
	<Command.Input placeholder="Search cards or type a command..." bind:value={cmdQuery} />
 <Command.List onclick={handleListClick}>
		<Command.Empty>
			<p class="prose">Nothing was found</p>
		</Command.Empty>
		{#if actionsHasAny}
			<Command.Group heading="Actions">
 			{#if matchesQuery('new deck')}
      <Command.Item
					value="action:new-deck"
					keywords={["new deck", "deck", "create"]}
					on:select={closeThenOpenNewDeck}
				>
					<button type="button" class="w-full text-left" on:click={closeThenOpenNewDeck} on:keydown={(e) => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); closeThenOpenNewDeck(); } }}>
						New Deck
					</button>
				</Command.Item>
				{/if}
			</Command.Group>
		{/if}
		{#if cardSearchLoading}
			<Command.Loading>Searching...</Command.Loading>
		{:else if cmdQuery.trim().length >= 2 && cardResults.length === 0}
			<Command.Empty>
				<p class="prose">No cards found</p>
			</Command.Empty>
		{:else if showCardGroup && cardResults.length > 0}
			<Command.Group heading="Card Results">
				{#each cardResults as c (c.id)}
     <Command.LinkItem href={`/card/${c.id}`} value={`card:${c.id}`} keywords={[c.name]}>
						{c.name}
					</Command.LinkItem>
				{/each}
			</Command.Group>
		{/if}

		{#if linksHasAny}
			<Command.Group heading="Links">
				{#if matchesQuery('View Your Decks\n')}
     <Command.LinkItem href="/decks/personal">
						View to Your Decks
					</Command.LinkItem>
				{/if}
    {#if matchesQuery('View All Decks')}
					<Command.LinkItem href="/decks/public">
						View All Decks
					</Command.LinkItem>
				{/if}
    {#if matchesQuery('View Decks You Liked')}
					<Command.LinkItem href="/decks/liked">
						View Decks You Liked
					</Command.LinkItem>
				{/if}
    {#if matchesQuery('View Preconstructed Decks')}
					<Command.LinkItem href="/decks/public">
						View Preconstructed Decks
					</Command.LinkItem>
				{/if}
			</Command.Group>
		{/if}
	</Command.List>
</Command.Dialog>
