<script lang="ts">
	import '../../app.css';

	import Header from '../Header.svelte';
	import Footer from '../footer.svelte';
	import type { LayoutData } from './$types';
	import type { Snippet } from 'svelte';

	let { data = $bindable(), children }: {data: LayoutData, children: Snippet} = $props();

</script>

<div class="app">
	<Header showSearch={false} deckForm={data.deckForm}/>

	<main>
		{@render children?.()}
	</main>

	<Footer />
</div>

<style>
	:root {
		--global-header-height: 3rem;
		--color-bg-0: rgb(202, 216, 228);
		--color-bg-1: hsl(209, 36%, 86%);
		--color-bg-2: hsl(224, 44%, 95%);
		--color-theme-1: #ff3e00;
		--color-theme-2: #4075a6;
		--color-text: rgba(0, 0, 0, 0.7);
		--column-width: 42rem;
		--column-margin-top: 4rem;
	}

	.app {
		display: flex;
		flex-direction: column;
		min-height: 100vh;
		max-width: 100vw;
		color: var(--color-text);
	}

	main {
		flex: 1;
		display: flex;
		flex-direction: column;
		padding: 1rem;
		width: 100%;
		max-width: 64rem;
		margin: 0 auto;
		box-sizing: border-box;
		min-height: calc(100vh - var(--global-header-height));
	}
</style>
