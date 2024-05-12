<script lang="ts">
	import { SearchInput } from '$lib/components/ui/search-input/';
	import { Button } from '$lib/components/ui/button/';
	import { goto } from '$app/navigation';

	let search: string;

	// let navigate = () => {
	// 	goto(`/cards?q=${search}`);
	// };
</script>

<svelte:head>
	<title>Home // Herocraft</title>
	<meta
		name="description"
		content="Herocraft is the Unofficial deckbuilder and card database for Ivion."
	/>
</svelte:head>

<section>
	<div>
		<h1>
			<b>Herocraft</b> is the Unofficial <b>Ivion</b> Card Database and Deck Builder
		</h1>
		<div>
			<form method="GET" action="/cards">
				<SearchInput name="q" bind:value={search} />
				<!--				<button type="submit" class="visually-hidden"></button>-->
			</form>
		</div>
		<div class="flex mr-8 p-2 justify-end">
			<Button href="/cards" data-sveltekit-preload-data="hover">All Cards</Button>
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
