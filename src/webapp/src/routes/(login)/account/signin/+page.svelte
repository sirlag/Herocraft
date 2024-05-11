<script lang="ts">
	import SignInForm from './signin-form.svelte';

	import type { PageData } from './$types';
	export let data: PageData;
</script>

<div class="flex flex-col">
	<div class="flex justify-end">
		<div>Don't have an account? <a href="/account/register">Sign up now</a></div>
	</div>
	<div class="justify-center">
		<SignInForm data={data.form} />
	</div>
</div>
