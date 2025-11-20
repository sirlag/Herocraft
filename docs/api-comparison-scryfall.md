### Herocrafter IvionCard vs. Scryfall Card API — Conceptual Comparison

This document compares the Herocrafter IvionCard API shape to the Scryfall Card API. While the games and many domain fields differ, both APIs model printed cards, multi-face layouts, related/linked cards, images, and variants/reprints. The goal is to clarify alignment, key differences, and optional enhancements for future parity.

---

#### High-level object identity
- Scryfall
  - id: UUID for a specific printing.
  - oracle_id: UUID shared by all reprints with identical rules text (“oracle text”).
  - prints_search_uri and related URIs allow navigating across printings.
- Herocrafter (current)
  - id: primary identifier for a card record (per printing).
  - ivionUUID: domain identifier for the front face (maps to image asset identity), secondUUID: optional back face id.
  - herocraftId: canonical grouping id for rules-equivalent cards across prints/seasons (closest analog to Scryfall’s oracle_id for grouping).
  - printVariantGroupId: optional group for print/finish variants within a season/set if needed.
  - variants: list of card ids that belong to the same group(s) as applicable.

Notes:
- If you want to precisely mirror Scryfall’s dual identity model, consider:
  - oracleId-like: fully rules-equivalent grouping id across printings/sets.
  - print group id: group for alternate frames/promo/foil variants of the same print in the same set (Scryfall implicitly models this via their Set + collector_number + finishes, not a separate id).

---

#### Multi-face cards
- Scryfall
  - card_faces: array present for multi-face layouts (transform, split, aftermath, modal DFC, etc.). Per-face fields contain: name, oracle_text, mana_cost, type_line, power/toughness for creatures, image_uris (for certain layouts), artist, illustration_id, etc.
  - layout: enum indicating the card’s physical layout (normal, transform, modal_dfc, split, flip, meld, etc.).
  - image_uris can be present at the top-level (single-faced) or per-face (modal/transform).
- Herocrafter (current)
  - faces?: List<IvionCardFaceData> where each face has: face (front|back), name?, rulesText?, flavorText?, artist?, imageUris?.
  - imageUris?: still present on the root for backward compatibility (represents front images by convention).
  - secondUUID: legacy indicator that a back face exists; UI now prefers faces when present.

Alignment:
- faces ≈ card_faces. Root imageUris ≈ Scryfall’s top-level image_uris for single-faced cards.

Differences and opportunities:
- Herocrafter uses a layout enum similar to Scryfall’s for clarity (currently NORMAL, TRANSFORM, TOKEN; split is not used).
- Per-face mechanical stats (e.g., power/toughness) aren’t modeled today, which is fine for Ivion’s mechanics but can be extended similarly to Scryfall if needed.

---

#### Images and sizes
- Scryfall image_uris keys: small, normal, large, png, art_crop, border_crop (and sometimes per-face).
- Herocrafter imageUris keys: small, normal, large, full.

Alignment:
- small/normal/large map directly.
- full roughly aligns with Scryfall’s png or a high-resolution original; semantics are project-defined.

Opportunities:
- Consider adding optional artCrop/borderCrop variants if you plan to render cropped art or borderless previews.

---

#### Linked/related cards
- Scryfall
  - all_parts: array of related objects with component values like token, meld_result, combo_piece, etc., each with a reference to another card.
- Herocrafter (current)
  - linkedIds?: List<Uuid> — plain references to other cards.

Opportunities:
- If you need richer semantics like Scryfall’s component field, consider making linkedIds an array of objects such as { id, relation } where relation is an enum (e.g., token, generated_by, transforms_from, transforms_into).

---

#### Variants, sets, and collector info
- Scryfall
  - set, set_name, collector_number, finishes, frame, frame_effects, promo, variation, rarity, language (lang), released_at, etc.
  - Reprints: use oracle_id to group across sets; each print has a unique id and set/collector_number pair.
- Herocrafter (current)
  - collectorsNumber, season (analogous to set), format, type, extraType, rarity not explicitly present, language not present.
  - variantGroupId and variants provide a grouping mechanism for promos/reprints.

Opportunities:
- Consider explicit fields for: rarity, frame/frameEffects (if relevant), language, release date, finishes (e.g., foil), and promo flags if they matter for collections.

---

#### Rules/Oracle text parity
- Scryfall
  - oracle_text (rules text), printed_text (as it appears on a specific printing), plus oracle_id for cross-print equivalence.
- Herocrafter (current)
  - rulesText and flavorText at root; per-face rulesText/flavorText optional via faces[].

Opportunities:
- If you anticipate templating differences across printings while maintaining game-equivalent rules, adopt/use a canonical herocraftId to distinguish “rules-equivalent” from “print/variant-equivalent.”

---

#### Legalities and formats
- Scryfall provides a legalities map per format (standard/pioneer/modern/commander, etc.).
- Herocrafter has format as a simple string per card; full legality matrices are not modeled.

Opportunity:
- If multiple Ivion formats exist with varying legality, consider a map of format → legal/banned/restricted, or maintain format at deck-level only.

---

#### Identifiers
- Scryfall exposes many third-party identifiers (multiverse_ids, tcgplayer_id, cardmarket_id, etc.).
- Herocrafter currently uses ivionUUID/secondUUID for image asset identity; external marketplace ids are not applicable.

---

### Summary mapping table (conceptual)
- IvionCard.faces[] ≈ Scryfall.card_faces[]
- IvionCard.imageUris ≈ Scryfall.image_uris (single-face) or card_faces[i].image_uris (multi-face)
- IvionCard.herocraftId ≈ Scryfall.oracle_id (group of rules-equivalent prints)
- IvionCard.variants ≈ all prints sharing the group id (Scryfall discovers via prints_search_uri)
- IvionCard.linkedIds ≈ Scryfall.all_parts (but without component metadata)
- IvionCard.season ≈ Scryfall.set (string key) / set_name (display)
- IvionCard.collectorsNumber ≈ Scryfall.collector_number

### Suggested incremental enhancements
1) Add/maintain layout: enum to describe card physical layout (normal, transform, token, etc.).
2) Clarify grouping semantics by using two ids:
   - herocraftId: rules-equivalent grouping across sets/prints.
   - printVariantGroupId: variants/promos within a specific set/season if you need that distinction.
3) Enrich linkedIds to linkedParts: [{ id, relation }], with relation enum akin to Scryfall’s component values.
4) Optional image variants: artCrop/borderCrop if your asset pipeline supports them.
5) Optional fields for rarity, language, release date, and finishes, if collection-centric features are desired.

These adjustments keep backward compatibility (all optional) while moving the model closer to Scryfall’s proven patterns.
