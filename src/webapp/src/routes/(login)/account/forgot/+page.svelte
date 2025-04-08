<script lang="ts">

import LoginWidget from '../LoginWidget.svelte';
import ForgotForm from './forgot-form.svelte';
import { LockKeyhole } from 'lucide-svelte';

import type { PageData } from './$types';

interface Props {
	data: PageData;
}

let { data }: Props = $props();

</script>

<LoginWidget
	title="Forgot Password?">
	<div class="text-center">
		<div class="flex justify-center m-2">
			<LockKeyhole size={120}/>
		</div>
		<div class="prose my-4">
			Enter your email and we'll send you a link to reset your password.
		</div>
	</div>
	<ForgotForm data={data.form} />
</LoginWidget>