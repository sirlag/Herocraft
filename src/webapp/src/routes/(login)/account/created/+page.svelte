<script type="ts">

	import LoginWidget from '../LoginWidget.svelte'
	import { CircleCheckBig } from 'lucide-svelte';
</script>

<LoginWidget
		title="Thank You For Registering"
		>
	<div class="text-center">
		<div class="flex justify-center m-2">
			<CircleCheckBig size={120} />
		</div>
		<div class="prose py-4">
			Your account has been created and a verification email has been sent to your registered email address. Please click on the verification link included in the email to activate your account.
		</div>
		<div class="pt-4 pb-4">
			<a href="/account/signin">Return to Sign In</a>
		</div>
	</div>

</LoginWidget>