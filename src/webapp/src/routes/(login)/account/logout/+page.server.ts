import { redirect } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';
import { PUBLIC_API_BASE_URL } from '$env/static/public';
import { invalidateAll } from '$app/navigation';

export const load: PageServerLoad = async (event) => {
	fetch(PUBLIC_API_BASE_URL + '/logout', {
		method: 'POST',
		headers: {
			Cookie: event.request.headers.get('Cookie')!!
		}
	}).then(it => console.log(it))
	event.cookies.delete('user_session', { path: '/' });
	redirect(302, '/');
};
