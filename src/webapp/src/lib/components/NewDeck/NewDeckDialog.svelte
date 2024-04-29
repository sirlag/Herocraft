<script lang="ts">
	import * as Dialog from "$lib/components/ui/dialog"
	import { Button, buttonVariants } from '$lib/components/ui/button';
	import NewDeckForm from '$lib/components/NewDeck/NewDeckForm.svelte';
	import { Footer } from '$lib/components/ui/dialog';

	export let form;

	let open = false

	const handleSubmit = () => {
		open = false
	}

</script>

<Dialog.Root bind:open>
	<Dialog.Trigger class={buttonVariants({ variant: "header", size: "header"})}><slot/></Dialog.Trigger>
	<Dialog.Content>
		<Dialog.Title>New Deck</Dialog.Title>

		<NewDeckForm data={form} handleSubmit={handleSubmit} />

		<Footer>
			<Button form="deckForm" type="submit">Create</Button>
		</Footer>
	</Dialog.Content>
</Dialog.Root>
