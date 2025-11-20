import type { Actions, PageServerLoad } from './$types';
import { PUBLIC_API_BASE_URL } from '$env/static/public';

export const load: PageServerLoad = async ({ fetch, params }) => {
  const herocraftId = params['herocraftId'];

  let rulings: any[] = [];
  try {
    const res = await fetch(
      `${PUBLIC_API_BASE_URL}/admin/rulings?cardHerocraftId=${encodeURIComponent(herocraftId)}`,
      { credentials: 'include' }
    );
    if (res.ok) rulings = await res.json();
  } catch (e) {}

  return { herocraftId, rulings };
};

export const actions: Actions = {
  create: async ({ request, fetch }) => {
    const form = await request.formData();
    const payload = {
      cardHerocraftId: form.get('cardHerocraftId'),
      source: form.get('source'),
      sourceUrl: (form.get('sourceUrl') as string) || null,
      publishedAt: form.get('publishedAt'),
      style: form.get('style'),
      comment: (form.get('comment') as string) || null,
      question: (form.get('question') as string) || null,
      answer: (form.get('answer') as string) || null
    };

    const res = await fetch(`${PUBLIC_API_BASE_URL}/admin/rulings`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify(payload)
    });

    if (!res.ok) {
      return { success: false, error: await res.text() };
    }

    return { success: true };
  },

  delete: async ({ request, fetch }) => {
    const form = await request.formData();
    const id = form.get('id');
    const res = await fetch(`${PUBLIC_API_BASE_URL}/admin/rulings/${id}`, {
      method: 'DELETE',
      credentials: 'include'
    });
    if (!res.ok && res.status !== 204) {
      return { success: false, error: await res.text() };
    }
    return { success: true };
  }
};
