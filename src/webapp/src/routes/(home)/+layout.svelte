<script>
	import '../../app.css';

	import Header from '../Header.svelte';
	import Footer from '../footer.svelte';
</script>

<div class="app">
	<Header showSearch={false} />

	<main>
		<slot />
	</main>

	<Footer />
</div>

<style>
	:root {
		--global-header-height: 3rem;
	}

	.app {
		display: flex;
		flex-direction: column;
		min-height: 100vh;
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

	:root {
		--font-body: Arial, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu,
			Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
		--font-mono: 'Fira Mono', monospace;
		--color-bg-0: rgb(202, 216, 228);
		--color-bg-1: hsl(209, 36%, 86%);
		--color-bg-2: hsl(224, 44%, 95%);
		--color-theme-1: #ff3e00;
		--color-theme-2: #4075a6;
		--color-text: rgba(0, 0, 0, 0.7);
		--column-width: 42rem;
		--column-margin-top: 4rem;
		font-family: var(--font-body);
		color: var(--color-text);
	}
</style>
