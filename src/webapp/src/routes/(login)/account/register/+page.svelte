<script lang="ts">
	import RegisterForm from './register-form.svelte';

	import type { PageData } from './$types';
	export let data: PageData;
</script>

<div class="flex flex-col h-full">
	<div class="flex justify-end">
		<div>Already have an account? <a href="/account/signin">Sign in</a></div>
	</div>
	<div class="justify-center items-center h-full">
		<RegisterForm data={data.form} />
	</div>
</div>
