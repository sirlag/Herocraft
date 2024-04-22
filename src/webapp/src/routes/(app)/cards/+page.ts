import type { PageLoad } from "./$types";
import { PUBLIC_API_BASE_URL } from '$env/static/public';
import { redirect } from '@sveltejs/kit';

export const load: PageLoad = async ({ fetch, url }) => {

    let offset = Number(url.searchParams.get("page"))
    let query = url.searchParams.get("q")

    let endpoint = new URL(PUBLIC_API_BASE_URL+"/cards",)
    endpoint.searchParams.append("q", query);
    endpoint.searchParams.append("page", offset);
    const res = await fetch(endpoint);
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


    if (page.totalItems == 1) {
        await redirect(303,`/card/${cards[0].id}`)
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