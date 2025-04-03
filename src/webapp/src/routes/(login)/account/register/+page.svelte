<script lang="ts">
	import RegisterForm from './register-form.svelte';

	import type { PageData } from './$types';
	import LoginWidget from '../LoginWidget.svelte';
	interface Props {
		data: PageData;
	}

	let { data }: Props = $props();
</script>

<LoginWidget
	title="Register for Herocraft"
	alt="Already have an Account? Sign in"
	altDestination="/account/signin">
	<RegisterForm data={data.form} />
</LoginWidget>