<script lang="ts">
	import * as Form from '$lib/components/ui/form';
	import * as RadioGroup from '$lib/components/ui/radio-group';
	import { Input } from '$lib/components/ui/input';
	import { deckSettingsSchema, type DeckSettingsSchema } from './schema.ts';
	import RadioItem from '$lib/components/RadioItem.svelte';

	import SuperDebug, { type SuperValidated, type Infer, superForm } from 'sveltekit-superforms';
	import { zodClient } from 'sveltekit-superforms/adapters';

	interface Props {
		data: SuperValidated<Infer<DeckSettingsSchema>>;
	}

	let { data }: Props = $props();

	let id = $derived(data.id);

	const form = superForm(data, {
		validators: zodClient(deckSettingsSchema)
	});

	const { form: formData, enhance } = form;
</script>

<form id="deckForm" method="POST" use:enhance>
	<Form.Field {form} name="name">
		<Form.Control >
			{#snippet children({ attrs })}
						<Form.Label>Deck Name</Form.Label>
				<Input {...attrs} bind:value={$formData.name} type="text" />
								{/snippet}
				</Form.Control>
		<Form.FieldErrors />
	</Form.Field>

	<Form.Fieldset {form} name="visibility">
		<Form.Legend>Visibility</Form.Legend>
		<RadioGroup.Root bind:value={$formData.visibility} class="flex flex-row py-4">
			<RadioItem value="public" text="Public" />
			<RadioItem value="unlisted" text="Unlisted" />
			<RadioItem value="private" text="Private" />
			<RadioGroup.Input name="visibility" />
		</RadioGroup.Root>
	</Form.Fieldset>

	<Form.Fieldset {form} name="format">
		<Form.Legend>Format</Form.Legend>
		<RadioGroup.Root bind:value={$formData.format} class="flex flex-row">
			<RadioItem value="constructed" text="Constructed" />
			<RadioItem value="paragon" text="Paragon" />
			<RadioItem value="other" text="Other" />

			<RadioGroup.Input name="format" />
		</RadioGroup.Root>
	</Form.Fieldset>

	<input name="id" value={id} aria-hidden="true" class="visually-hidden hidden" />

	<div class="mt-4">
		<Form.Button variant="secondary" href=".">Cancel</Form.Button>
		<Form.Button>Submit</Form.Button>
	</div>
</form>

<style lang="postcss">
</style>
