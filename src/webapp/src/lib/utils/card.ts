/**
 * Card utility functions for working with Ivion card data
 */

/**
 * Normalizes a face value to either 'front', 'back', or null
 */
export function normalizeFace(face: CardFace | string | undefined | null): 'front' | 'back' | null {
	if (!face) return null;
	const v = face.toString().toLowerCase();
	return v === 'front' ? 'front' : v === 'back' ? 'back' : null;
}

/**
 * Gets the image URIs for a specific face of a transform card
 */
export function pickFaceUris(card: IvionCard, which: 'front' | 'back'): ImageUris | null {
	if (!card || card.layout !== 'TRANSFORM' || !Array.isArray(card.faces)) return null;
	const match = card.faces.find((f) => normalizeFace(f.face) === which);
	return match?.imageUris ?? null;
}

/**
 * Picks the large image from a set of image URIs for embeds/SEO
 * Prefers: large -> normal -> full -> small
 */
export function pickLargeImage(uris?: ImageUris | null): string | null {
	if (!uris) return null;
	return uris.large ?? uris.normal ?? uris.full ?? uris.small ?? null;
}

/**
 * Gets the primary (front) face of a transform card, or null for non-transform cards
 */
export function pickPrimaryFace(card: IvionCard): IvionCardFaceData | null {
	if (card.layout === 'TRANSFORM' && Array.isArray(card.faces)) {
		return card.faces.find((f) => normalizeFace(f.face) === 'front') ?? null;
	}
	return null;
}

/**
 * Gets a specific face from a card by key
 */
export function getFace(card: IvionCard, faceKey: 'front' | 'back'): IvionCardFaceData | null {
	if (!card || !card.faces) return null;
	return card.faces.find((f) => normalizeFace(f.face) === faceKey) ?? null;
}

/**
 * Checks if a card has multiple faces (transform layout)
 */
export function hasFaces(card: IvionCard): boolean {
	return card.layout === 'TRANSFORM';
}
