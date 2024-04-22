<script lang="ts">
	export let card;
	$: hasBack = card.secondUUID !== null && card.secondUUID !== undefined && card.secondUUID !== "";

	$: front = true
	$: uuid = front ? card.ivionUUID : card.secondUUID
	$: src = `https://static.wixstatic.com/media/b096d7_${uuid.toString().replaceAll("-", "")}~mv2.png`;
</script>

<div class="relative rounded-lg overflow-hidden">
	<img {src} alt="{card.name}" />
	{#if (hasBack)}
		<div class="absolute right-4 top-4 bg-blue-700 cursor-pointer" on:click|stopPropagation="{() => front = !front}">
			<p>BACK SIDE</p>
		</div>
	{/if}
</div>

<div>

</div>
