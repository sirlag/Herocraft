import type { PageServerLoad } from './$types';
import { PUBLIC_API_BASE_URL } from '$env/static/public';
import { pickFaceUris, pickLargeImage } from '$lib/utils/card';
import { buildCardSeoMeta } from '$lib/utils/seo';

export const load: PageServerLoad = async ({ fetch, params, url }) => {
	const uuid = params['uuid'];

	// Fetch card data from API
	const res = await fetch(`${PUBLIC_API_BASE_URL}/card/${uuid}`);
	const card: IvionCard = await res.json();

	// Fetch rulings using hypermedia link or herocraftId if available
	let rulings: any[] = [];
	try {
		// Prefer hypermedia link from card payload
		const rulingsPath =
			card?.rulings_uri ?? (card?.herocraftId ? `/cards/${card.herocraftId}/rulings` : null);
		if (rulingsPath) {
			const r = await fetch(`${PUBLIC_API_BASE_URL}${rulingsPath}`);
			if (r.ok) rulings = await r.json();
		}
	} catch {
		// Rulings optional, ignore errors
	}

	// Build SEO metadata
	const frontUris = pickFaceUris(card, 'front');
	const image = pickLargeImage(frontUris);
	const seo = buildCardSeoMeta(card, url.href, image);

	return { card, rulings, pageUrl: url.href, seo };
};
