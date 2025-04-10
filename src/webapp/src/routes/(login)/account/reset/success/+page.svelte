<script lang="ts">
	import LoginWidget from '../../LoginWidget.svelte';
	import { CircleCheckBig } from 'lucide-svelte';
</script>

<LoginWidget
	title="Your password has been reset."
>
	<div class="text-center">
		<div class="flex justify-center m-2">
			<CircleCheckBig size={120} />
		</div>
		<div class="prose py-4">
			You have successfully reset your password. You can now sign in to your account.
		</div>
		<div class="pt-4 pb-4">
			<a href="/account/signin">Return to Sign In</a>
		</div>
	</div>
</LoginWidget>