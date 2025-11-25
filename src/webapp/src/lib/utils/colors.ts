/**
 * Card color utilities for handling Ivion card colors and special types
 */

export const CARD_COLORS = {
	Red: '212,55,46',
	Green: '115,171,98',
	Blue: '89,176,217',
	Gray: '116,134,136',
	White: '231,230,225',
	Black: '0,0,0',
	'Relic Left': '254,182,22',
	'Relic Right': '248,152,41',
	Trap: '244,152,165',
	Arrow: '76,58,84',
	None: '79,78,73'
} as const;

export type CardColor = keyof typeof CARD_COLORS;

/**
 * Determines special color for cards with special types (Relics, Traps)
 */
export function getSpecialColor(card: IvionCard): CardColor {
	if (card.format === 'Relic' || card.format === 'Relic Skill') return 'Relic Left';
	if (card.extraType === 'Trap') return 'Trap';
	return 'None';
}

/**
 * Gets the RGB color value for a given color name
 */
export function getColorValue(color: string): string {
	return CARD_COLORS[color as CardColor] ?? CARD_COLORS.None;
}

/**
 * Builds CSS custom properties string for card gradient shadows
 * Returns format: "--color-1:r,g,b;--color-2:r,g,b"
 */
export function buildColorStyle(card: IvionCard): string {
	const color1 = card.colorPip1 ?? getSpecialColor(card);
	const color2 = card.colorPip2 ?? (color1 === 'Relic Left' ? 'Relic Right' : color1);

	const c1 = getColorValue(color1);
	const c2 = getColorValue(color2);

	return `--color-1:${c1};--color-2:${c2}`;
}
