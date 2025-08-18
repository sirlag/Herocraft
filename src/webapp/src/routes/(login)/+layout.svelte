<script lang="ts">
	import '../../app.css';
	import Herocraft from '$lib/images/herocraft.svelte';

	/** @type {{children?: import('svelte').Snippet}} */
	let { children } = $props();
	let year = new Date().getFullYear();
</script>

<div>
	<main class="flex flex-row min-h-lvh w-lvw">
		<div class="hidden lg:flex flex-col justify-between sidebar-background min-w-96 p-4">
			<div class="text-gray-50">
				<a href="/">
					<Herocraft />
				</a>
			</div>
			<div class="text-sm text-gray-50">
				© {year} Herocraft ● <a href="/about/terms">Terms of Service</a> ● <a href="/about/privacy">Privacy Policy</a>
			</div>
		</div>
		<div class="grid place-items-center h-lvh w-lvw border-gray-100">
			<!--		<div class="flex flex-col grow justify-center align-middle h-lvh bg-gray-100">-->
			<div>
				<div class="lg:hidden flex justify-center py-8">
					<Herocraft />
				</div>
				{@render children?.()}

			</div>
			<div class="block text-sm lg:hidden text-red-700 justify-end">
				<p>
					© {year} Herocraft ● <a href="/about/terms">Terms of Service</a> ● <a href="/about/privacy">Privacy Policy</a>
				</p>
			</div>
		</div>
	</main>
</div>

<style>
    .sidebar-background {
        background: rgb(105, 43, 214);
        background: linear-gradient(166deg, rgba(105, 43, 214, 1) 0%, rgba(151, 81, 164, 1) 66%, rgba(250, 171, 73, 1) 100%);
    }

    :global(svg) {
        max-height: 60px;
    }
</style>