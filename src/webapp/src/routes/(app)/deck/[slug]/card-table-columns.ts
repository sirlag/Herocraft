import type { ColumnDef } from '@tanstack/table-core';
import { createRawSnippet } from 'svelte';
import type { DeckEntry } from '../../../../app';
import { renderSnippet } from '$lib/components/ui/data-table';

export let getCardColumns = (category: string):ColumnDef<DeckEntry>[] =>  ([
	{
		accessorKey: 'count',
		header: ''
	},
	{
		accessorKey: 'card',
		header: category,
		cell: ({row}) => {
			const nameCellSnippet = createRawSnippet<[DeckEntry]>((getCardListing) => {
				const {card} = getCardListing()
				return {
					render: () => `<div class=""><a href="/card/${card.id}">${card.name}</a></div>"`
				}
			})
			return renderSnippet(
				nameCellSnippet,
				row.original
			)
		}
	}
]);