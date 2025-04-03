<script>
	/** @type {{ordered: any, start: any, children?: import('svelte').Snippet}} */
	let { ordered, start, children } = $props();
</script>

{#if ordered}
	<ol {start}>{@render children?.()}</ol>
{:else}
	<ul>{@render children?.()}</ul>
{/if}
