<script lang="ts">
	import { createRender, createTable, Render, Subscribe } from 'svelte-headless-table';
	import { readable, writable } from 'svelte/store';
	import * as Table from '$lib/components/ui/table';
	import DataTableActions from './data-table-actions.svelte';

	import LinkCell from '$lib/components/data-table/link-cell.svelte';
	import assert from 'node:assert';
	import Time from 'svelte-time';

	type Deck = {
		id: string;
		hash: string;
		name: string;
		format: string;
		lastModified: Date;
	};

	type DeckEntry = {
		display: {
			name: string;
			hash: string;
		};
		format: string;
		lastModified: Date;
		id: string;
	};

	interface Props {
		decks: Deck[];
	}

	let { decks }: Props = $props();
	let relativeFormat = new Intl.RelativeTimeFormat('en-US', { style: 'short' });

	let mappedDecks = decks.map((it) => {
		return {
			display: {
				name: it.name,
				hash: it.hash
			},
			format: it.format,
			lastModified: it.lastModified,
			id: it.id
		};
	});

	let table = createTable(writable(mappedDecks));
	let columns = table.createColumns([
		table.column({
			accessor: 'display',
			header: 'Name',
			cell: ({ value }) => {
				return createRender(LinkCell, {
					text: value.name,
					link: `/deck/${value.hash}`
				});
			}
		}),
		table.column({
			accessor: 'format',
			header: 'Format'
		}),
		table.column({
			accessor: 'lastModified',
			header: 'Last Updated',
			cell: ({ value }) => {
				return createRender(Time, {
					timestamp: value,
					relative: true
				});
				// return relativeFormat.format(value)
			}
		}),
		table.column({
			accessor: 'id',
			header: '',
			cell: ({ value }) => {
				return createRender(DataTableActions, { id: value });
			}
		})
	]);

	const { headerRows, pageRows, tableAttrs, tableBodyAttrs } = table.createViewModel(columns);
</script>

<div class="rounded border">
	<Table.Root {...$tableAttrs}>
		<Table.Header>
			{#each $headerRows as headerRow}
				<Subscribe rowAttrs={headerRow.attrs()}>
					<Table.Row>
						{#each headerRow.cells as cell (cell.id)}
							<Subscribe attrs={cell.attrs()}  props={cell.props()}>
								{#snippet children({ attrs })}
																<Table.Head {...attrs}>
										<Render of={cell.render()} />
									</Table.Head>
																							{/snippet}
														</Subscribe>
						{/each}
					</Table.Row>
				</Subscribe>
			{/each}
		</Table.Header>
		<Table.Body {...$tableBodyAttrs}>
			{#each $pageRows as row (row.id)}
				<Subscribe rowAttrs={row.attrs()} >
					{#snippet children({ rowAttrs })}
										<Table.Row {...rowAttrs}>
							{#each row.cells as cell (cell.id)}
								<Subscribe attrs={cell.attrs()} >
									{#snippet children({ attrs })}
																<Table.Cell {...attrs}>
											<Render of={cell.render()} />
										</Table.Cell>
																								{/snippet}
														</Subscribe>
							{/each}
						</Table.Row>
														{/snippet}
								</Subscribe>
			{/each}
		</Table.Body>
	</Table.Root>
</div>
