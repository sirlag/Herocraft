import { redirect } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';
import { PUBLIC_API_BASE_URL } from '$env/static/public';

export const load: PageServerLoad = async (event) => {
	await fetch(PUBLIC_API_BASE_URL + '/logout', {
		method: 'POST',
		headers: {
			Cookie: event.request.headers.get('Cookie')!!
		}
	});
	event.cookies.delete('user_session', { path: '/' });
	throw redirect(302, '/');
};
