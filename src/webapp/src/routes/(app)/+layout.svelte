<script lang="ts">

	import '../../app.css';
	import Header from '../Header.svelte';
	import Footer from '../footer.svelte';
	import type { LayoutData } from './$types'
	import type { Snippet } from 'svelte';

	let { data = $bindable(), children }: {data: LayoutData, children: Snippet} = $props();
</script>

<div class="flex flex-col min-h-lvh">
	<Header showSearch={true} deckForm={data.deckForm} />

	<main class="flex flex-1 flex-col mx-0 my-0 box-border max-w-wh">
		{@render children?.()}
	</main>

	<Footer />
</div>

<style lang="postcss">
	:root {
		--global-header-height: 3rem;
	}

	main {
		min-height: calc(100vh - var(--global-header-height));
	}
</style>
