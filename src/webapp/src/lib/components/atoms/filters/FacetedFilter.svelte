<script lang="ts">

	import * as Command from '$lib/components/ui/command';
	import * as Popover from '$lib/components/ui/popover';
	import { Button } from '$lib/components/ui/button';
	import type { Filters } from './index.ts';
	import { PlusCircle } from 'lucide-svelte';
	import { Separator } from '$lib/components/ui/separator';
	import { Badge } from '$lib/components/ui/badge';
	import { Checkbox } from '$lib/components/ui/checkbox';

	interface Props {
		title: string;
		options: Filters[];
		filterValues?: string[];
		displayCount?: number;
	}

	let {  title, options, filterValues = $bindable([]), displayCount = 3}: Props = $props();

	let open = $state(false);

	function handleSelect(currentValue: string) {
		if (Array.isArray(filterValues) && filterValues.includes(currentValue)) {
			filterValues = filterValues.filter((v) => v !== currentValue);
		} else {
			filterValues = [...(Array.isArray(filterValues) ? filterValues : []), currentValue];
		}
	}

</script>

<Popover.Root bind:open>
	<Popover.Trigger>
		<Button variant="outline" size="sm" class="h-8">
			<PlusCircle class="mr-2 h-4 w-4" />
			{title}
			{#if filterValues.length > 0}
				<Separator orientation="vertical" class="mx-2 h-4" />
				<Badge variant="secondary" class="rounded-sm px-1 font-normal lg:hidden">
					{filterValues.length}
				</Badge>
				<div class="hidden space-x-1 lg:flex">
					{#if filterValues.length > displayCount}
						<Badge variant="secondary" class="rounded-sm px-1 font-normal">
							{filterValues.length} Selected
						</Badge>
					{:else}
						{#each filterValues as option}
							<Badge variant="secondary" class="rounded-sm px-1 font-normal">
								{option}
							</Badge>
						{/each}
					{/if}
				</div>
			{/if}
		</Button>
	</Popover.Trigger>
	<Popover.Content class="w-[200px] p-0" align="start" side="bottom">
		<Command.Root>
			<Command.Input placeholder={title} />
			<Command.List>
				<Command.Empty>No results found.</Command.Empty>
				<Command.Group>
					{#each options as option}
						<Command.Item
							value={option.value}
							onSelect={() => {
								handleSelect(option.value)
							}}
						>
							<Checkbox
								checked={filterValues.includes(option.value)}
								aria-labelledby="filter-label"
								class="mr-2 rounded-sm" />
							<span id="filter-label">
								{option.label}
							</span>
						</Command.Item>
					{/each}
				</Command.Group>
				{#if filterValues.length > 0}
					<Command.Separator />
					<Command.Item class="justify-center text-center"
												onSelect={() => filterValues = []}>
						Clear Filters
					</Command.Item>
				{/if}
			</Command.List>
		</Command.Root>
	</Popover.Content>
</Popover.Root>