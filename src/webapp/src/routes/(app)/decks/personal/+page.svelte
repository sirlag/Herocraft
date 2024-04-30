<script lang="ts">
	import type { PageData } from './$types'

	import DataTable from "./data-table.svelte"
	import { Button } from '$lib/components/ui/button';
	import { BulkImportDialog } from '$lib/components/bulk-import/';

	export let data;
</script>

<div class="flex-1 flex flex-col bg-neutral-50">
	<div>
		<BulkImportDialog data="{data.bulkImportForm}">
			<a href="">Import Decks</a>
		</BulkImportDialog>
	</div>
	<div class="py-10 mx-auto">
		<DataTable decks="{data.decks}"/>
	</div>
</div>
