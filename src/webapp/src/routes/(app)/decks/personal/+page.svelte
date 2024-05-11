<script lang="ts">
	import type { PageData } from './$types';

	import DataTable from './data-table.svelte';
	import { BulkImportDialog } from '$lib/components/bulk-import/';

	export let data: PageData;

	$: ({ bulkImportForm, decks } = data);
</script>

<div class="flex-1 flex flex-col bg-neutral-50">
	<div>
		<BulkImportDialog data={bulkImportForm}>
			<a>Import Decks</a>
		</BulkImportDialog>
	</div>
	{#if decks !== undefined}
		<div class="py-10 mx-auto">
			<DataTable {decks} />
		</div>
	{/if}
</div>
