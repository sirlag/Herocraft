<script>
	/** @type {{children?: import('svelte').Snippet}} */
	let { children } = $props();
</script>

<p>{@render children?.()}</p>
