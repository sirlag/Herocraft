<script lang="ts">/** @type {{children?: import('svelte').Snippet}} */
	import SignInForm from './signin/signin-form.svelte';


let { children, title, alt = null, altDestination = null} = $props()
</script>

<div class="mx-auto ">
	<div class="shadow-lg p-10 rounded-lg border border-gray-200">
		<div class="prose prose-h2 mb-4 text-center">
			<h1>{title}</h1>
		</div>
		<div class="justify-center">
			{@render children()}
		</div>
		{#if alt && altDestination}
			<div class="flex justify-end pt-4">
				<div><a href="{altDestination}">{alt}</a></div>
			</div>
		{/if}
	</div>
</div>