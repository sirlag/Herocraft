<script lang="ts">
	import * as Form from '$lib/components/ui/form';
	import * as RadioGroup from '$lib/components/ui/radio-group';
	import { Input } from '$lib/components/ui/input';
	import { formSchema, type FormSchema } from './schema.ts';
	import RadioItem from '$lib/components/RadioItem.svelte';

	import SuperDebug, { type SuperValidated, type Infer, superForm } from 'sveltekit-superforms';
	import { zodClient } from 'sveltekit-superforms/adapters';

	interface Props {
		data: SuperValidated<Infer<FormSchema>>;
		handleSubmit: () => void;
	}

	let { data, handleSubmit }: Props = $props();

	const form = superForm(data, {
		validators: zodClient(formSchema),
		onSubmit: () => {
			handleSubmit();
		}
	});

	const { form: formData, enhance } = form;
</script>

<form id="deckForm" method="POST" action="/decks/new" use:enhance>
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
</form>

<style lang="postcss">
</style>
