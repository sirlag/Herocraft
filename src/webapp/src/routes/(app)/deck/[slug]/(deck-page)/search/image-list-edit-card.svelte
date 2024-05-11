<script lang="ts">
	import CardImage from '$lib/components/CardImage.svelte'
	import { Button } from '$lib/components/ui/button'
	import { Input } from '$lib/components/ui/input'
	import { MinusIcon, PlusIcon } from 'lucide-svelte';

	export let card;
	export let count: number = 0;
</script>

<div class="m-1 isolate relative">
	<div class="absolute top-0 left-0 right-0 p-2 px-3 z-10 text-xs box-border text-transparent pointer-events-none">
		{card.name}
	</div>
	<div class="absolute top-12 right-4 z-10 flex align-middle space-x-1 box-border text-right">
		{#if count > 0}
			<Button size="sm-icon" variant="secondary" class="p-1"><MinusIcon/></Button>
			<Input class="h-6 w-16 inline text-right mb-8" readonly value="1"/>
			<Button size="sm-icon" variant="secondary" class="p-1"><PlusIcon/></Button>
		{:else}
			<Button size="sm-icon" variant="secondary"><PlusIcon/></Button>
		{/if}
	</div>
	<CardImage {card} />
</div>
