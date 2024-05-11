<script lang="ts">
	import { createRender, createTable, Render, Subscribe } from 'svelte-headless-table';
	import { readable } from 'svelte/store';
	import * as Table from '$lib/components/ui/table';
	import type { DeckEntry } from '../../../../app';

	import LinkCell from '$lib/components/data-table/link-cell.svelte';

	export let cards: DeckEntry[];
	export let category: string;
	export let mouseOver: (link: IvionCard) => () => void;

	$: totalCards = cards.map((it) => it.count).reduce((acc, it) => acc + it, 0);

	const table = createTable(readable(cards));
	const columns = table.createColumns([
		table.column({
			accessor: 'count',
			header: ''
		}),
		table.column({
			accessor: 'card',
			header: category,
			cell: ({ value }) => {
				return createRender(LinkCell, {
					text: value.name,
					link: `/card/${value.id}`
				});
			}
		})
	]);

	const { headerRows, pageRows, tableAttrs, tableBodyAttrs } = table.createViewModel(columns);
</script>

<div class="rounded">
	<Table.Root {...$tableAttrs}>
		<Table.Header>
			<Table.Row>
				<Table.Head colspan={2}>
					<h6>{category} ({totalCards})</h6>
				</Table.Head>
			</Table.Row>
		</Table.Header>
		<Table.Body {...$tableBodyAttrs}>
			{#each $pageRows as row (row.id)}
				<Subscribe rowAttrs={row.attrs()} let:rowAttrs>
					<Table.Row>
						<div on:mouseover={mouseOver(row.cells[1].value)} {...rowAttrs}>
							{#each row.cells as cell (cell.id)}
								<Subscribe attrs={cell.attrs()} let:attrs>
									<Table.Cell {...attrs}>
										<Render of={cell.render()} />
									</Table.Cell>
								</Subscribe>
							{/each}
						</div>
					</Table.Row>
				</Subscribe>
			{/each}
		</Table.Body>
	</Table.Root>
</div>
