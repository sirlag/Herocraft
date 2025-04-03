<script>
	/** @type {{children?: import('svelte').Snippet}} */
	let { children } = $props();
</script>

<li>{@render children?.()}</li>
