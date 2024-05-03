<script lang="ts">
	export let card: IvionCard;

	let front = true

	let getImageData = (card: IvionCard) => {
		if (card === undefined) return undefined
		let hasBack = card.secondUUID !== null && card.secondUUID !== undefined && card.secondUUID !== "";

		let uuid = front ? card.ivionUUID : card.secondUUID
		let src = `https://static.wixstatic.com/media/b096d7_${uuid.toString().replaceAll("-", "")}~mv2.png`;

		return {
			hasBack,
			uuid,
			src
		}
	}

	$: imageData = getImageData(card)

</script>

<div class="relative rounded-lg overflow-hidden">
	{#if imageData}
		<img src={imageData.src} alt="{card.name}" />
		{#if (imageData.hasBack)}
			<div class="absolute right-4 top-4 bg-blue-700 cursor-pointer" on:click|stopPropagation="{() => front = !front}">
				<p>BACK SIDE</p>
			</div>
		{/if}

	{:else}
		<div class="bg-gray-500 flex items-center justify-between w-full h-full" >
			Card Not Found
		</div>
	{/if}

</div>

<div>

</div>
