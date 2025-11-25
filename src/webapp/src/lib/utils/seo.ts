/**
 * SEO utilities for building card metadata
 */

import { pickPrimaryFace } from './card';

/**
 * Normalizes text by removing HTML tags, icon markup, and collapsing whitespace
 */
export function normalizeText(s?: string | null): string {
	if (!s) return '';
	return s
		.replace(/<[^>]*>/g, ' ')
		.replace(/[{}]/g, '')
		.replace(/\s+/g, ' ')
		.trim();
}

/**
 * Truncates text to a maximum length, adding ellipsis if needed
 */
export function truncate(s: string, max = 300): string {
	if (s.length <= max) return s;
	return s.slice(0, max - 1).trimEnd() + '…';
}

/**
 * Builds a rich description for a card including type, rules, costs, and artist
 */
export function buildCardDescription(card: IvionCard): string {
	const headerParts: string[] = [];
	if (card?.archetype) headerParts.push(card.archetype);
	if (card?.type) headerParts.push(card.type);
	if (card?.extraType) headerParts.push(card.extraType);

	const primary = pickPrimaryFace(card);
	const rulesText = normalizeText(primary?.rulesText ?? card?.rulesText ?? '');

	const costs: string[] = [];
	const action = primary?.actionCost ?? card?.actionCost;
	const power = primary?.powerCost ?? card?.powerCost;
	const range = primary?.range ?? card?.range;
	if (action !== undefined && action !== null) costs.push(`Action ${action}`);
	if (power !== undefined && power !== null) costs.push(`Power ${power}`);
	if (range !== undefined && range !== null) costs.push(`Range ${range}`);

	const artist = primary?.artist ?? card?.artist ?? null;

	const segments: string[] = [];
	if (headerParts.length) segments.push(headerParts.join(' – '));
	if (rulesText) segments.push(rulesText);
	if (costs.length) segments.push(costs.join(' • '));
	if (artist) segments.push(`Artist: ${artist}`);

	return truncate(segments.length ? segments.join(' | ') : 'An Ivion card on Herocraft');
}

/**
 * Builds a rich title for a card page (e.g., "Card Name · Archetype Type · Herocraft")
 */
function buildCardTitle(card: IvionCard): string {
	if (!card?.name) return 'Herocraft';

	const parts = [card.name];

	// Add archetype and type if available
	if (card.archetype || card.type) {
		const typeParts = [card.archetype, card.type].filter(Boolean);
		if (typeParts.length) parts.push(typeParts.join(' '));
	}

	parts.push('Herocraft');
	return parts.join(' · ');
}

/**
 * Determines image type from URL
 */
function getImageType(imageUrl: string | null): string | null {
	if (!imageUrl) return null;
	if (imageUrl.endsWith('.png')) return 'image/png';
	if (imageUrl.endsWith('.jpg') || imageUrl.endsWith('.jpeg')) return 'image/jpeg';
	return null;
}

/**
 * Builds complete SEO metadata for a card page
 */
export function buildCardSeoMeta(card: IvionCard, pageUrl: string, imageUrl: string | null) {
	return {
		title: buildCardTitle(card),
		description: buildCardDescription(card),
		image: imageUrl,
		// Omit dimensions - let Discord/social platforms detect them automatically
		imageType: getImageType(imageUrl),
		url: pageUrl,
		siteName: 'Herocraft',
		type: 'website'
	};
}
