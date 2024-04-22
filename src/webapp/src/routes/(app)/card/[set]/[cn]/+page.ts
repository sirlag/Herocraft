import type { PageLoad } from '../../../../../../.svelte-kit/types/src/routes/(app)/cards/$types';
import { PUBLIC_API_BASE_URL } from '$env/static/public';

export const load: PageLoad = async ({ fetch, params }) => {

	let set = params["set"];
	let cn = params["cn"];

	const res = await fetch(`${PUBLIC_API_BASE_URL}/card/${set}/${cn}`);
	const card = await res.json()

	return { card }
}