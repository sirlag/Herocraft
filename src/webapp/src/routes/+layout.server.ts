import type { LayoutServerLoad } from './$types';
import { PUBLIC_API_BASE_URL } from '$env/static/public';
import { redirect } from '@sveltejs/kit';

export const load: LayoutServerLoad = async ({ request, locals, fetch, cookies }) => {
	let session = locals.user;

	if (session !== undefined && session.isAuthenticated) {
		let userResponse = await fetch(`${PUBLIC_API_BASE_URL}/user`, {
			method: 'GET',
			headers: {
				Cookie: request.headers.get('Cookie')!!
			}
		});

		if (!userResponse.ok) {
			cookies.delete("user_session", { path: '/'})
			locals.user = {
				isAuthenticated: false,
				session: undefined
			}

			return {
				session
			}
			// redirect(302, `/account/signin`)
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
