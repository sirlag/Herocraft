<script lang="ts">
	import * as Dialog from '$lib/components/ui/dialog';
	import { Button, buttonVariants } from '$lib/components/ui/button';
	import NewDeckForm from '$lib/components/NewDeck/NewDeckForm.svelte';

	interface Props {
		form: any;
		children?: import('svelte').Snippet;
		open?: boolean;
	}

	let { form, children, open = $bindable(false) }: Props = $props();

	const handleSubmit = () => {
		open = false;
	};

	const handleOpenChange = (newOpen: boolean) => {
		open = newOpen;
	};
</script>

<Dialog.Root {open} onOpenChange={handleOpenChange}>
	{#if children}
		<Dialog.Trigger class={buttonVariants({ variant: 'header', size: 'header' })}
			>{@render children?.()}</Dialog.Trigger>
	{/if}
	<Dialog.Content>
		<Dialog.Title>New Deck</Dialog.Title>

		<NewDeckForm data={form} {handleSubmit} />

		<Dialog.Footer>
			<Button form="deckForm" type="submit">Create</Button>
		</Dialog.Footer>
	</Dialog.Content>
</Dialog.Root>
