<script>
	import { Input } from '$lib/components/ui/input/';
	import { Button } from '$lib/components/ui/button/';
</script>

<svelte:head>
	<title>Home</title>
	<meta name="description" content="Svelte demo app" />
</svelte:head>

<section>
	<div>
		<h1>
			<b>Herocraft</b> is the Unofficial <b>Ivion</b> Card Database and Deck Builder
		</h1>
		<div>
			<Input />
		</div>
		<div class="flex mr-8 p-2 justify-end">
			<Button href="/cards" data-sveltekit-preload-data>All Cards</Button>
		</div>
	</div>


</section>

<style>
	section {
		display: flex;
		flex-direction: column;
		justify-content: center;
		align-items: center;
		flex: 0.6;
	}

	h1 {
		width: 100%;
	}

	body {
			background: gold;
	}

</style>
