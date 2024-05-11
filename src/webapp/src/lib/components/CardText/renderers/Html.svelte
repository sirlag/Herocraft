<script>
	import IvionIcon from '$lib/components/IvionIcon.svelte';

	export let text;
</script>

<IvionIcon icon={text.substring(1, text.length - 1)} />
