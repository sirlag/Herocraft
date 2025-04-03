<script lang="ts">
	import { run } from 'svelte/legacy';

	import charge from '$lib/images/IvionIcons/Charge.png';
	import IvionIcon from '$lib/components/IvionIcon.svelte';
	import Parser from '$lib/components/CardText/Parser.svelte';
	import { createEventDispatcher, setContext } from 'svelte';
	import { Lexer, defaultRenderers } from './card-parser';

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

	const dispatch = createEventDispatcher();

	let tokens = $state();
	let lexer = $state();
	let mounted;

	let preprocessed = $derived(Array.isArray(source));
	run(() => {
		if (preprocessed) {
			tokens = source;
		} else {
			lexer = new Lexer();

			let str = source as string;
			str = str
				.replaceAll(/[{}]/g, '**')
				.replaceAll(/[<>]/g, '*')
				.replaceAll(/\[(\w+)]/g, '<$1>');

			tokens = isInline ? lexer.inlineTokens(str) : lexer.lex(str);

			console.log(source);
			console.log(tokens);

			dispatch('parsed', { tokens });
		}
	});

	let combindedRenderers = $derived({ ...defaultRenderers, ...renderers });

	run(() => {
		mounted && !preprocessed && dispatch('parsed', { tokens });
	});
</script>

<Parser {tokens} renderers={combindedRenderers} />
