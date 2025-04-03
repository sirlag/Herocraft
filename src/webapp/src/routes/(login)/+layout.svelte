<script>
	import '../../app.css';
	/** @type {{children?: import('svelte').Snippet}} */
	let { children } = $props();
</script>

<div>
	<main class="min-h-lvh w-full max-w-5xl">
		<div>
			{@render children?.()}
		</div>
	</main>
</div>

<!--class="flex flex-col -->

<!--class="flex flex-1 flex-col p-4 w-full max-w-5xl mx-auto my-0 box-border"-->
