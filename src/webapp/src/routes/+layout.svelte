<script lang="ts">
    import type { Snippet } from 'svelte';
    import { page } from '$app/stores';
    import '../app.css';

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

    {#if $page.data?.seo}
        <!-- Page-specific SEO from page.server.ts -->
        <meta property="og:site_name" content={$page.data.seo.siteName || 'Herocraft'} />
        <meta property="og:type" content={$page.data.seo.type || 'website'} />
        <meta property="og:url" content={$page.data.seo.url} />
        <meta property="og:title" content={$page.data.seo.title} />
        {#if $page.data.seo.description}
            <meta property="og:description" content={$page.data.seo.description} />
        {/if}
        {#if $page.data.seo.image}
            <meta property="og:image" content={$page.data.seo.image} />
        {/if}

        <!-- Twitter cards -->
        <meta name="twitter:card" content="summary_large_image" />
        <meta name="twitter:title" content={$page.data.seo.title} />
        {#if $page.data.seo.description}
            <meta name="twitter:description" content={$page.data.seo.description} />
        {/if}
        {#if $page.data.seo.image}
            <meta name="twitter:image" content={$page.data.seo.image} />
        {/if}
    {:else}
        <!-- Global defaults (only when page does not provide SEO data) -->
        <meta property="og:site_name" content="Herocraft" />
        <meta property="og:type" content="website" />
        <meta property="og:url" content="https://herocraft.app/" />
        <meta property="og:title" content="Herocraft" />
        <meta property="og:description" content="A Card or Deck from Herocraft" />
        <meta name="twitter:card" content="summary" />
        <meta name="twitter:title" content="Herocraft" />
    {/if}

    <!-- Weird -->
    <link rel="preconnect" type="https://api.herocraft.app" />
</svelte:head>

{@render children?.()}
