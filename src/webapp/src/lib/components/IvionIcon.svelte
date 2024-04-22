<script lang="ts">
	import charge from '$lib/images/IvionIcons/Charge.png';

	export let icon: string;

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

	console.log(icon)
</script>

{#if ImageMap.hasOwnProperty(icon)}
	<img class="w-4 inline-block" src={ImageMap[icon].image} alt={ImageMap[icon].alt}/>
{:else}
	"UNKNOWN IMAGE TAG"
{/if}
