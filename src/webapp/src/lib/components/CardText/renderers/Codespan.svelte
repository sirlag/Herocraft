<script>
	/** @type {{raw: any}} */
	let { raw } = $props();
</script>

<code>{raw.replace(/`/g, '')}</code>
