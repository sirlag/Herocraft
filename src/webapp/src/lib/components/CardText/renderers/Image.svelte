<script>
	/** @type {{href?: string, title?: any, text?: string}} */
	let { href = '', title = undefined, text = '' } = $props();
</script>

<img src={href} {title} alt={text} />
