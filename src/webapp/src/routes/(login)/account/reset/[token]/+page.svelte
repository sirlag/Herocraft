<script lang="ts">
	import LoginWidget from '../../LoginWidget.svelte';

	import type { PageData } from './$types';

	import PasswordResetForm from './password-reset-form.svelte'

	interface Props {
		data: PageData;
	}

	let { data }: Props = $props();
</script>

<LoginWidget
	title="Reset Your Password"
>
	<PasswordResetForm data={data.form} />
</LoginWidget>
