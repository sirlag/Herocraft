<script>
	/** @type {{href?: string, title?: any, children?: import('svelte').Snippet}} */
	let { href = '', title = undefined, children } = $props();
</script>

<a {href} {title}>{@render children?.()}</a>
