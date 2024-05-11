<script lang="ts">
	import Enchantress from '$lib/images/specs/Enchantress.png';
	import Errant from '$lib/images/specs/Errant.png';
	import Executioner from '$lib/images/specs/Executioner.png';
	import Fletcher from '$lib/images/specs/Fletcher.png';
	import Friar from '$lib/images/specs/Friar.png';
	import Huntsman from '$lib/images/specs/Huntsman.png';
	import Incarnate from '$lib/images/specs/Incarnate.png';
	import Invoker from '$lib/images/specs/Invoker.png';
	import Jarl from '$lib/images/specs/Jarl.png';
	import Poacher from '$lib/images/specs/Poacher.png';
	import Ruffian from '$lib/images/specs/Ruffian.png';
	import Saint from '$lib/images/specs/Saint.png';
	import Steward from '$lib/images/specs/Steward.png';
	import Survivalist from '$lib/images/specs/Survivalist.png';
	import Watcher from '$lib/images/specs/Watcher.png';
	import Wilder from '$lib/images/specs/Wilder.png';
	import Winterborn from '$lib/images/specs/Winterborn.png';
	import Ancient from '$lib/images/specs/Ancient.png';
	import Archmage from '$lib/images/specs/Archmage.png';
	import Avatar from '$lib/images/specs/Avatar.png';
	import Baron from '$lib/images/specs/Baron.png';
	import Illusionist from '$lib/images/specs/Illusionist.png';
	import Curseblade from '$lib/images/specs/Curseblade.png';
	import EbonMage from '$lib/images/specs/Ebon Mage.png';

	export let spec;

	let getImage = (spec: string) => {
		switch (spec) {
			case 'Enchantress':
				return Enchantress;
			case 'Errant':
				return Errant;
			case 'Executioner':
				return Executioner;
			case 'Fletcher':
				return Fletcher;
			case 'Friar':
				return Friar;
			case 'Huntsman':
				return Huntsman;
			case 'Incarnate':
				return Incarnate;
			case 'Invoker':
				return Invoker;
			case 'Jarl':
				return Jarl;
			case 'Poacher':
				return Poacher;
			case 'Ruffian':
				return Ruffian;
			case 'Saint':
				return Saint;
			case 'Steward':
				return Steward;
			case 'Survivalist':
				return Survivalist;
			case 'Watcher':
				return Watcher;
			case 'Wilder':
				return Wilder;
			case 'Winterborn':
				return Winterborn;
			case 'Ancient':
				return Ancient;
			case 'Archmage':
				return Archmage;
			case 'Avatar':
				return Avatar;
			case 'Baron':
				return Baron;
			case 'Illusionist':
				return Illusionist;
			case 'Curseblade':
				return Curseblade;
			case 'Ebon Mage':
				return EbonMage;
			default:
				return undefined;
		}
	};

	let src = getImage(spec)!!;
</script>

<!--{#if src}-->
<img class="min-w-full min-h-full masked" {src} alt={spec} />

<!--{/if}-->

<style lang="postcss">
	.masked {
		mask-image: linear-gradient(to right, rgba(0, 0, 0, 0) 0%, rgb(0, 0, 0) 100%);
		overflow-x: clip;
		overflow-y: clip;
		overflow-clip-margin: content-box;
		object-fit: cover;
		object-position: 0% 0%;
	}
</style>
