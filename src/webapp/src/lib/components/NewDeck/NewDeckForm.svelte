<script lang="ts">
	import * as Form from '$lib/components/ui/form'
	import * as RadioGroup from '$lib/components/ui/radio-group';
	import { Footer } from '$lib/components/ui/dialog';
	import { Input } from "$lib/components/ui/input"
	import { formSchema, type FormSchema} from './schema.ts'
	import RadioItem from '$lib/components/RadioItem.svelte'

	import SuperDebug, {
		type SuperValidated,
		type Infer,
		superForm,
	} from 'sveltekit-superforms'
	import { zodClient } from 'sveltekit-superforms/adapters'
	import { Button } from '$lib/components/ui/button';

	export let data: SuperValidated<Infer<FormSchema>>;

	const form = superForm(data, {
		validators: zodClient(formSchema),
	})

	const { form: formData, enhance } = form;

</script>

<form method="POST" use:enhance action="/decks/new">
	<Form.Field {form} name="name">
		<Form.Control let:attrs>
			<Form.Label>Deck Name</Form.Label>
			<Input {...attrs} bind:value={$formData.name} type="text" />
		</Form.Control>
		<Form.FieldErrors />
	</Form.Field>

	<Form.Fieldset {form} name="visibility">
		<Form.Legend>Visibility</Form.Legend>
		<RadioGroup.Root
			bind:value={$formData.visibility}
			class="flex flex-row py-4"
		>
			<RadioItem value="public" text="Public" />
			<RadioItem value="unlisted" text="Unlisted" />
			<RadioItem value="private" text="Private" />
		</RadioGroup.Root>
	</Form.Fieldset>

	<Form.Fieldset {form} name="format">
		<Form.Legend>Format</Form.Legend>
		<RadioGroup.Root
			bind:value={$formData.format}
			class="flex flex-row"
		>
			<RadioItem value="constructed" text="Constructed" />
			<RadioItem value="paragon" text="Paragon" />
			<RadioItem value="other" text="Other" />
		</RadioGroup.Root>
	</Form.Fieldset>

<!--	<SuperDebug data="{form}"/>-->

	<Footer>
		<Button type="submit">Create</Button>
	</Footer>

</form>

<style lang="postcss">

</style>