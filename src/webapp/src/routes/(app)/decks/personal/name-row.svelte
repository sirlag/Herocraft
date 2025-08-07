<script lang="ts">

	import FavoriteToggle from './favorite-toggle.svelte'
	import { Badge } from '$lib/components/ui/badge';

	export let name: string;
	export let hash: string
	export let id: string;
	export let favorite: boolean;
	export let visibility: string;

</script>

<div>
	<FavoriteToggle {id} {favorite}/>
	<a href="/deck/{hash}">{name}</a>
	{#if visibility !== 'PUBLIC'}
		<span class="mx-4">
			<Badge variant="secondary">{visibility}</Badge>
		</span>
	{/if}
</div>