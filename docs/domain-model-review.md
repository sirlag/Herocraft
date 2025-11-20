### IvionCard Domain Model Review

Date: 2025-11-19 22:13 (local)

This review summarizes obvious strengths, potential pitfalls, and pragmatic improvements for the current IvionCard domain model and its mapping. It focuses on correctness, API ergonomics, and long‑term maintainability, keeping alignment with Scryfall’s patterns where it makes sense.

---

#### What looks solid
- Multi‑face support: faces[] with per‑face name/rulesText/flavorText/artist/imageUris maps cleanly to Scryfall’s card_faces[]. Good backward compatibility by keeping root imageUris.
- Layout enum: CardLayout = NORMAL | TRANSFORM | TOKEN is clear and avoids unused variants. Mapping infers TRANSFORM when a back face exists.
- Linked relations: linkedParts with { id, relation } (TOKEN, GENERATED_BY, TRANSFORMS_FROM, TRANSFORMS_INTO) is the right direction to capture semantics (Scryfall’s all_parts analogue).
- Identity layers: id (printing), herocraftId (rules‑equivalent group), printVariantGroupId (optional print/finish grouping) provide the necessary distinctions for reprints and promos.

---

#### High‑priority adjustments (likely bugs or confusing contracts)
1) Image URI contract can yield empty src on the frontend
- Why: IvionCardImageURIs fields are non‑nullable strings. The repo helpers build the object with empty strings for sizes that don’t exist, returning null only if all sizes are empty. On the frontend, getUrl() returns the requested size without checking for empty; fallback logic only triggers when imageUris is null. Result: empty <img src=""> is possible.
- Fix options:
  - Preferred: make IvionCardImageURIs fields nullable and only set non‑null sizes, or guarantee all sizes are present server‑side by filling missing sizes from the best available size.
  - UI stop‑gap: if selected size URL is empty string, fall back to another available size or to the legacy imageUris and finally to the Wix fallback. This still leaves the API contract a bit leaky.

2) TOKEN layout is never inferred today
- Why: Mapping sets TRANSFORM if back exists, else NORMAL. TOKEN is never produced.
- Suggestion: Infer TOKEN when type == "Token" (or extraType indicates token) so clients can rely on it for rendering and behavior.

3) Dual artist fields can mislead
- Why: artist is non‑null at root, but faces[] can carry per‑face artist. For transform cards with different back‑side artist, the root field may be inaccurate.
- Suggestion: Treat root artist as the front‑face artist (document it), or make root artist nullable and rely on per‑face artist when faces are present. Document precedence rules for clients.

---

#### Medium‑priority modeling clarifications
4) herocraftId, printVariantGroupId, variants invariants
- Clarify whether variants must list “siblings” from herocraftId and/or printVariantGroupId, or if variants is just a convenience list. If derivable via queries, consider omitting variants from the payload and provide an endpoint to discover prints by group id(s). If you keep it, document:
  - If herocraftId is present, variants should contain all other print ids sharing the same herocraftId (or explicitly say it’s partial/best‑effort).
  - Whether printVariantGroupId implies a disjoint or nested relation with herocraftId.

5) LinkedPart directionality
- With TRANSFORMS_FROM and TRANSFORMS_INTO, decide whether both directions appear simultaneously or standardize on one direction per pair to avoid duplication/noise. Document expectations so consumers don’t double‑render relationships.

6) secondUUID vs faces
- secondUUID is legacy and useful for fallback, but it duplicates faces semantics. Consider marking it as deprecated in API docs and planning a removal once all images are face‑backed. Until then, define precedence: faces → secondUUID → root imageUris → Wix fallback.

7) Root imageUris vs faces.imageUris precedence
- You already prefer faces.imageUris in the UI. Document that contract in the API: when faces[] present, root imageUris refers to the front face only and may be omitted later.

---

#### Lower‑priority improvements / polish
8) Constrain stringly‑typed fields where stable
- type, extraType, colorPip1/colorPip2, format are strings. If these come from closed sets, consider enums (backend) and string unions (frontend) to improve validation and searchability. Keep them optional to preserve backward compatibility.

9) Optional metadata parity (future‑proofing)
- If collection features matter later, consider adding rarity, language, releasedAt, and finishes. Keep them optional.

10) Numeric sanity
- actionCost, powerCost, range, health presumably non‑negative. Enforce basic validation at ingestion or document expected ranges.

11) Identity naming clarity
- ivionUUID is used as a front‑face asset id. You may want to rename it in the future to frontFaceAssetId (and secondUUID → backFaceAssetId) for self‑descriptive semantics. Not urgent given existing DB and clients.

12) Time representation consistency
- IvionCardImage uses kotlin.time.Instant for createdAt/updatedAt, while Exposed timestamp usually pairs best with kotlinx.datetime.Instant. The code compiles, but aligning on kotlinx.datetime.Instant across persistence/DTOs reduces confusion and avoids potential serialization quirks.

---

### Recommended immediate tweaks (backward‑compatible)
- Imaging contract:
  - Backend: ensure IvionCardImageURIs never contains empty strings. Either make fields nullable or backfill missing sizes from the best available size before serializing.
  - Frontend: add a defensive fallback if getUrl(...) returns an empty string.
- Layout inference:
  - In CardRepo.CardEntity.toIvionCard, set layout = TOKEN when type == "Token" (or a defined token marker), otherwise keep current logic.
- Documentation:
  - Document precedence and deprecation: faces over root imageUris, and eventual removal of secondUUID.

These changes keep the API additive, improve correctness for common clients, and align your domain model with the behaviors you intend.

### Optional next steps
- Add a tiny integration/unit test around image URI selection to catch regressions: construct a card with partial sizes and verify selected size fallback behavior.
- Provide a /cards/{id}/prints and /cards/{id}/related endpoints to let clients fetch variants and linkedParts explicitly, reducing payload size and ambiguity.
