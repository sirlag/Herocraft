<script lang="ts">
	import type { HTMLInputAttributes } from 'svelte/elements';
	import type { InputEvents } from './index.js';
	import { cn } from '$lib/utils.js';
	import { Button } from '$lib/components/ui/button';
	import { Search } from 'lucide-svelte';

	type $$Props = HTMLInputAttributes;
	type $$Events = InputEvents;

	let className: $$Props['class'] = undefined;
	export let value: $$Props['value'] = undefined;
	export { className as class };

	// Workaround for https://github.com/sveltejs/svelte/issues/9305
	// Fixed in Svelte 5, but not backported to 4.x.
	export let readonly: $$Props['readonly'] = undefined;
</script>

<div
	class={cn(
		'flex justify-end h-10 rounded-md border border-input bg-background text-sm focus-within:ring-ring focus-within:ring-2 focus-within:ring-offset-2 ring-offset ring-offset-background',
		className
	)}
>
	<input
		class="w-full px-3 py-2 rounded-md placeholder:text-muted-foreground disabled:cursor-not-allowed disabled:opacity-50 focus-visible:shadow-none focus-visible:outline-none"
		bind:value
		{readonly}
		on:blur
		on:change
		on:click
		on:focus
		on:focusin
		on:focusout
		on:keydown
		on:keypress
		on:keyup
		on:mouseover
		on:mouseenter
		on:mouseleave
		on:paste
		on:input
		on:wheel
		{...$$restProps}
	/>
	<Button variant="ghost" size="icon" class="p-2" type="submit"><Search /></Button>
</div>
