<script lang="ts">
	import type { Snippet } from 'svelte';

	let { children }: {children: Snippet} = $props();

</script>

<svelte:head>
	<!-- SEO -->
	<link
		rel="search"
		type="application/opensearchdescription+xml"
		Title="Herocraft"
		href="https://herocraft.app/opensearch.xml"
	/>
	<meta name="twitter:title" content="Herocraft" />
	<meta property="og:site_name" content="Herocraft" />
	<meta property="og:type" content="website" />
	<meta property="og:url" content="https://herocraft.app/" />
	<meta property="og:title" content="Herocraft" />
	<meta property="og:description" content="A Card or Deck from Herocraft" />

	<!-- Weird -->
	<link rel="preconnect" type="https://api.herocraft.app" />
</svelte:head>

{@render children?.()}
