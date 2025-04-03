<script lang="ts">
	import * as Table from '$lib/components/ui/table';
	import type { DeckEntry } from '../../../../app';

	import { PlusCircle } from 'lucide-svelte';
	import { createSvelteTable, FlexRender } from '$lib/components/ui/data-table';
	import { getCardColumns } from './card-table-columns.ts';
	import { getCoreRowModel } from '@tanstack/table-core';

	interface Props {
		cards: DeckEntry[];
		category: string;
		mouseOver: (link: IvionCard) => () => void;
	}

	let { cards, category, mouseOver }: Props = $props();

	let totalCards = $derived(cards.map((it) => it.count).reduce((acc, it) => acc + it, 0));

	let columns = getCardColumns(category);

		const table = createSvelteTable({
			get data() {
				return cards;
			},
			columns,
			getCoreRowModel: getCoreRowModel(),
	})

</script>

<div class="rounded">
	<Table.Root>
		<Table.Header>
			<Table.Row>
				<Table.Head colspan={2}>
					<h6 class="block">{category} ({totalCards}) <a href="">
						<PlusCircle />
					</a>
					</h6>
				</Table.Head>
			</Table.Row>
		</Table.Header>
		<Table.Body>
			{#each table.getRowModel().rows as row (row.id)}
						<Table.Row onmouseover={mouseOver(row.original.card)}
											 onfocus={mouseOver(row.original.card)}
											 onblur={null}
											 aria-hidden="true">
								{#each row.getVisibleCells() as cell (cell.id)}
											<Table.Cell>
												<FlexRender
													content={cell.column.columnDef.cell}
													context={cell.getContext()}
													/>
											</Table.Cell>
								{/each}
						</Table.Row>
			{/each}
		</Table.Body>
	</Table.Root>
</div>
