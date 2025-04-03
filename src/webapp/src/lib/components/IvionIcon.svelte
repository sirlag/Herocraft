<script lang="ts">
	import action from '$lib/images/IvionIcons/Action.png';
	import charge from '$lib/images/IvionIcons/Charge.png';
	import chivalrous from '$lib/images/IvionIcons/Chivalrous.png';
	import difficultTerrain from '$lib/images/IvionIcons/DifficultTerrain.png';
	import disarm from '$lib/images/IvionIcons/DisarmSquare.png';
	import duration from '$lib/images/IvionIcons/Duration.png';
	import enchantedTerrain from '$lib/images/IvionIcons/EnchantedTerrain.png';
	import initiative from '$lib/images/IvionIcons/Initiative.png';
	import luck from '$lib/images/IvionIcons/Clover.png';
	import mitigate from '$lib/images/IvionIcons/Mitigate.png';
	import poison from '$lib/images/IvionIcons/Poison.png';
	import power from '$lib/images/IvionIcons/Power.png';
	import silence from '$lib/images/IvionIcons/SilenceSquare.png';
	import slow from '$lib/images/IvionIcons/SlowSquare.png';
	import trap from '$lib/images/IvionIcons/Trap.png';

	interface Props {
		icon: string;
	}

	let { icon }: Props = $props();

	class ImageSpec {
		image: {} = '';
		alt: string = '';
	}

	let ImageMap: Record<string, ImageSpec> = {
		a: {
			image: action,
			alt: 'action'
		},
		charge: {
			image: charge,
			alt: 'charge'
		},
		c: {
			image: chivalrous,
			alt: 'chivalrous'
		},
		d: {
			image: disarm,
			alt: 'disarm'
		},
		dt: {
			image: difficultTerrain,
			alt: 'difficult terrain'
		},
		du: {
			image: duration,
			alt: 'duration'
		},
		et: {
			image: enchantedTerrain,
			alt: 'enchanted terrain'
		},
		i: {
			image: silence,
			alt: 'silence'
		},
		lu: {
			image: luck,
			alt: 'luck'
		},
		mi: {
			image: mitigate,
			alt: 'mitigation'
		},
		n: {
			image: initiative,
			alt: 'initiative'
		},
		o: {
			image: slow,
			alt: 'slow'
		},
		p: {
			image: power,
			alt: 'power'
		},
		pi: {
			image: poison,
			alt: 'posion'
		},
		t: {
			image: trap,
			alt: 'trap'
		}
	};
</script>

{#if ImageMap.hasOwnProperty(icon)}
	<img class="w-4 inline-block" src={ImageMap[icon].image} alt={ImageMap[icon].alt} />
{:else}
	"UNKNOWN IMAGE TAG - "{icon}"
{/if}
