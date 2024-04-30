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

enum DeckVisibility {
	Public = "PUBLIC",
	Unlisted = "UNLISTED",
	Private = "PRIVATE"
}

enum DeckFormat {
	Constructed = "CONSTRUCTED",
	Paragon = "PARAGON",
	Other = "OTHER"
}

type DeckEntry = {
	count: number,
	card: IvionCard
}

type Deck = {
	id: string,
	hash: string,
	name: string,
	list: DeckEntry[],
	owner: string,
	ownerName: string,
	visibility: DeckVisibility,
	format: DeckFormat
}

type C2Array = {
	c2Array: boolean
	size: number[]
	data: any[][][]
}

export {
	C2Array,
	Deck,
	DeckEntry,
	UserSession,
};
