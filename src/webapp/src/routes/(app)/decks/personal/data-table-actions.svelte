<script lang="ts">
	import Ellipsis from 'lucide-svelte/icons/ellipsis';
	import * as DropdownMenu from '$lib/components/ui/dropdown-menu';
	import { Button } from '$lib/components/ui/button';
	import { enhance } from '$app/forms';

	interface Props {
		id: string;
	}

	let { id }: Props = $props();
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
				<DropdownMenu.Item>
					<form method="POST" action="?/delete" use:enhance>
						<input class="hidden" aria-hidden="true" type="text" name="id" value={id} />
						<button><span class="text-red-600">Delete Deck</span></button>
					</form>
				</DropdownMenu.Item>
			</DropdownMenu.Group>
		</DropdownMenu.Content>
	</DropdownMenu.Root>
</div>
