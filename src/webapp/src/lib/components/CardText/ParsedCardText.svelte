<script lang="ts">

	import charge from '$lib/images/IvionIcons/Charge.png';
	import IvionIcon from '$lib/components/IvionIcon.svelte';
	import Parser from '$lib/components/CardText/Parser.svelte';
	import { Lexer, defaultRenderers } from './card-parser';
	import { onMount } from 'svelte';

	interface Props {
		source?: string | Array<string>;
		renderers?: any;
		options?: any;
		isInline?: boolean;
	}

	let {
		source = [],
		renderers = {},
		options = {},
		isInline = false
	}: Props = $props();


	let tokens = $state();
	let lexer = $state();
	let mounted;

	let preprocessed = $derived(Array.isArray(source));

	onMount(() => {
		if (preprocessed) {
			tokens = source;
		} else {
			lexer = new Lexer();

			let str = source as string;
			str = str
				.replaceAll(/[{}]/g, '**')
				.replaceAll(/[<>]/g, '*')
				.replaceAll(/\[(\w+)]/g, '<$1>');

			//@ts-ignore
			tokens = isInline ? lexer.inlineTokens(str) : lexer.lex(str);

			$inspect(source).with(console.log);
			$inspect(tokens).with(console.log);
		}
	})

	// run(() => {

	// });

	let combinedRenderers = $derived({ ...defaultRenderers, ...renderers });

</script>

<Parser {tokens} renderers={combinedRenderers} />
