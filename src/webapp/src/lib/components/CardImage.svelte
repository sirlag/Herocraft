<script lang="ts">
	import { Button } from '$lib/components/ui/button'
	import { Repeat } from 'lucide-svelte';

	interface Props {
		card: IvionCard;
		size?: "small" | "normal" | "large" | "full";
		href?: string | undefined;
	}

	let { card, href = undefined, size = "normal"}: Props = $props();

	let front = $state(true);

	let getUrl = (uris: ImageUris, size: string) => {
		switch (size) {
			case "full":
				return uris.full;
			case "large":
				return uris.large
			case "normal":
				return uris.normal
			case "small":
				return uris.small;
		}
	}

	let fallbackUrl = (uuid: String | null) =>  {
		return `https://static.wixstatic.com/media/b096d7_${uuid?.toString().replaceAll('-', '')}~mv2.png`;
	}

	let getImageData = (card: IvionCard, front: boolean, cardSize: string ) => {
		if (card === undefined) return undefined;
		let hasBack =
			card.secondUUID !== null && card.secondUUID !== undefined && card.secondUUID !== '';

		let uuid = front ? card.ivionUUID : card.secondUUID;

		// console.log(card)

		let src =  (card.imageUris !== null && card.imageUris !== undefined) ? getUrl(card.imageUris, cardSize) : fallbackUrl(uuid)

		return {
			hasBack,
			uuid,
			src
		};
	};

	let imageData = $derived(getImageData(card, front, size));
</script>

<div class="relative rounded-lg overflow-hidden">
	{#if imageData}
		{#if href}
			<a data-sveltekit-preload-data="off" {href}>
				<img src={imageData.src} alt={card.name} />
			</a>
		{:else}
			<img src={imageData.src} alt={card.name} />
		{/if}

		{#if imageData.hasBack}
			<div class="absolute right-4 top-4">
				<Button variant="secondary" size="sm-icon" type="button" onclick={() => (front = !front)}><Repeat /></Button>
			</div>
		{/if}
	{:else}
		<div class="bg-gray-500 flex items-center justify-between w-full h-full">Card Not Found</div>
	{/if}
</div>
