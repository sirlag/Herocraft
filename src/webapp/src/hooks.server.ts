import type { Handle } from '@sveltejs/kit';

export const handle: Handle = async ({ event, resolve }) => {
	const userSession = event.cookies.get('user_session');

	if (userSession) {
		event.locals.user = {
			isAuthenticated: true,
			session: userSession
		};
	} else {
		event.locals.user = {
			isAuthenticated: false,
			session: undefined
		};
	}

	return resolve(event);
};
