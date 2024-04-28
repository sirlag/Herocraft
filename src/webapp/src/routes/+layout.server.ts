import type { LayoutServerLoad } from '../../.svelte-kit/types/src/routes/(home)/$types';

export const load: LayoutServerLoad = async ({ locals }) => {
	let user = locals.user;
	return {
		user
	}
}