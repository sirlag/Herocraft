import type { PageLoad } from './$types';
import { PUBLIC_API_BASE_URL } from '$env/static/public';
import type { DeckEntry } from '../../../../../app';

export const load: PageLoad = async ({ fetch, url, parent }) => {
	let offset = Number(url.searchParams.get('page'));
	let query = url.searchParams.get('q');


	let endpoint = new URL(PUBLIC_API_BASE_URL + '/cards');
	if (query !== null) {
		endpoint.searchParams.append('q', query);
	}

	let classes = url.searchParams.getAll('c').filter((it) => it !== '');
	if (classes !== null) {
		classes
			.forEach((c) => endpoint.searchParams.append('c', c));
	}

	let specs = url.searchParams.getAll('s').filter((it) => it !== '');
	if (specs !== null) {
		specs
			.forEach((s) => endpoint.searchParams.append('s', s));
	}

	let formats = url.searchParams.getAll('f').filter((it) => it !== '');
	if (formats !== null) {
		formats
			.forEach((f) => endpoint.searchParams.append('f', f));
	}

	endpoint.searchParams.append('page', offset.toString());
	const res = await fetch(endpoint);
	const jsonRes = await res.json();

	const cards = jsonRes.items;
	const page: Page = {
		itemCount: jsonRes.itemCount,
		totalItems: jsonRes.totalItems,
		page: jsonRes.page,
		totalPages: jsonRes.totalPages,
		pageSize: jsonRes.pageSize,
		hasNext: jsonRes.hasNext
	};

	let parentData = await parent();
	let countObj: Map<string, number> = new Map();

	parentData.deckList.list.forEach((it: DeckEntry) => {
		// @ts-ignore
		countObj.set(it.card.id, it.count);
	});

	return {
		...parentData,
		cards,
		countObj,
		page,
		query,
		selectedClasses: classes,
		selectedSpecs: specs,
		selectedTypes: formats
	};
};

type Page = {
	itemCount: number;
	totalItems: number;
	page: number;
	totalPages: number;
	pageSize: number;
	hasNext: boolean;
};
