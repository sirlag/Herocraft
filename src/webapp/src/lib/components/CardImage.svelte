<script lang="ts">
	import { Button } from '$lib/components/ui/button'
	import { Repeat } from 'lucide-svelte';

	interface Props {
		card: IvionCard;
		href?: string | undefined;
	}

	let { card, href = undefined }: Props = $props();

	let front = $state(true);

	let getImageData = (card: IvionCard, front: boolean) => {
		if (card === undefined) return undefined;
		let hasBack =
			card.secondUUID !== null && card.secondUUID !== undefined && card.secondUUID !== '';

		let uuid = front ? card.ivionUUID : card.secondUUID;
		let src = `https://static.wixstatic.com/media/b096d7_${uuid?.toString().replaceAll('-', '')}~mv2.png`;

		return {
			hasBack,
			uuid,
			src
		};
	};

	let imageData = $derived(getImageData(card, front));
</script>

<div class="relative rounded-lg overflow-hidden">
	{#if imageData}
		{#if href}
			<a {href}>
				<img src={imageData.src} alt={card.name} />
			</a>
		{:else}
			<img src={imageData.src} alt={card.name} />
		{/if}

		{#if imageData.hasBack}
			<div class="absolute right-4 top-4">
				<Button variant="secondary" size="sm-icon" type="button" on:click={() => (front = !front)}><Repeat /></Button>
			</div>
		{/if}
	{:else}
		<div class="bg-gray-500 flex items-center justify-between w-full h-full">Card Not Found</div>
	{/if}
</div>
