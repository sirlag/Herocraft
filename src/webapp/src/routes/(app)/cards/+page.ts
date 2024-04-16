import type { PageLoad } from "./$types";
import { PUBLIC_API_BASE_URL } from '$env/static/public';

export const load: PageLoad = async ({ fetch, params, url }) => {

    let offset = Number(url.searchParams.get("page"))


    const res = await fetch(`${PUBLIC_API_BASE_URL}/cards?page=${offset}`);
    const items = await res.json()

    return {
        items
    }
}