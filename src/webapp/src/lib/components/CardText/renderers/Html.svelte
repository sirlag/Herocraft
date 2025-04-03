<script>
	import IvionIcon from '$lib/components/IvionIcon.svelte';

	/** @type {{text: any}} */
	let { text } = $props();
</script>

<IvionIcon icon={text.substring(1, text.length - 1)} />
