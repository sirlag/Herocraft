// See https://kit.svelte.dev/docs/types#app
// for information about these interfaces
declare global {
	namespace App {
		// interface Error {}
		interface Locals {
			user: UserSession | undefined;
		}
		// interface PageData {}
		// interface PageState {}
		// interface Platform {}
	}
}

type UserSession = {
	isAuthenticated: Boolean,
	session: String
}

export {

};
