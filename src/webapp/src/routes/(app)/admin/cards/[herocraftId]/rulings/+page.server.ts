import type { Actions, PageServerLoad } from './$types';
import { PUBLIC_API_BASE_URL } from '$env/static/public';

export const load: PageServerLoad = async ({ fetch, params }) => {
  const herocraftId = params['herocraftId'];

  let rulings: any[] = [];
  let cardName: string | undefined = undefined;
  try {
    const res = await fetch(
      `${PUBLIC_API_BASE_URL}/admin/rulings?cardHerocraftId=${encodeURIComponent(herocraftId)}`,
      { credentials: 'include' }
    );
    if (res.ok) {
      rulings = await res.json();
      // Ensure rulings display in the order they were created (oldest first).
      // Prefer explicit createdAt if present; fall back to publishedAt; otherwise leave as-is.
      rulings = Array.isArray(rulings)
        ? [...rulings].sort((a, b) => {
            const ta = a?.createdAt ?? a?.publishedAt ?? 0;
            const tb = b?.createdAt ?? b?.publishedAt ?? 0;
            const da = typeof ta === 'string' ? Date.parse(ta) : Number(ta) || 0;
            const db = typeof tb === 'string' ? Date.parse(tb) : Number(tb) || 0;
            return da - db; // oldest first
          })
        : rulings;
    }
  } catch (e) {}

  // Also fetch the card details to display a friendly name in the UI
  try {
    const cardRes = await fetch(`${PUBLIC_API_BASE_URL}/card/${encodeURIComponent(herocraftId)}`, {
      credentials: 'include'
    });
    if (cardRes.ok) {
      const card = await cardRes.json();
      cardName = card?.name ?? undefined;
    }
  } catch (_) {}

  return { herocraftId, rulings, cardName };
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
