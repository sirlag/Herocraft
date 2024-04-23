<script lang="ts">
	import charge from '$lib/images/IvionIcons/Charge.png'
	import IvionIcon from '$lib/components/IvionIcon.svelte'
	import Parser from '$lib/components/CardText/Parser.svelte';
	import { createEventDispatcher, setContext } from 'svelte';
	import { Lexer, defaultRenderers } from './card-parser';

	export let source: (string | Array<string>) = []
	export let renderers = {}
	export let options = {}
	export let isInline = false

	const dispatch = createEventDispatcher();

	let tokens;
	let lexer;
	let mounted;

	$: preprocessed = Array.isArray(source)
	$: if (preprocessed) {
		tokens = source
	} else {
		lexer = new Lexer()

		let str = source as string;
		str = str.replaceAll(/[{}]/g, "**")
			.replaceAll(/[<>]/g, "*")
			.replaceAll(/\[(\w+)]/g, "<$1>")

		tokens = isInline ? lexer.inlineTokens(str) : lexer.lex(str)

		console.log(source)
		console.log(tokens)

		dispatch('parsed', {tokens})
	}

	$: combindedRenderers = { ...defaultRenderers, ...renderers }

	$: mounted && !preprocessed && dispatch('parsed', { tokens })

</script>

<Parser {tokens} renderers={combindedRenderers} />