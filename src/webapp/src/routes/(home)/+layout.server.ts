
import type { LayoutServerLoad } from './$types';

export const load: LayoutServerLoad = async ({ locals }) => {
	let user = locals.user
	console.log(user)
	console.log("HI CLOUDFLARE")
	return {
		user
	}
}