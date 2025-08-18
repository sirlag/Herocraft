<script lang="ts">
	import { page } from '$app/stores';
	import { Button } from '$lib/components/ui/button';

	interface Props {
		isLoggedIn?: boolean;
	}

	let { isLoggedIn = false }: Props = $props();

	const currentPath = $derived($page.url.pathname);
	
	function isActive(path: string): boolean {
		return currentPath === path;
	}
</script>

<div class="mb-6">
	<nav class="flex space-x-1 bg-gray-100 p-1 rounded-lg">
		<Button
			href="/decks/public"
			variant={isActive('/decks/public') ? 'default' : 'ghost'}
			class="flex-1 justify-center"
		>
			Public Decks
		</Button>
		
		{#if isLoggedIn}
			<Button
				href="/decks/private"
				variant={isActive('/decks/private') ? 'default' : 'ghost'}
				class="flex-1 justify-center"
			>
				My Decks
			</Button>
			
			<Button
				href="/decks/liked"
				variant={isActive('/decks/liked') ? 'default' : 'ghost'}
				class="flex-1 justify-center"
			>
				Liked Decks
			</Button>
		{/if}
	</nav>
</div>