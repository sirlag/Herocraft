<script lang="ts">
	import * as Dialog from '$lib/components/ui/dialog';
	import { Button, buttonVariants } from '$lib/components/ui/button';
	import NewDeckForm from '$lib/components/NewDeck/NewDeckForm.svelte';

	interface Props {
		form: any;
		children?: import('svelte').Snippet;
	}

	let { form, children }: Props = $props();

	let open = $state(false);

	const handleSubmit = () => {
		open = false;
	};
</script>

<Dialog.Root bind:open>
	<Dialog.Trigger class={buttonVariants({ variant: 'header', size: 'header' })}
		>{@render children?.()}</Dialog.Trigger
	>
	<Dialog.Content>
		<Dialog.Title>New Deck</Dialog.Title>

		<NewDeckForm data={form} {handleSubmit} />

		<Dialog.Footer>
			<Button form="deckForm" type="submit">Create</Button>
		</Dialog.Footer>
	</Dialog.Content>
</Dialog.Root>
