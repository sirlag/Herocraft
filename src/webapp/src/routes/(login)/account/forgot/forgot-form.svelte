<script lang="ts">
	import * as Form from "$lib/components/ui/form";
	import * as Alert from "$lib/components/ui/alert";
	import { Input } from "$lib/components/ui/input";

	import { forgotSchema, type ForgotSchema } from './ForgotSchema.ts';
	import { type Infer, type SuperValidated, superForm } from 'sveltekit-superforms';
	import { zodClient } from 'sveltekit-superforms/adapters';
	import { CircleAlert } from 'lucide-svelte';

	interface Props {
		data: SuperValidated<Infer<ForgotSchema>>;
	}

	let { data }: Props = $props();

	const form = superForm(data, {
		validators: zodClient(forgotSchema)
	})

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
	<Form.Field { form } name="email">
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
	<div class="mt-4">
		<Form.Button>Reset Password</Form.Button>
	</div>
	<div class="mt-4">
		<a class="mx-8" href="/account/signin">Back to Sign In</a>
	</div>
</form>
