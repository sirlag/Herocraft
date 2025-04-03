import type { ColumnDef } from "@tanstack/table-core";
import { createRawSnippet } from 'svelte';
import Time from 'svelte-time';
import { renderComponent, renderSnippet } from '$lib/components/ui/data-table';
import DataTableActions from './data-table-actions.svelte'

type DeckDisplay = {
	name: string;
	hash: string;
}

export type DeckListing = {
	id: string;
	hash: string;
	name: string;
	format: string;
	lastModified: Date;
};


export const columns: ColumnDef<DeckListing>[] = [
	{
		accessorKey: 'name',
		header: "Name",
		cell: ({row}) => {
			const nameCellSnippet = createRawSnippet<[DeckDisplay]>((getDisplayInfo) => {
				const {name, hash} = getDisplayInfo();
				return {
					render: () => `<div class=""><a href="/deck/${hash}">${name}</a></div>`,
				}
			})

			return renderSnippet(
				nameCellSnippet,
				row.original
			)
		}
	},
	{
		accessorKey: 'format',
		header: "Format"
	},
	{
		accessorKey: "lastModified",
		header: "Last Updated",
		cell: ({row}) => {
			// @ts-ignore
			return renderComponent(Time, {
								timestamp: row.original.lastModified,
								relative: true
							});
		}
	},
	{
		id: "actions",
		cell: ({row}) => {
			return renderComponent(DataTableActions, {id: row.original.id})
		},
	},
];