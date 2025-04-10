import { PUBLIC_API_BASE_URL } from '$env/static/public';

export const API_BASE_URL = PUBLIC_API_BASE_URL;

export const AccountURLs = {
	signin: `${API_BASE_URL}/account/login`,
	register: `${API_BASE_URL}/account/register`,
	forgotPassword: `${API_BASE_URL}/account/forgot`,
	verifyToken: (token: string) => `${API_BASE_URL}/account/verification/verify/${token}`,
}