import type { PageLoad } from "./$types";
import { PUBLIC_API_BASE_URL } from '$env/static/public';

export const load: PageLoad = async ({ fetch, url }) => {

    let offset = Number(url.searchParams.get("page"))

    const res = await fetch(`${PUBLIC_API_BASE_URL}/cards?page=${offset}`);
    const jsonRes = await res.json()

    const cards = jsonRes.items
    const page: Page = {
        itemCount: jsonRes.itemCount,
        totalItems: jsonRes.totalItems,
        page: jsonRes.page,
        totalPages: jsonRes.totalPages,
        pageSize: jsonRes.pageSize,
        hasNext: jsonRes.hasNext
    }

    return {
        cards, page
    }
}

class Page {
    itemCount: number = 0;
    totalItems: number = 0;
    page: number = 0;
    totalPages: number = 0;
    pageSize: number = 0;
    hasNext: boolean = false;
}