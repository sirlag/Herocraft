<script>
	import "../../app.css";
	import Header from '../Header.svelte';
	import Footer from '../footer.svelte';
</script>

<div class="flex flex-col min-h-lvh">
	<Header showSearch={true} />

	<main class="flex flex-1 flex-col p-4 w-full max-w-5xl mx-auto my-0 box-border">
		<div >
			<slot />
		</div>
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