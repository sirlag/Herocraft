import type { LayoutServerLoad } from './$types';
import { PUBLIC_API_BASE_URL } from '$env/static/public';
import { redirect } from '@sveltejs/kit';

export const load: LayoutServerLoad = async ({ request, fetch }) => {
  // Ask backend who we are and whether we're admin
  const res = await fetch(`${PUBLIC_API_BASE_URL}/api/me`, {
    method: 'GET',
    headers: {
      Cookie: request.headers.get('Cookie')!!
    }
  });

  if (!res.ok) {
    // Not authenticated or session invalid -> sign in
    throw redirect(302, '/account/signin');
  }

  const me = await res.json();

  if (!me?.isAdmin) {
    // Authenticated but not an admin -> send home
    throw redirect(302, '/');
  }

  // Expose user to child pages if needed
  return { user: me };
};
