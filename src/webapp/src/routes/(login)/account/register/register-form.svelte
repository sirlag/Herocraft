<script lang="ts">
	import * as Form from '$lib/components/ui/form';
	import { Input } from '$lib/components/ui/input';
	import { formSchema, type FormSchema } from './schema.ts';

	import { type SuperValidated, type Infer, superForm } from 'sveltekit-superforms';
	import { zodClient } from 'sveltekit-superforms/adapters';

	interface Props {
		data: SuperValidated<Infer<FormSchema>>;
	}

	let { data }: Props = $props();

	const form = superForm(data, {
		validators: zodClient(formSchema)
	});

	const { form: formData, enhance } = form;
</script>

<form method="POST" use:enhance>
	<Form.Field {form} name="username">
		<Form.Control >
			{#snippet children({ props })}
						<Form.Label>Username</Form.Label>
				<Input {...props} bind:value={$formData.username} type="text" />
								{/snippet}
				</Form.Control>
		<Form.FieldErrors />
	</Form.Field>
	<Form.Field {form} name="email">
		<Form.Control >
			{#snippet children({ props })}
						<Form.Label>Email</Form.Label>
				<Input {...props} bind:value={$formData.email} type="text" />
								{/snippet}
				</Form.Control>
		<Form.FieldErrors />
	</Form.Field>
	<Form.Field {form} name="password">
		<Form.Control >
			{#snippet children({ props })}
						<Form.Label>Password</Form.Label>
				<Input {...props} bind:value={$formData.password} type="password" />
								{/snippet}
				</Form.Control>
		<Form.FieldErrors />
	</Form.Field>
	<Form.Field {form} name="confirmPassword" class="mb-4">
		<Form.Control >
			{#snippet children({ props })}
						<Form.Label>Confirm Password</Form.Label>
				<Input {...props} bind:value={$formData.confirmPassword} type="password" />
								{/snippet}
				</Form.Control>
		<Form.FieldErrors />
	</Form.Field>
	<Form.Button>Register</Form.Button>
</form>
