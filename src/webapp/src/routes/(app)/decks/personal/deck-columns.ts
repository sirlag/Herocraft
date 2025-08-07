import type { ColumnDef } from "@tanstack/table-core";
import { createRawSnippet } from 'svelte';
import Time from 'svelte-time';
import { renderComponent, renderSnippet } from '$lib/components/ui/data-table';
import DataTableActions from './data-table-actions.svelte'
import FavoriteToggle from './favorite-toggle.svelte'
import NameRow from './name-row.svelte'
import { render } from 'svelte/server';

type DeckDisplay = {
	name: string;
	hash: string;
}

export type DeckListing = {
	id: string;
	favorite: boolean;
	hash: string;
	name: string;
	format: string;
	lastModified: Date;
	visibility: string;
};


export const columns: ColumnDef<DeckListing>[] = [
	{
		accessorKey: 'name',
		header: "Name",
		enableSorting: true,
		cell: ({row}) => {
			return renderComponent(NameRow, {
				name: row.original.name,
				hash: row.original.hash,
				id: row.original.id,
				favorite: row.original.favorite,
				visibility: row.original.visibility
			})
		}
	},
	{
		accessorKey: 'format',
		header: "Format",
		enableSorting: true
	},
	{
		accessorKey: "lastModified",
		header: "Last Updated",
		enableSorting: true,
		sortingFn: "datetime",
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
		enableSorting: false,
		cell: ({row}) => {
			return renderComponent(DataTableActions, {id: row.original.id, hash: row.original.hash})
		},
	},
];