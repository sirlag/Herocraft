<script lang="ts">
	import DataTable from "./data-table.svelte"

	export let data;
</script>

<div class="py-10">
	<DataTable decks="{data.decks}"/>
</div>