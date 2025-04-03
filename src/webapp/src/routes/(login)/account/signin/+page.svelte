<script lang="ts">
	import SignInForm from './signin-form.svelte';

	import type { PageData } from './$types';
	import Herocraft from '$lib/images/herocraft.svelte';
	interface Props {
		data: PageData;
	}

	let { data }: Props = $props();
</script>

<div class="flex flex-row min-h-lvh w-lvw">
	<div class="pagebackgound min-w-96 p-4">
		<div class="text-white">
			<a href="/">
				<Herocraft/>
			</a>
		</div>
	</div>
	<div class="flex flex-col justify-center align-middle w-lvw bg-gray-100">
		<div class="mx-auto">
			<div class="shadow-lg p-10 rounded-lg border border-gray-200">
				<div class="prose prose-h2 mb-4">
					<h1>Sign in to Herocraft</h1>
				</div>
				<div class="justify-center">
					<SignInForm data={data.form} />
				</div>
				<div class="flex justify-end pt-4">
					<div>Don't have an account? <a href="/account/register">Register now</a></div>
				</div>
			</div>
		</div>

	</div>
</div>


<style>
    .pagebackgound {
        background: rgb(105,43,214);
        background: linear-gradient(166deg, rgba(105,43,214,1) 0%, rgba(151,81,164,1) 66%, rgba(250,171,73,1) 100%);
    }

		:global(svg) {
				max-height: 60px;
		}
</style>