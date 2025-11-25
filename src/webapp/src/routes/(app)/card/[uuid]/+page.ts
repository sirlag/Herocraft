import type { PageLoad } from './$types';
import { PUBLIC_API_BASE_URL } from '$env/static/public';

export const load: PageLoad = async ({ fetch, params, url }) => {
    let uuid = params['uuid'];

    const res = await fetch(`${PUBLIC_API_BASE_URL}/card/${uuid}`);
    const card = await res.json();

    // Fetch rulings using hypermedia link or herocraftId if available
    let rulings: any[] = [];
    try {
        // Prefer hypermedia link from card payload
        let rulingsPath: string | null = card?.rulings_uri ?? null;
        // Fallback to herocraftId if provided
        if (!rulingsPath && card?.herocraftId) rulingsPath = `/cards/${card.herocraftId}/rulings`;
        if (rulingsPath) {
            const r = await fetch(`${PUBLIC_API_BASE_URL}${rulingsPath}`);
            if (r.ok) rulings = await r.json();
        }
    } catch (e) {
        // ignore; rulings optional
    }

    // Provide absolute page URL for OG/Twitter meta tags
    const pageUrl = url?.href ?? `https://herocraft.app/card/${uuid}`;

    return { card, rulings, pageUrl };
};
