<script>
	import "../../../app.css";
	import Header from '../../Header.svelte';
</script>

<div class="flex flex-col min-h-lvh">
	<Header />

	<main class="flex flex-1 flex-col p-4 w-full max-w-5xl mx-auto my-0 box-border">
		<div >
			<slot />
		</div>
	</main>

</div>