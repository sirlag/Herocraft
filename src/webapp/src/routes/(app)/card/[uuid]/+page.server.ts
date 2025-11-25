import type { PageServerLoad } from './$types';
import { PUBLIC_API_BASE_URL } from '$env/static/public';

type ImageUris = {
  full?: string | null;
  large?: string | null;
  normal?: string | null;
  small?: string | null;
};

type CardFace = {
  face?: string | null;
  imageUris?: ImageUris | null;
  // Optional face-level metadata we may want in SEO
  rulesText?: string | null;
  actionCost?: number | null;
  powerCost?: number | null;
  range?: number | null;
  artist?: string | null;
};

type IvionCard = {
  id?: string | number;
  uuid?: string;
  name?: string;
  layout?: string;
  imageUris?: ImageUris | null;
  faces?: CardFace[] | null;
  archetype?: string | null;
  type?: string | null;
  extraType?: string | null;
  rulesText?: string | null;
  actionCost?: number | null;
  powerCost?: number | null;
  range?: number | null;
  artist?: string | null;
  herocraftId?: string | null;
  rulings_uri?: string | null;
};

function normalizeFace(face?: string | null): 'front' | 'back' | null {
  if (!face) return null;
  const v = face.toLowerCase();
  return v === 'front' ? 'front' : v === 'back' ? 'back' : null;
}

function pickFaceUris(card: IvionCard, which: 'front' | 'back'): ImageUris | null {
  if (!card) return null;
  if (card.layout === 'TRANSFORM' && Array.isArray(card.faces)) {
    const m = card.faces.find((f) => normalizeFace(f.face) === which);
    return m?.imageUris ?? null;
  }
  return card.imageUris ?? null;
}

function pickSmallest(uris?: ImageUris | null): string | null {
  if (!uris) return null;
  // Prefer the smallest reasonable preview for link embeds
  return uris.small ?? uris.normal ?? uris.large ?? uris.full ?? null;
}

function normalizeText(s?: string | null): string {
  if (!s) return '';
  // Strip HTML tags and collapse whitespace; also remove common icon markup like {A}
  const noTags = s
    .replace(/<[^>]*>/g, ' ')
    .replace(/[{}]/g, '')
    .replace(/\s+/g, ' ')
    .trim();
  return noTags;
}

function truncate(s: string, max = 300): string {
  if (s.length <= max) return s;
  return s.slice(0, max - 1).trimEnd() + '…';
}

function pickPrimaryFace(card: IvionCard): CardFace | null {
  if (card.layout === 'TRANSFORM' && Array.isArray(card.faces)) {
    const front = card.faces.find((f) => normalizeFace(f.face) === 'front');
    return front ?? null;
  }
  return null;
}

export const load: PageServerLoad = async ({ fetch, params, url }) => {
  const uuid = params['uuid'];

  // Reuse the existing public API to fetch card data (server-side)
  const res = await fetch(`${PUBLIC_API_BASE_URL}/card/${uuid}`);
  const card: IvionCard = await res.json();

  // Fetch rulings using hypermedia link or herocraftId if available
  let rulings: any[] = [];
  try {
    // Prefer hypermedia link from card payload
    let rulingsPath: string | null = card?.rulings_uri ?? null;
    // Fallback to herocraftId if provided
    if (!rulingsPath && card?.herocraftId) rulingsPath = `/cards/${card.herocraftId}/rulings`;
    if (rulingsPath) {
      const r = await fetch(`${PUBLIC_API_BASE_URL}${rulingsPath}`);
      if (r.ok) rulings = await r.json();
    }
  } catch (e) {
    // ignore; rulings optional
  }

  // Build an enriched description including rules text, costs/range, and artist
  const headerParts: string[] = [];
  if (card?.archetype) headerParts.push(card.archetype);
  if (card?.type) headerParts.push(card.type);
  if (card?.extraType) headerParts.push(card.extraType);

  const primary = pickPrimaryFace(card);
  const rulesText = normalizeText(primary?.rulesText ?? card?.rulesText ?? '');

  const costs: string[] = [];
  const action = primary?.actionCost ?? card?.actionCost;
  const power = primary?.powerCost ?? card?.powerCost;
  const range = primary?.range ?? card?.range;
  if (action !== undefined && action !== null) costs.push(`Action ${action}`);
  if (power !== undefined && power !== null) costs.push(`Power ${power}`);
  if (range !== undefined && range !== null) costs.push(`Range ${range}`);

  const artist = primary?.artist ?? card?.artist ?? null;

  const segments: string[] = [];
  if (headerParts.length) segments.push(headerParts.join(' – '));
  if (rulesText) segments.push(rulesText);
  if (costs.length) segments.push(costs.join(' • '));
  if (artist) segments.push(`Artist: ${artist}`);

  const description = truncate(
    segments.length ? segments.join(' | ') : 'An Ivion card on Herocraft'
  );

  // Prefer the front face for transform cards, otherwise the card image
  const frontUris = pickFaceUris(card, 'front');
  const image = pickSmallest(frontUris);

  const seo = {
    title: card?.name ? `${card.name} // Herocraft` : 'Herocraft',
    description,
    image: image ?? null,
    url: url.href,
    siteName: 'Herocraft',
    type: 'website'
  };

  return { card, rulings, pageUrl: url.href, seo };
};
