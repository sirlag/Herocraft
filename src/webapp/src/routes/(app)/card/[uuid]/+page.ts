import type { PageLoad } from './$types';
import { PUBLIC_API_BASE_URL } from '$env/static/public';

export const load: PageLoad = async ({ fetch, params }) => {
	let uuid = params['uuid'];

	const res = await fetch(`${PUBLIC_API_BASE_URL}/card/${uuid}`);
	const card = await res.json();

	return { card };
};
