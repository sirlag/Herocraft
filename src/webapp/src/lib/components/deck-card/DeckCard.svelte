<script lang="ts">
	import type { Deck } from '$app';
	import { Badge } from '$lib/components/ui/badge';
	import { HeroImage } from '$lib/components/image/hero-image';
	import Time from 'svelte-time';

	interface Props {
		deck: Deck;
	}

	let { deck }: Props = $props();

	// Format the deck format for display
	function formatDeckFormat(format: string): string {
		return format.charAt(0).toUpperCase() + format.slice(1).toLowerCase();
	}

	// Map color names to RGB values
	function getColorValue(color: string): string {
		switch (color) {
			case 'Red':
				return '212,55,46';
			case 'Green':
				return '115,171,98';
			case 'Blue':
				return '89,176,217';
			case 'White':
				return '231,230,225';
			case 'Black':
				return '0,0,0';
			case 'Gray':
			default:
				return '116,134,136';
		}
	}

	// Map specializations to their color(s) based on actual ultimate card data
	function getSpecializationColors(spec: string): { primary: string; secondary?: string } {
		switch (spec) {
			// Single color specializations
			case 'Saint':
				return { primary: 'White' };
			case 'Invoker':
				return { primary: 'Red' };
			case 'Archmage':
				return { primary: 'Blue' };
			case 'Incarnate':
				return { primary: 'White' };
			case 'Ebon Mage':
				return { primary: 'Black' };
			case 'Ancient':
				return { primary: 'Red' };
			case 'Winterborn':
				return { primary: 'Blue' };
			case 'Wilder':
				return { primary: 'Green' };
			case 'Fletcher':
				return { primary: 'Green' };
			case 'Ruffian':
				return { primary: 'Black' };
			// Dual color specializations
			case 'Executioner':
				return { primary: 'Black', secondary: 'Gray' };
			case 'Illusionist':
				return { primary: 'Blue', secondary: 'Green' };
			case 'Curseblade':
				return { primary: 'Black', secondary: 'Red' };
			case 'Enchantress':
				return { primary: 'White', secondary: 'Red' };
			case 'Errant':
				return { primary: 'White', secondary: 'Gray' };
			case 'Jarl':
				return { primary: 'Red', secondary: 'Gray' };
			case 'Survivalist':
				return { primary: 'Gray', secondary: 'Green' };
			case 'Poacher':
				return { primary: 'Red', secondary: 'Green' };
			case 'Watcher':
				return { primary: 'Blue', secondary: 'Gray' };
			case 'Huntsman':
				return { primary: 'Green', secondary: 'Black' };
			case 'Avatar':
				return { primary: 'White', secondary: 'Blue' };
			case 'Baron':
				return { primary: 'Black', secondary: 'Blue' };
			// Gray primary specializations
			case 'Friar':
				return { primary: 'Gray' };
			case 'Steward':
				return { primary: 'Gray' };
			// Default fallback
			default:
				return { primary: 'Gray' };
		}
	}

	// Get colors for a specialization (returns both primary and secondary for dual-color shadows)
	function getSpecializationColorString(spec: string): { c1: string; c2: string } {
		const colors = getSpecializationColors(spec);
		const primaryColor = getColorValue(colors.primary);
		
		if (colors.secondary) {
			const secondaryColor = getColorValue(colors.secondary);
			return { c1: primaryColor, c2: secondaryColor };
		}
		
		// For single colors, use the same color for both shadow layers
		return { c1: primaryColor, c2: primaryColor };
	}

	let { c1, c2 } = $derived(getSpecializationColorString(deck.primarySpec));
	let colorStyle = $derived(`--color-1:${c1};--color-2:${c2}`);
</script>

<a 
	href="/deck/{deck.hash}" 
	class="block bg-white rounded-lg overflow-hidden hover:shadow-lg transition-shadow duration-200 cursor-pointer deck-card-shadow"
	style={colorStyle}
>
	<!-- Hero Image Background with Overlaid Content -->
	<div class="h-48 bg-gray-200 relative overflow-hidden">
		{#if deck.primarySpec}
			<HeroImage spec={deck.primarySpec} />
		{:else}
			<div class="absolute inset-0 flex items-center justify-center">
				<div class="text-gray-400 text-4xl">🃏</div>
			</div>
		{/if}
		<!-- Overlay for better text readability -->
		<div class="absolute inset-0 bg-black bg-opacity-40"></div>
		
		<!-- Overlaid Content -->
		<div class="absolute inset-0 p-4 flex flex-col justify-between text-white">
			<!-- Top Section: Deck Title and Format -->
			<div>
				<h3 class="font-semibold text-lg mb-2 line-clamp-2 text-white drop-shadow-lg" title={deck.name}>
					{deck.name}
				</h3>
				<div class="mb-2">
					<Badge variant="secondary" class="bg-white/90 text-gray-900 hover:bg-white/80">
						{formatDeckFormat(deck.format)}
					</Badge>
				</div>
			</div>
			
			<!-- Bottom Section: Stats and Info -->
			<div class="space-y-2">
				<!-- Stats Row -->
				<div class="flex items-center space-x-4 text-sm">
					<!-- Likes -->
					<div class="flex items-center space-x-1">
						<svg class="w-4 h-4 text-red-400" fill="currentColor" viewBox="0 0 20 20">
							<path fill-rule="evenodd" d="M3.172 5.172a4 4 0 015.656 0L10 6.343l1.172-1.171a4 4 0 115.656 5.656L10 17.657l-6.828-6.829a4 4 0 010-5.656z" clip-rule="evenodd"></path>
						</svg>
						<span class="drop-shadow-sm">{deck.likes}</span>
					</div>
					
					<!-- Views -->
					<div class="flex items-center space-x-1">
						<svg class="w-4 h-4 text-gray-200" fill="none" stroke="currentColor" viewBox="0 0 24 24">
							<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
							<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
						</svg>
						<span class="drop-shadow-sm">{deck.views}</span>
					</div>
				</div>
				
				<!-- Username and Last Updated Row -->
				<div class="text-xs text-gray-200 space-y-1">
					{#if deck.ownerName}
						<div class="drop-shadow-sm">by {deck.ownerName}</div>
					{/if}
					<div class="drop-shadow-sm">
						<Time timestamp={deck.lastModified} relative={true} />
					</div>
				</div>
			</div>
		</div>
	</div>
</a>

<style>
	.line-clamp-2 {
		display: -webkit-box;
		-webkit-line-clamp: 2;
		-webkit-box-orient: vertical;
		overflow: hidden;
	}

	.deck-card-shadow {
		box-shadow: 0px 1px 3px 0px rgba(0, 0, 0, 0.1),
		0px 1px 2px -1px rgba(0, 0, 0, 0.1);
	}

	.deck-card-shadow:hover {
		box-shadow: 0px 1px 3px 0px rgba(0, 0, 0, 0.1),
		0px 1px 2px -1px rgba(0, 0, 0, 0.1),
		-4px 6px 12px 1px rgba(var(--color-1), 0.6),
		4px 8px 15px 1px rgba(var(--color-2), 0.5);
	}
</style>