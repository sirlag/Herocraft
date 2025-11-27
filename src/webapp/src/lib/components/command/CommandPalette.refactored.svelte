<script lang="ts">
	import * as Command from '$lib/components/ui/command/index.ts';
	import { PUBLIC_API_BASE_URL } from '$env/static/public';

	type Props = { open?: boolean; onNewDeck?: () => void };
	let { open = $bindable(false), onNewDeck }: Props = $props();

	// State
	type CardSummary = { id: string; name: string };
	let cmdQuery = $state('');
	let cardResults = $state<CardSummary[]>([]);
	let cardSearchLoading = $state(false);
	const cardSearchCache = new Map<string, CardSummary[]>();

	// Derived computed values instead of manual $effect tracking
	let searchQuery = $derived(cmdQuery.trim().toLowerCase());
	let filteredCommands = $derived({
		newDeck: !searchQuery || 'new deck'.includes(searchQuery) || 'create'.includes(searchQuery),
		yourDecks: !searchQuery || 'your decks'.includes(searchQuery),
		allDecks: !searchQuery || 'all decks'.includes(searchQuery),
		likedDecks: !searchQuery || 'liked'.includes(searchQuery),
		preconstructed: !searchQuery || 'preconstructed'.includes(searchQuery)
	});

	let showCardResults = $derived(cmdQuery.trim().length >= 2);
	let hasAnyCommands = $derived(
		filteredCommands.newDeck ||
		filteredCommands.yourDecks ||
		filteredCommands.allDecks ||
		filteredCommands.likedDecks ||
		filteredCommands.preconstructed
	);

	// Card search with debouncing
	$effect(() => {
		const query = cmdQuery.trim();

		if (query.length < 2) {
			cardResults = [];
			cardSearchLoading = false;
			return;
		}

		// Check cache first
		if (cardSearchCache.has(query)) {
			cardResults = cardSearchCache.get(query)!;
			return;
		}

		// Debounced search
		cardSearchLoading = true;
		const timeoutId = setTimeout(async () => {
			try {
				const res = await fetch(
					`${PUBLIC_API_BASE_URL}/cards?q=${encodeURIComponent(query)}`,
					{ headers: { Accept: 'application/json' } }
				);

				if (!res.ok) throw new Error(`Search failed: ${res.status}`);

				const data = await res.json();
				const items = Array.isArray(data) ? data : (data?.items ?? []);

				const results = items
					.map((c: any) => ({
						id: String(c?.id ?? c?.uuid ?? c?.cardId ?? ''),
						name: String(c?.name ?? c?.title ?? '')
					}))
					.filter((c) => c.id && c.name);

				// Deduplicate by id
				const unique = Array.from(
					new Map(results.map((c) => [c.id, c])).values()
				);

				cardSearchCache.set(query, unique);

				// Only update if query hasn't changed
				if (cmdQuery.trim() === query) {
					cardResults = unique;
				}
			} catch (err) {
				console.error('Card search error:', err);
				if (cmdQuery.trim() === query) {
					cardResults = [];
				}
			} finally {
				if (cmdQuery.trim() === query) {
					cardSearchLoading = false;
				}
			}
		}, 350);

		return () => clearTimeout(timeoutId);
	});

	// Reset on close
	$effect(() => {
		if (!open) {
			cmdQuery = '';
			cardResults = [];
			cardSearchLoading = false;
		}
	});

	// Event handlers - clean and simple
	function handleNewDeck() {
		open = false;
		// Use requestAnimationFrame for proper sequencing after dialog closes
		requestAnimationFrame(() => onNewDeck?.());
	}

	function closeDialog() {
		open = false;
	}
</script>

<Command.Dialog bind:open shouldFilter={false}>
	<Command.Input placeholder="Search cards or type a command..." bind:value={cmdQuery} />
	<Command.List>
		<Command.Empty>
			<p class="prose">Nothing found</p>
		</Command.Empty>

		{#if hasAnyCommands}
			<Command.Group heading="Actions">
				{#if filteredCommands.newDeck}
					<Command.Item
						value="action:new-deck"
						on:select={handleNewDeck}
					>
						New Deck
					</Command.Item>
				{/if}
			</Command.Group>

			<Command.Group heading="Links">
				{#if filteredCommands.yourDecks}
					<Command.LinkItem
						href="/decks/personal"
						value="link:your-decks"
						on:select={closeDialog}
					>
						Your Decks
					</Command.LinkItem>
				{/if}
				{#if filteredCommands.allDecks}
					<Command.LinkItem
						href="/decks/public"
						value="link:all-decks"
						on:select={closeDialog}
					>
						All Decks
					</Command.LinkItem>
				{/if}
				{#if filteredCommands.likedDecks}
					<Command.LinkItem
						href="/decks/liked"
						value="link:liked-decks"
						on:select={closeDialog}
					>
						Decks You Liked
					</Command.LinkItem>
				{/if}
				{#if filteredCommands.preconstructed}
					<Command.LinkItem
						href="/decks/public"
						value="link:preconstructed"
						on:select={closeDialog}
					>
						Preconstructed Decks
					</Command.LinkItem>
				{/if}
			</Command.Group>
		{/if}

		{#if showCardResults}
			{#if cardSearchLoading}
				<Command.Loading>Searching cards...</Command.Loading>
			{:else if cardResults.length > 0}
				<Command.Group heading="Cards">
					{#each cardResults as card (card.id)}
						<Command.LinkItem
							href="/card/{card.id}"
							value="card:{card.id}"
							keywords={[card.name]}
							on:select={closeDialog}
						>
							{card.name}
						</Command.LinkItem>
					{/each}
				</Command.Group>
			{:else if cmdQuery.trim().length >= 2}
				<Command.Empty>
					<p class="prose">No cards found</p>
				</Command.Empty>
			{/if}
		{/if}
	</Command.List>
</Command.Dialog>
