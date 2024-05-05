<script lang="ts">
	import { createRender, createTable, Render, Subscribe } from 'svelte-headless-table';
	import { readable, writable } from 'svelte/store';
	import * as Table from "$lib/components/ui/table"
	import DataTableActions from "./data-table-actions.svelte"

	import LinkCell from "$lib/components/data-table/link-cell.svelte"

	type Deck = {
		id: string;
		hash: string;
		name: string;
	};

	type DeckEntry = {
		display: {
			name: string;
			hash: string;
		},
		id: string;
	}

	export let decks: Deck[]

	let mappedDecks  = decks.map((it) =>{
		return {
			display: {
				name: it.name,
				hash: it.hash
			},
			id: it.id
		}
	})

	let table = createTable(writable(mappedDecks))
	let columns = table.createColumns([
		table.column({
			accessor: "display",
			header: "Name",
			cell: ({value}) => {
				return createRender(LinkCell, {
					text: value.name,
					link: `/deck/${value.hash}`
				})
			}
		}),
		table.column({
			accessor: "id",
			header: "",
			cell: ({value}) => {
				return createRender(DataTableActions, { id: value })
			}
		})
	]);

	const { headerRows, pageRows, tableAttrs, tableBodyAttrs } = table.createViewModel(columns)
</script>

<div class="rounded border">
	<Table.Root {...$tableAttrs}>
		<Table.Header>
			{#each $headerRows as headerRow}
				<Subscribe rowAttrs={headerRow.attrs()}>
					<Table.Row>
						{#each headerRow.cells as cell (cell.id)}
							<Subscribe attrs={cell.attrs()} let:attrs props={cell.props()}>
								<Table.Head {...attrs}>
									<Render of={cell.render()} />
								</Table.Head>
							</Subscribe>
						{/each}
					</Table.Row>
				</Subscribe>
			{/each}
		</Table.Header>
		<Table.Body {...$tableBodyAttrs}>
			{#each $pageRows as row (row.id)}
				<Subscribe rowAttrs={row.attrs()} let:rowAttrs>
					<Table.Row {...rowAttrs}>
						{#each row.cells as cell (cell.id)}
							<Subscribe attrs={cell.attrs()} let:attrs>
								<Table.Cell {...attrs}>
									<Render of={cell.render()} />
								</Table.Cell>
							</Subscribe>
						{/each}
					</Table.Row>
				</Subscribe>
			{/each}
		</Table.Body>
	</Table.Root>
</div>