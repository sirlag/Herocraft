<script lang="ts">
	import charge from '$lib/images/IvionIcons/Charge.png'
	import IvionIcon from '$lib/components/IvionIcon.svelte'

	export let text: string;

	let className;
	export { className as class };

	class ImageSpec {
		image: {} = "";
		alt: string = ""

	}

	let ImageMap:Record<string, ImageSpec> = {
		"charge": {
			image: charge,
			alt: "charge"
		},
	}

	let processText = (): string => {
		if (text) {
			let formated = text
				.replaceAll("<", "@")
				.replaceAll(">", "#")
				.replaceAll("@", "<i>")
				.replaceAll("#", "</i>")
				.replaceAll("{", "<b>")
				.replaceAll("}", "</b>")

			let pt = formated.replace(
				/\B\[(\S+)]\B/g,
				(_, word) => { return `{<IvionIcon icon={word} />}`}
			);
			return pt
		}
		return ""

	}
	$: processedText =  processText()


</script>


<p class="{$$props.class || ''}">{@html processedText}</p>

<!--<IvionIcon icon="charge" />-->