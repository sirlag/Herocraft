import { PUBLIC_API_BASE_URL } from '$env/static/public';

export const API_BASE_URL = PUBLIC_API_BASE_URL;

export const AccountURLs = {
	forgotPassword: `${API_BASE_URL}/account/forgot`,
	register: `${API_BASE_URL}/account/register`,
	resetPassword: `${API_BASE_URL}/account/reset`,
	signin: `${API_BASE_URL}/account/login`,
	verifyToken: (token: string) => `${API_BASE_URL}/account/verification/verify/${token}`,
}