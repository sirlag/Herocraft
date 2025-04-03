<script lang="ts">
	import { createBubbler } from 'svelte/legacy';

	const bubble = createBubbler();
	import type { HTMLInputAttributes } from 'svelte/elements';
	import type { InputEvents } from './index.js';
	import { cn } from '$lib/utils.js';
	import { Button } from '$lib/components/ui/button';
	import { Search } from 'lucide-svelte';

	type $$Props = HTMLInputAttributes;
	type $$Events = InputEvents;

	

	// Workaround for https://github.com/sveltejs/svelte/issues/9305
	// Fixed in Svelte 5, but not backported to 4.x.
	interface Props {
		class?: $$Props['class'];
		value?: $$Props['value'];
		readonly?: $$Props['readonly'];
		[key: string]: any
	}

	let { class: className = undefined, value = $bindable(undefined), readonly = undefined, ...rest }: Props = $props();
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
		onblur={bubble('blur')}
		onchange={bubble('change')}
		onclick={bubble('click')}
		onfocus={bubble('focus')}
		onfocusin={bubble('focusin')}
		onfocusout={bubble('focusout')}
		onkeydown={bubble('keydown')}
		onkeypress={bubble('keypress')}
		onkeyup={bubble('keyup')}
		onmouseover={bubble('mouseover')}
		onmouseenter={bubble('mouseenter')}
		onmouseleave={bubble('mouseleave')}
		onpaste={bubble('paste')}
		oninput={bubble('input')}
		onwheel={bubble('wheel')}
		{...rest}
	/>
	<Button variant="ghost" size="icon" class="p-2" type="submit"><Search /></Button>
</div>
