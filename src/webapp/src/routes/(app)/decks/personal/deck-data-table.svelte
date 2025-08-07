<script lang="ts" generics="TData, TValue">
 import { type ColumnDef, getCoreRowModel, getSortedRowModel } from '@tanstack/table-core';
 import {
 	createSvelteTable,
 	FlexRender
 } from '$lib/components/ui/data-table/index.ts';
 import * as Table from '$lib/components/ui/table/index.ts';
 import { ChevronDown, ChevronUp } from 'lucide-svelte';


 type DataTableProps<TData, TValue> = {
 	columns: ColumnDef<TData, TValue>[];
 	data: TData[];
 };

 let { data, columns }: DataTableProps<TData, TValue> = $props();

 let sorting = $state([{ id: 'lastModified', desc: true }]);

const table = createSvelteTable({
 	get data() {
 		return data;
 	},
 	columns,
 	getCoreRowModel: getCoreRowModel(),
 	getSortedRowModel: getSortedRowModel(),
 	state: {
 		get sorting() {
 			return sorting;
 		}
 	},
 	onStateChange: (updater) => {
 		if (typeof updater === 'function') {
 			const newState = updater(table.getState());
 			if (newState.sorting !== undefined) {
 				sorting = newState.sorting;
 			}
 		} else if (updater.sorting !== undefined) {
 			sorting = updater.sorting;
 		}
 	}
 });

</script>

<Table.Root>
	<Table.Header>
		{#each table.getHeaderGroups() as headerGroup (headerGroup.id)}
			<Table.Row>
				{#each headerGroup.headers as header (header.id)}
					<Table.Head>
						{#if !header.isPlaceholder}
							<div class="flex items-center">
								<button
									class="flex items-center gap-1"
									on:click={() => header.column.toggleSorting()}
									disabled={!header.column.getCanSort()}
								>
									<FlexRender
										content={header.column.columnDef.header}
										context={header.getContext()}
									/>
									{#if header.column.getCanSort()}
										<span class="ml-1">
											{#if header.column.getIsSorted() === 'asc'}
												<ChevronUp class="h-4 w-4" />
											{:else if header.column.getIsSorted() === 'desc'}
												<ChevronDown class="h-4 w-4" />
											{:else}
												<ChevronUp class="h-4 w-4 opacity-30" />
											{/if}
										</span>
									{/if}
								</button>
							</div>
						{/if}
					</Table.Head>
				{/each}
			</Table.Row>
		{/each}
	</Table.Header>
	<Table.Body >
		{#each table.getSortedRowModel().rows as row (row.id)}
			<Table.Row data-state={row.getIsSelected() && "selected"} class="h-10">
				{#each row.getVisibleCells() as cell (cell.id)}
					<Table.Cell>
							<FlexRender
								content={cell.column.columnDef.cell}
								context={cell.getContext()}
							/>
					</Table.Cell>
				{/each}
			</Table.Row>
		{:else}
			<Table.Row>
				<Table.Cell colspan={columns.length} class="h-24 text-center">
					No results.
				</Table.Cell>
			</Table.Row>
		{/each}
	</Table.Body>
</Table.Root>