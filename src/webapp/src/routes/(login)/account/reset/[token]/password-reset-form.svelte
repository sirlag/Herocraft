<script lang="ts">
	import * as Form from '$lib/components/ui/form';
	import { PasswordInput } from '$lib/components/ui/input';
	import { type Infer, superForm, type SuperValidated } from 'sveltekit-superforms';
	import { resetPasswordSchema, type ResetPasswordSchema } from './PasswordResetSchema.ts';
	import { zodClient } from 'sveltekit-superforms/adapters';


	interface Props {
		data: SuperValidated<Infer<ResetPasswordSchema>>;
	}

	let { data }: Props = $props();

	const form = superForm(data, {
		validators: zodClient(resetPasswordSchema)
	});

	const { form: formData, enhance } = form;
</script>

<form method="POST" use:enhance>
	<Form.Field {form} name="password">
		<Form.Control>
			{#snippet children({ props })}
				<PasswordInput {...props} bind:value={$formData.password} label="New password" />
			{/snippet}
		</Form.Control>
		<Form.FieldErrors />
	</Form.Field>
	<Form.Field {form} name="confirmPassword">
		<Form.Control>
			{#snippet children({ props })}
				<PasswordInput {...props} bind:value={$formData.confirmPassword} label="Confirm new password" />
			{/snippet}
		</Form.Control>
		<Form.FieldErrors />
	</Form.Field>
	<div class="mt-4">
		<Form.Button>Reset Password</Form.Button>
	</div>
</form>