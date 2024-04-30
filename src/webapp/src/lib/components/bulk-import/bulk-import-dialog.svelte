<script lang="ts">
	import * as Dialog from "$lib/components/ui/dialog"
	import * as Form from '$lib/components/ui/form'
	import * as RadioGroup from '$lib/components/ui/radio-group';
	import { Button, buttonVariants } from '$lib/components/ui/button';
	import { Input } from '$lib/components/ui/input';
	import { InputFile } from '$lib/components/ui/input-file';
	import { Checkbox } from '$lib/components/ui/checkbox'
	import { ScrollArea } from '$lib/components/ui/scroll-area'

	import SuperDebug, {
		type SuperValidated,
		type Infer,
		superForm, fileProxy
	} from 'sveltekit-superforms';
	import { zodClient } from 'sveltekit-superforms/adapters';
	import { formSchema, type FormSchema } from './schema.ts';
	import RadioItem from '$lib/components/RadioItem.svelte';
	import type { C2Array } from '../../../app';


	export let data: SuperValidated<Infer<FormSchema>>;

	const form = superForm(data, {
		validators: zodClient(formSchema),
		onSubmit: () => {
			handleSubmit()
		}
	})

	const { form: formData, enhance } = form;

	const file = fileProxy(formData, 'deckBuilderSave')

	const viewDecks = (file: File) => {
		if (file) {
			const reader = new FileReader();
			reader.addEventListener("load", () => {
				listOfDecks =  (typeof reader.result === 'string')
					? listOfDecks = JSON.parse(reader.result)
					: undefined
			})
			reader.readAsText(file)
		}
	}

	$: deckViewer = viewDecks($formData.deckBuilderSave)
	let listOfDecks: C2Array | undefined

	let open = false

	const handleSubmit = () => {
		data.data
	}

	function addItem(id: string) {
		console.log(id)
		$formData.decks = [...$formData.decks, id];
	}

	function removeItem(id: string) {
		console.log(id)
		console.log($formData.decks)
		$formData.decks = $formData.decks.filter((i) => i !== id);
	}

</script>

<Dialog.Root bind:open>
	<Dialog.Trigger class={buttonVariants({variant: "link"})}><slot/></Dialog.Trigger>
	<Dialog.Content>
		<Dialog.Title>Import From Aldenwar's Ivion Deckbuilder</Dialog.Title>
		<form
			id="bulkImportForm"
			method="POST"
			action="/decks/import"
			encType="multipart/form-data"
			use:enhance
		>
			<Form.Field {form} name="deckBuilderSave" class="pb-4">
				<Form.Control let:attrs>
					<Form.Label>Ivion Deck Builder Save</Form.Label>
					<InputFile
						{...attrs}
						type="file"
						accept="application/json"
						bind:files={$file}
					/>
				</Form.Control>
				<Form.FieldErrors />
			</Form.Field>

			{#if (listOfDecks !== undefined) }
				<Form.Fieldset {form} name="decks" class="space-y-0 pb-4">
					<div class="mb-4">
						<Form.Legend class="text-base">Decks</Form.Legend>
						<Form.Description>
							Select which decks you want to import.
						</Form.Description>
					</div>
					<div class="space-y-2">
						<ScrollArea class="flex flex-col h-[14rem] w-full rounded-lg border p-4">
						{#each listOfDecks.data as deckList}
							{@const checked = $formData.decks.includes(deckList[0][0])}
								<div class="flex flex-row items-start space-x-3 align-middle m-2 h-8">
									<Form.Control let:attrs>
										<Checkbox
											{...attrs}
											{checked}
											onCheckedChange={(v) => {
												if (v) {
													addItem(deckList[0][0]);
												} else {
													removeItem(deckList[0][0]);
												}
											}}
										/>
										<Form.Label class="font-normal">
											{deckList[0]}
										</Form.Label>
										<input
											hidden
											type="checkbox"
											name={attrs.name}
											value={deckList[0][0]}
											{checked}
										/>
									</Form.Control>
								</div>

						{/each}
						</ScrollArea>

					</div>
				</Form.Fieldset>
			{/if}

			<Form.Fieldset {form} name="defaultVisibility">
				<Form.Legend>Default Visibility</Form.Legend>
				<RadioGroup.Root
					bind:value={$formData.defaultVisibility}
					class="flex flex-row py-4"
				>
					<RadioItem value="public" text="Public" />
					<RadioItem value="unlisted" text="Unlisted" />
					<RadioItem value="private" text="Private" />
					<RadioGroup.Input name="defaultVisibility" />
				</RadioGroup.Root>
			</Form.Fieldset>
		</form>
		<Dialog.Footer>
			<Button form="bulkImportForm" type="submit">Import</Button>
		</Dialog.Footer>
	</Dialog.Content>

</Dialog.Root>