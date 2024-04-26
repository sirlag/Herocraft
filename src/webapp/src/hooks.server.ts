import type { Handle } from '@sveltejs/kit';

export const handle: Handle = async ({ event, resolve}) => {
	const userSession = event.cookies.get("user_session")

	if (userSession) {
		event.locals.user = {
			isAuthenticated: true,
			session: userSession
		}
	}

	return resolve(event);
}