import type { PageLoad } from './$types';
import { PUBLIC_API_BASE_URL } from '$env/static/public';

export const load: PageLoad = async ({ fetch, params }) => {
    let uuid = params['uuid'];

    const res = await fetch(`${PUBLIC_API_BASE_URL}/card/${uuid}`);
    const card = await res.json();

    // Fetch rulings from the new endpoint (uses internal [uuid])
    let rulings: any[] = [];
    try {
        const r = await fetch(`${PUBLIC_API_BASE_URL}/cards/${uuid}/rulings`);
        if (r.ok) rulings = await r.json();
    } catch (e) {
        // ignore; rulings optional
    }

    return { card, rulings };
};
