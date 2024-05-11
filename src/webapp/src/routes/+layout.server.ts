import type { LayoutServerLoad } from './$types';
import { PUBLIC_API_BASE_URL } from '$env/static/public';

export const load: LayoutServerLoad = async ({ request, locals, fetch }) => {
	let session = locals.user;

	if (session !== undefined && session.isAuthenticated) {
		let userResponse = await fetch(`${PUBLIC_API_BASE_URL}/user`, {
			method: 'GET',
			headers: {
				Cookie: request.headers.get('Cookie')!!
			}
		});

		if (!userResponse.ok) {

		}

		let user = await userResponse.json();

		return {
			user,
			session
		};
	}

	return {
		session
	};
};
