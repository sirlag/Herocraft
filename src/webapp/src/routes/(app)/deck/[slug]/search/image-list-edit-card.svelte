<script lang="ts">
	import CardImage from '$lib/components/CardImage.svelte'
	import { Button } from '$lib/components/ui/button'
	import { Input } from '$lib/components/ui/input'
	import { LoaderCircle, MinusIcon, PlusIcon } from 'lucide-svelte';

	interface Props {
		card: IvionCard;
		count?: number;
		modify: (card: IvionCard, change: number) => Promise<void>;
	}

	let { card, count = 0, modify }: Props = $props();

	const onClick = async (change: number) => {
		if (change > 0) {
			plusLoading = true
		} else {
			minLoading = true
		}
		await modify(card, change)
		if (change > 0) {
			plusLoading = false
		} else {
			minLoading = false
		}
	}

	let minLoading = $state(false);
	let plusLoading = $state(false);

</script>

<div class="m-1 isolate relative">
	<div class="absolute top-0 left-0 right-0 p-2 px-3 z-10 text-xs box-border text-transparent pointer-events-none">
		{card.name}
	</div>
	<div class="absolute top-12 right-4 z-10 flex align-middle space-x-1 box-border text-right">
		{#if count > 0}
				<Button size="sm-icon" variant="secondary" class="p-1" disabled={minLoading} on:click={() => onClick(-1)} >
					{#if (minLoading)}
						<LoaderCircle class="animate-spin"/>
					{:else}
						<MinusIcon/>
					{/if}
				</Button>
				<Input class="h-6 w-16 inline text-right mb-8" readonly value={count}/>
				<Button size="sm-icon" variant="secondary" class="p-1" disabled={plusLoading} on:click={() => onClick(+1)} >
					{#if (plusLoading)}
						<LoaderCircle class="animate-spin"/>
					{:else}
						<PlusIcon/>
					{/if}
				</Button>
		{:else}
			<Button size="sm-icon" variant="secondary" class="p-1" disabled={plusLoading} on:click={() => onClick(+1)} >
				{#if (plusLoading)}
					<LoaderCircle class="animate-spin"/>
				{:else}
					<PlusIcon/>
				{/if}
			</Button>
		{/if}
	</div>
	<CardImage {card} />
</div>
