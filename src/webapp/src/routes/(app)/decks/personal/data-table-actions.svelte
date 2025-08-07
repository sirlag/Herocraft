<script lang="ts">
	import Ellipsis from 'lucide-svelte/icons/ellipsis';
	import * as DropdownMenu from '$lib/components/ui/dropdown-menu';
	import { Button } from '$lib/components/ui/button';
	import { enhance } from '$app/forms';

	interface Props {
		id: string;
		hash: string;
		visibility?: string;
	}

	let { id, hash, visibility = 'public' }: Props = $props();
	
	// Convert visibility to lowercase for consistency
	const currentVisibility = $derived(visibility.toLowerCase());
	
	// Define all possible visibility states
	const visibilityStates = ['public', 'unlisted', 'private'];
	
	// Filter out the current visibility state to show only the other two options
	const otherVisibilityStates = $derived(visibilityStates.filter(state => state !== currentVisibility));
</script>

<div class="flex float-right">
	<DropdownMenu.Root>
		<DropdownMenu.Trigger >
			{#snippet child({ props })}
				<Button {...props} variant="ghost"  size="icon" class="relative h-8 w-8 p-0">
					<span class="sr-only">Open Menu</span>
					<Ellipsis class="h-4 w-4" />
				</Button>
			{/snippet}
		</DropdownMenu.Trigger>
		<DropdownMenu.Content>
			<DropdownMenu.Group>
				{#each otherVisibilityStates as state}
					<DropdownMenu.Item closeOnSelect={false}>
						<form method="POST" action="?/toggleVisibility" use:enhance>
							<input class="hidden" aria-hidden="true" type="text" name="id" value={id} />
							<input class="hidden" aria-hidden="true" type="text" name="visibility" value={state} />
							<button>Set Visibility to {state.charAt(0).toUpperCase() + state.slice(1)}</button>
						</form>
					</DropdownMenu.Item>
				{/each}

				<a href="/deck/{hash}/settings">
					<DropdownMenu.Item closeOnSelect={false}>
						<button>Settings</button>
					</DropdownMenu.Item>
				</a>
			</DropdownMenu.Group>
			<DropdownMenu.Separator />
			<DropdownMenu.Group>
				<DropdownMenu.Item closeOnSelect={false}>
					<form method="POST" action="?/delete" use:enhance>
						<input class="hidden" aria-hidden="true" type="text" name="id" value={id} />
						<button><span class="text-red-600">Delete Deck</span></button>
					</form>
				</DropdownMenu.Item>
			</DropdownMenu.Group>
		</DropdownMenu.Content>
	</DropdownMenu.Root>
</div>
