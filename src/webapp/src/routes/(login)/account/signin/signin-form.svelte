<script lang="ts">
	import * as Form from '$lib/components/ui/form';
	import * as Alert from "$lib/components/ui/alert";
	import { Input, PasswordInput } from '$lib/components/ui/input';
	import { formSchema, type FormSchema } from './schema';

	import { type SuperValidated, type Infer, superForm } from 'sveltekit-superforms';
	import { zodClient } from 'sveltekit-superforms/adapters';
	import { CircleAlert } from 'lucide-svelte';

	interface Props {
		data: SuperValidated<Infer<FormSchema>>;
	}

	let { data }: Props = $props();

	const form = superForm(data, {
		validators: zodClient(formSchema)
	});

	const { form: formData, enhance, message } = form;
</script>

<form method="POST" use:enhance>
	{#if $message}
		<Alert.Root variant="destructive">
			<CircleAlert class="size-4" />
			<Alert.Title>Error</Alert.Title>
			<Alert.Description>
				{$message}
			</Alert.Description>
		</Alert.Root>
	{/if}
	<Form.Field {form} name="email">
		<Form.Control>
			{#snippet children({ props })}
				<Form.Label>Email</Form.Label>
				<Input
					{...props}
					bind:value={$formData.email}
					type="email"
					placeholder="example@domain.com"
				/>
			{/snippet}
		</Form.Control>
		<Form.FieldErrors />
	</Form.Field>
	<Form.Field {form} name="password" class="mb-4">
		<Form.Control>
			{#snippet children({ props })}
				<PasswordInput {...props} bind:value={$formData.password} />
			{/snippet}
		</Form.Control>
		<Form.FieldErrors />
	</Form.Field>
	<div>
		<Form.Button>Login</Form.Button>
		<a class="mx-8" href="/account/forgot">Forget password?</a>
	</div>
</form>

<style>
</style>