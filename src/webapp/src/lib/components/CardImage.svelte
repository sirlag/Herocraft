<script lang="ts">
	export let card;
	$: src = `https://static.wixstatic.com/media/b096d7_${card.ivionUUID.toString().replaceAll("-", "")}~mv2.png`;
</script>

<img {src} alt="{card.name}" />