<script lang="ts">
	import { createBubbler } from 'svelte/legacy';

	const bubble = createBubbler();
	import type { HTMLInputAttributes } from 'svelte/elements';
	import type { InputEvents } from './index.js';
	import { cn } from '$lib/utils.js';

	interface HTMLInputFileAttributes extends HTMLInputAttributes {
		files: FileList | undefined;
	}

	type $$Props = HTMLInputFileAttributes;
	type $$Events = InputEvents;

	

	// Workaround for https://github.com/sveltejs/svelte/issues/9305
	// Fixed in Svelte 5, but not backported to 4.x.
	interface Props {
		class?: $$Props['class'];
		value?: $$Props['value'];
		files?: $$Props['files'];
		readonly?: $$Props['readonly'];
		[key: string]: any
	}

	let {
		class: className = undefined,
		value = $bindable(undefined),
		files = $bindable(undefined),
		readonly = undefined,
		...rest
	}: Props = $props();
</script>

<input
	class={cn(
		'flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50',
		className
	)}
	bind:value
	bind:files
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
	type="file"
	{...rest}
/>
