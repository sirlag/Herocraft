<!--<script src="+page.server.ts"></script>-->
<script lang="ts">
	import SignInForm from './signin-form.svelte';

	import type { PageData } from './$types';
	import LoginWidget from '../LoginWidget.svelte';

	interface Props {
		data: PageData;
	}

	let { data }: Props = $props();

</script>

<LoginWidget
	title="Sign in to Herocraft"
	alt="Don't have an account? Register now"
	altDestination="/account/register">
	<SignInForm data={data.form} />
</LoginWidget>


