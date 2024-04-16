import type { PageLoad } from "./$types";

export const load: PageLoad = async ({ fetch, params, url }) => {

    let offset = Number(url.searchParams.get("page"))

    const res = await fetch(`http://localhost:8080/cards?page=${offset}`);
    const items = await res.json()

    return {
        items
    }
}