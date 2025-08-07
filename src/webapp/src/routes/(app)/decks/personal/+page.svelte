<script lang="ts">
	import type { PageData } from './$types';

	import DeckDataTable from './deck-data-table.svelte';
	import { columns } from './deck-columns.ts'
 	import { BulkImportDialog } from '$lib/components/bulk-import/';
	import { Button } from '$lib/components/ui/button';

	interface Props {
		data: PageData;
	}

	let { data }: Props = $props();
	let test = $derived(data.decks);


	let { bulkImportForm, decks } = $derived(data);
</script>

<svelte:head>
	<title>Your Decks // Herocraft</title>
</svelte:head>

<div class="flex-1 flex flex-col bg-neutral-50">
	<div>
		<BulkImportDialog data={bulkImportForm}>
			<Button variant="outline">Import Decks</Button>
		</BulkImportDialog>
	</div>
	{#if decks !== undefined}
		<div class="py-10 mx-auto">
			<DeckDataTable data={test} columns={columns} />
		</div>
	{/if}
</div>
