<script lang="ts">
	import CardImage from '$lib/components/CardImage.svelte';

	export let card;
	export let href;
</script>

<div class="relative isolate">
	<div class="absolute top-0 left-0 right-0 p-2 px-3 z-10 text-xs box-border text-transparent pointer-events-none">
		{card.name}
	</div>
	<CardImage {card} {href} />
</div>
