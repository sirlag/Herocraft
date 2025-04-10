<script lang="ts">
	import LoginWidget from '../../LoginWidget.svelte'
	import { MailCheck } from 'lucide-svelte';
</script>

<LoginWidget
	title="Password Reset Sent"
	>
	<div class="text-center">
		<div class="flex justify-center m-2">
			<MailCheck size={120} />
		</div>
		<div class="prose py-4">
			A email has been sent containing a link to reset your password.
			If you haven't received the password reset email, please check your spam folder. You can also retry after 10 minutes.
		</div>
		<div class="pt-4 pb-4">
			<a href="/account/signin">Return to Sign In</a>
		</div>
	</div>

</LoginWidget>