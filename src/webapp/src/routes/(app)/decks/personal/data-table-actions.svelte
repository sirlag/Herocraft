<script lang="ts">
	import Ellipsis from 'lucide-svelte/icons/ellipsis';
	import * as DropdownMenu from '$lib/components/ui/dropdown-menu'
	import { Button } from "$lib/components/ui/button"

	export let id: string
</script>

<div class="flex float-right">
	<DropdownMenu.Root>
		<DropdownMenu.Trigger asChild let:builder>
			<Button
				variant="ghost"
				builders="{[builder]}"
				size="icon"
				class="relative h-8 w-8 p-0"
			>
				<span class="sr-only">Open Menu</span>
				<Ellipsis class="h-4 w-4"/>
			</Button>
		</DropdownMenu.Trigger>
		<DropdownMenu.Content>
			<DropdownMenu.Group>
				<DropdownMenu.Item on:click={() => navigator.clipboard.writeText(id)}>
					<span class="text-red-600">Delete Deck</span>
				</DropdownMenu.Item>
			</DropdownMenu.Group>
		</DropdownMenu.Content>
	</DropdownMenu.Root>
</div>