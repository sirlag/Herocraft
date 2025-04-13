<script lang="ts">
	import * as DropdownMenu from "$lib/components/ui/dropdown-menu/index.js";
	import { EllipsisVertical } from 'lucide-svelte';
	import { buttonVariants } from "$lib/components/ui/button/index.js";

	interface Props {
		card: IvionCard,
		count: number,
		modify: (card: IvionCard, count: number) => void;
	}

	let { card, count, modify } = $props();

</script>

<DropdownMenu.Root>
	<DropdownMenu.Trigger class={buttonVariants({variant: "ghost", size: "sm-icon"})}>
		<EllipsisVertical size={64}/>
	</DropdownMenu.Trigger>
	<DropdownMenu.Content>
		<DropdownMenu.Group>
			<DropdownMenu.Item
				onclick={() => modify(card, count + 1)}
			>Add One</DropdownMenu.Item>
			<DropdownMenu.Item
				class="text-red-600 data-[highlighted]:text-red-100 data-[highlighted]:bg-red-500"
				onclick={() => {modify(card, count - 1)}}
			>
				Remove
			</DropdownMenu.Item>
			{#if count > 1}
				<DropdownMenu.Item
					class="text-red-600 data-[highlighted]:text-red-100 data-[highlighted]:bg-red-500"
					onclick={() => {modify(card, 0)}}
				>
					Remove All
				</DropdownMenu.Item>
			{/if}

		</DropdownMenu.Group>
	</DropdownMenu.Content>
</DropdownMenu.Root>
