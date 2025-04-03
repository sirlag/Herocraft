<script>
	text;
	/** @type {{text: any, raw: any, children?: import('svelte').Snippet}} */
	let { text, raw, children } = $props();
	raw;
</script>

{@render children?.()}
