import { PUBLIC_API_BASE_URL } from '$env/static/public';

export const API_BASE_URL = PUBLIC_API_BASE_URL;

export const AccountURLs = {
	forgotPassword: `${API_BASE_URL}/account/forgot`,
	register: `${API_BASE_URL}/account/register`,
	resetPassword: `${API_BASE_URL}/account/reset`,
	signin: `${API_BASE_URL}/account/login`,
	verifyToken: (token: string) => `${API_BASE_URL}/account/verification/verify/${token}`,
}

export const MeURLs = {
	me: `${API_BASE_URL}/api/me`,
	updateProfile: `${API_BASE_URL}/api/me/profile`,
	changePassword: `${API_BASE_URL}/api/me/security/password`
}

export const DeckURLs = {
	// Deck listing endpoints
	public: (page?: number, size?: number) => `${API_BASE_URL}/decks/public${page && size ? `?page=${page}&size=${size}` : ''}`,
	private: (page?: number, size?: number) => `${API_BASE_URL}/decks/private${page && size ? `?page=${page}&size=${size}` : ''}`,
	personal: `${API_BASE_URL}/decks/personal`,
	liked: (page?: number, size?: number) => `${API_BASE_URL}/decks/liked${page && size ? `?page=${page}&size=${size}` : ''}`,
	
	// Individual deck operations
	create: `${API_BASE_URL}/deck/new`,
	delete: (id: string) => `${API_BASE_URL}/decks/${id}`,
	update: (id: string) => `${API_BASE_URL}/decks/${id}`,
	favorite: (id: string) => `${API_BASE_URL}/decks/${id}/favorite`,
}