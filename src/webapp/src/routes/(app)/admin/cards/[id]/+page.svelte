<script lang="ts">
  import { onMount } from 'svelte';
  import { Button } from '$lib/components/ui/button';
  import { API_BASE_URL } from '$lib/routes';
  import { page } from '$app/state';
  import ParsedCardText from '$lib/components/CardText/ParsedCardText.svelte';

  let id = $derived(page.params.id);

  // Fields
  let name = $state('');
  let artist = $state('');
  let season = $state('');
  let format = $state('');
  let archetype = $state('');
  let type = $state('');
  let rulesText = $state('');
  let flavorText = $state('');
  let collectorsNumber = $state('');
  let ivionUUID = $state('');
  let secondUUID = $state('');
  let colorPip1 = $state('');
  let colorPip2 = $state('');
  let actionCost: number | null = $state(null);
  let powerCost: number | null = $state(null);
  let range: number | null = $state(null);
  let health: number | null = $state(null);
  let heroic = $state(false);
  let slow = $state(false);
  let silence = $state(false);
  let disarm = $state(false);
  let extraType = $state('');

  // New features
  type CardLayout = 'NORMAL' | 'TRANSFORM' | 'TOKEN';
  let layout: CardLayout = $state('NORMAL');
  let variantsText = $state('');

  // Face data and image URIs
  let frontName = $state('');
  let frontRules = $state('');
  let frontFlavor = $state('');
  let frontArtist = $state('');
  let frontActionCost: number | null = $state(null);
  let frontPowerCost: number | null = $state(null);
  let frontHeroic = $state(false);
  let frontSlow = $state(false);
  let frontSilence = $state(false);
  let frontDisarm = $state(false);
  let frontFull = $state('');
  let frontLarge = $state('');
  let frontNormal = $state('');
  let frontSmall = $state('');

  let hasBackFace = $state(false);
  let backName = $state('');
  let backRules = $state('');
  let backFlavor = $state('');
  let backArtist = $state('');
  let backActionCost: number | null = $state(null);
  let backPowerCost: number | null = $state(null);
  let backHeroic = $state(false);
  let backSlow = $state(false);
  let backSilence = $state(false);
  let backDisarm = $state(false);
  let backFull = $state('');
  let backLarge = $state('');
  let backNormal = $state('');
  let backSmall = $state('');

  let loading = $state(true);
  let saving = $state(false);
  let error: string | null = $state(null);

  async function load() {
    loading = true;
    try {
      const res = await fetch(`${API_BASE_URL}/card/${id}`);
      if (!res.ok) throw new Error('Failed to load card');
      const c = await res.json();
      name = c.name ?? '';
      artist = c.artist ?? '';
      season = c.season ?? '';
      format = c.format ?? '';
      archetype = c.archetype ?? '';
      type = c.type ?? '';
      rulesText = c.rulesText ?? '';
      flavorText = c.flavorText ?? '';
      collectorsNumber = c.collectorsNumber ?? '';
      ivionUUID = c.ivionUUID ?? '';
      secondUUID = c.secondUUID ?? '';
      colorPip1 = c.colorPip1 ?? '';
      colorPip2 = c.colorPip2 ?? '';
      actionCost = c.actionCost ?? null;
      powerCost = c.powerCost ?? null;
      range = c.range ?? null;
      health = c.health ?? null;
      heroic = c.heroic ?? false;
      slow = c.slow ?? false;
      silence = c.silence ?? false;
      disarm = c.disarm ?? false;
      extraType = c.extraType ?? '';

      // Note: herocraftId will be implemented later; for now we will use ivionUUID

      layout = (c.layout ?? 'NORMAL');
      variantsText = Array.isArray(c.variants) ? c.variants.join(', ') : '';

      // Faces
      if (Array.isArray(c.faces)) {
        const front = c.faces.find((f: any) => f.face === 'FRONT');
        const back = c.faces.find((f: any) => f.face === 'BACK');
        if (front) {
          frontName = front.name ?? '';
          frontRules = front.rulesText ?? '';
          frontFlavor = front.flavorText ?? '';
          frontArtist = front.artist ?? '';
          frontActionCost = front.actionCost ?? null;
          frontPowerCost = front.powerCost ?? null;
          frontHeroic = !!front.heroic;
          frontSlow = !!front.slow;
          frontSilence = !!front.silence;
          frontDisarm = !!front.disarm;
          if (front.imageUris) {
            frontFull = front.imageUris.full ?? '';
            frontLarge = front.imageUris.large ?? '';
            frontNormal = front.imageUris.normal ?? '';
            frontSmall = front.imageUris.small ?? '';
          }
        }
        if (back) {
          hasBackFace = true;
          backName = back.name ?? '';
          backRules = back.rulesText ?? '';
          backFlavor = back.flavorText ?? '';
          backArtist = back.artist ?? '';
          backActionCost = back.actionCost ?? null;
          backPowerCost = back.powerCost ?? null;
          backHeroic = !!back.heroic;
          backSlow = !!back.slow;
          backSilence = !!back.silence;
          backDisarm = !!back.disarm;
          if (back.imageUris) {
            backFull = back.imageUris.full ?? '';
            backLarge = back.imageUris.large ?? '';
            backNormal = back.imageUris.normal ?? '';
            backSmall = back.imageUris.small ?? '';
          }
        } else {
          hasBackFace = false;
        }
      } else {
        hasBackFace = false;
      }
    } catch (e) {
      error = (e as Error).message;
    } finally {
      loading = false;
    }
  }

  async function save() {
    saving = true;
    error = null;
    try {
      const payload = {
        id,
        collectorsNumber: collectorsNumber || null,
        format: format || null,
        name,
        archetype: archetype || null,
        actionCost,
        powerCost,
        range,
        health,
        heroic,
        slow,
        silence,
        disarm,
        extraType: extraType || null,
        rulesText: rulesText || null,
        flavorText: flavorText || null,
        artist,
        ivionUUID,
        secondUUID: secondUUID || null,
        colorPip1: colorPip1 || null,
        colorPip2: colorPip2 || null,
        season,
        type: type || null,
        layout,
        variants: variantsText
          .split(',')
          .map((v) => v.trim())
          .filter((v) => v.length > 0),
        faces: buildFacesPayload(),
      };
      const res = await fetch(`${API_BASE_URL}/admin/cards/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      if (!res.ok) throw new Error('Failed to save');
      alert('Saved');
    } catch (e) {
      error = (e as Error).message;
    } finally {
      saving = false;
    }
  }

  onMount(load);

  function buildFacesPayload() {
    const faces: any[] = [];
    const front: any = {
      face: 'FRONT',
      name: (frontName || name) || null,
      rulesText: (frontRules || rulesText) || null,
      flavorText: (frontFlavor || flavorText) || null,
      artist: (frontArtist || artist) || null,
      actionCost: frontActionCost,
      powerCost: frontPowerCost,
      heroic: frontHeroic,
      slow: frontSlow,
      silence: frontSilence,
      disarm: frontDisarm,
    };
    const fUris = pickUris(frontFull, frontLarge, frontNormal, frontSmall);
    if (fUris) front.imageUris = fUris;
    faces.push(front);

    if (hasBackFace) {
      const back: any = {
        face: 'BACK',
        name: backName || null,
        rulesText: backRules || null,
        flavorText: backFlavor || null,
        artist: backArtist || null,
        actionCost: backActionCost,
        powerCost: backPowerCost,
        heroic: backHeroic,
        slow: backSlow,
        silence: backSilence,
        disarm: backDisarm,
      };
      const bUris = pickUris(backFull, backLarge, backNormal, backSmall);
      if (bUris) back.imageUris = bUris;
      faces.push(back);
    }
    return faces;
  }

  function pickUris(full: string, large: string, normal: string, small: string) {
    const any = full || large || normal || small;
    if (!any) return null;
    return { full: full || '', large: large || '', normal: normal || '', small: small || '' };
  }
</script>

<div class="container mx-auto p-4 space-y-4">
  <div class="flex items-center justify-between">
    <h1 class="text-2xl font-semibold">Edit Card</h1>
    <div class="flex items-center gap-4">
      {#if ivionUUID}
        <a class="underline" href={`/admin/cards/${ivionUUID}/rulings`} title="Manage rulings for this card">
          Rulings
        </a>
      {/if}
      <a class="underline" href="/admin/cards">Back</a>
    </div>
  </div>
  {#if loading}
    <p>Loading…</p>
  {:else}
    {#if error}<p class="text-red-600">{error}</p>{/if}
    <form class="grid gap-3 max-w-3xl" on:submit|preventDefault={save}>
      <label class="grid gap-1">
        <span>Name</span>
        <input class="p-2 border rounded" bind:value={name} required />
      </label>
      <label class="grid gap-1">
        <span>Artist</span>
        <input class="p-2 border rounded" bind:value={artist} required />
      </label>
      <label class="grid gap-1">
        <span>Season</span>
        <input class="p-2 border rounded" bind:value={season} required />
      </label>

      <div class="grid grid-cols-2 gap-3">
        <label class="grid gap-1">
          <span>Format</span>
          <input class="p-2 border rounded" bind:value={format} />
        </label>
        <label class="grid gap-1">
          <span>Archetype</span>
          <input class="p-2 border rounded" bind:value={archetype} />
        </label>
        <label class="grid gap-1">
          <span>Type</span>
          <input class="p-2 border rounded" bind:value={type} />
        </label>
        <label class="grid gap-1">
          <span>Collector #</span>
          <input class="p-2 border rounded" bind:value={collectorsNumber} />
        </label>
        <label class="grid gap-1">
          <span>Ivion UUID</span>
          <input class="p-2 border rounded" bind:value={ivionUUID} required />
        </label>
        <label class="grid gap-1">
          <span>Second UUID</span>
          <input class="p-2 border rounded" bind:value={secondUUID} />
        </label>
        <label class="grid gap-1">
          <span>Color Pip 1</span>
          <input class="p-2 border rounded" bind:value={colorPip1} />
        </label>
        <label class="grid gap-1">
          <span>Color Pip 2</span>
          <input class="p-2 border rounded" bind:value={colorPip2} />
        </label>
        <label class="grid gap-1">
          <span>Action Cost</span>
          <input class="p-2 border rounded" bind:value={actionCost} type="number" />
        </label>
        <label class="grid gap-1">
          <span>Power Cost</span>
          <input class="p-2 border rounded" bind:value={powerCost} type="number" />
        </label>
        <label class="grid gap-1">
          <span>Range</span>
          <input class="p-2 border rounded" bind:value={range} type="number" />
        </label>
        <label class="grid gap-1">
          <span>Health</span>
          <input class="p-2 border rounded" bind:value={health} type="number" />
        </label>
      </div>

      <div class="grid grid-cols-2 gap-3">
        <label class="flex items-center gap-2"><input type="checkbox" bind:checked={heroic} /> Heroic</label>
        <label class="flex items-center gap-2"><input type="checkbox" bind:checked={slow} /> Slow</label>
        <label class="flex items-center gap-2"><input type="checkbox" bind:checked={silence} /> Silence</label>
        <label class="flex items-center gap-2"><input type="checkbox" bind:checked={disarm} /> Disarm</label>
      </div>

      <label class="grid gap-1">
        <span>Extra Type</span>
        <input class="p-2 border rounded" bind:value={extraType} />
      </label>
      <label class="grid gap-1">
        <span>Rules Text</span>
        <textarea class="p-2 border rounded" bind:value={rulesText} rows="4"></textarea>
      </label>
      <label class="grid gap-1">
        <span>Flavor Text</span>
        <textarea class="p-2 border rounded" bind:value={flavorText} rows="3"></textarea>
      </label>

      <!-- Live preview for rules rendered like the normal card page -->
      <div class="rounded border p-3 bg-muted/30">
        <div class="text-sm font-medium mb-1">Rules Preview</div>
        <div class="prose text-sm">
          <ParsedCardText source={rulesText} />
        </div>
      </div>

      <!-- Variants and layout -->
      <div class="grid grid-cols-2 gap-3">
        <label class="grid gap-1">
          <span>Layout</span>
          <select class="p-2 border rounded" bind:value={layout}>
            <option value="NORMAL">Normal</option>
            <option value="TRANSFORM">Transform</option>
            <option value="TOKEN">Token</option>
          </select>
        </label>
        <label class="grid gap-1">
          <span>Variants (UUIDs, comma-separated)</span>
          <input class="p-2 border rounded" bind:value={variantsText} />
        </label>
      </div>

      <!-- Faces -->
      <fieldset class="border rounded p-3 space-y-3">
        <legend class="px-1 text-sm font-semibold">Front Face</legend>
        <div class="grid grid-cols-2 gap-3">
          <label class="grid gap-1">
            <span>Name (override)</span>
            <input class="p-2 border rounded" bind:value={frontName} placeholder="defaults to Name" />
          </label>
          <div class="grid grid-cols-2 gap-3 col-span-2">
            <label class="grid gap-1">
              <span>Action Cost (override)</span>
              <input class="p-2 border rounded" bind:value={frontActionCost} type="number" />
            </label>
            <label class="grid gap-1">
              <span>Power Cost (override)</span>
              <input class="p-2 border rounded" bind:value={frontPowerCost} type="number" />
            </label>
            <label class="flex items-center gap-2"><input type="checkbox" bind:checked={frontHeroic} /> Heroic</label>
            <label class="flex items-center gap-2"><input type="checkbox" bind:checked={frontSlow} /> Slow</label>
            <label class="flex items-center gap-2"><input type="checkbox" bind:checked={frontSilence} /> Silence</label>
            <label class="flex items-center gap-2"><input type="checkbox" bind:checked={frontDisarm} /> Disarm</label>
          </div>
          <label class="grid gap-1">
            <span>Artist (override)</span>
            <input class="p-2 border rounded" bind:value={frontArtist} placeholder="defaults to Artist" />
          </label>
          <label class="col-span-2 grid gap-1">
            <span>Rules Text (override)</span>
            <textarea class="p-2 border rounded" bind:value={frontRules} rows="3"></textarea>
          </label>
          <label class="col-span-2 grid gap-1">
            <span>Flavor Text (override)</span>
            <textarea class="p-2 border rounded" bind:value={frontFlavor} rows="2"></textarea>
          </label>
          <label class="grid gap-1">
            <span>Image Full URL</span>
            <input class="p-2 border rounded" bind:value={frontFull} />
          </label>
          <label class="grid gap-1">
            <span>Image Large URL</span>
            <input class="p-2 border rounded" bind:value={frontLarge} />
          </label>
          <label class="grid gap-1">
            <span>Image Normal URL</span>
            <input class="p-2 border rounded" bind:value={frontNormal} />
          </label>
          <label class="grid gap-1">
            <span>Image Small URL</span>
            <input class="p-2 border rounded" bind:value={frontSmall} />
          </label>
        </div>
        {#if frontFull || frontLarge || frontNormal || frontSmall}
          <div class="mt-2">
            <div class="text-sm text-muted-foreground mb-1">Front Image Preview</div>
            <img class="max-h-80 rounded border" src={(frontNormal || frontLarge || frontSmall || frontFull)} alt="Front preview" />
          </div>
        {/if}
      </fieldset>

      <fieldset class="border rounded p-3 space-y-3">
        <legend class="px-1 text-sm font-semibold">Back Face</legend>
        <label class="flex items-center gap-2"><input type="checkbox" bind:checked={hasBackFace} /> Enable Back Face</label>
        {#if hasBackFace}
          <div class="grid grid-cols-2 gap-3">
            <label class="grid gap-1">
              <span>Name</span>
              <input class="p-2 border rounded" bind:value={backName} />
            </label>
            <div class="grid grid-cols-2 gap-3 col-span-2">
              <label class="grid gap-1">
                <span>Action Cost</span>
                <input class="p-2 border rounded" bind:value={backActionCost} type="number" />
              </label>
              <label class="grid gap-1">
                <span>Power Cost</span>
                <input class="p-2 border rounded" bind:value={backPowerCost} type="number" />
              </label>
              <label class="flex items-center gap-2"><input type="checkbox" bind:checked={backHeroic} /> Heroic</label>
              <label class="flex items-center gap-2"><input type="checkbox" bind:checked={backSlow} /> Slow</label>
              <label class="flex items-center gap-2"><input type="checkbox" bind:checked={backSilence} /> Silence</label>
              <label class="flex items-center gap-2"><input type="checkbox" bind:checked={backDisarm} /> Disarm</label>
            </div>
            <label class="grid gap-1">
              <span>Artist</span>
              <input class="p-2 border rounded" bind:value={backArtist} />
            </label>
            <label class="col-span-2 grid gap-1">
              <span>Rules Text</span>
              <textarea class="p-2 border rounded" bind:value={backRules} rows="3"></textarea>
            </label>
            <label class="col-span-2 grid gap-1">
              <span>Flavor Text</span>
              <textarea class="p-2 border rounded" bind:value={backFlavor} rows="2"></textarea>
            </label>
            <label class="grid gap-1">
              <span>Image Full URL</span>
              <input class="p-2 border rounded" bind:value={backFull} />
            </label>
            <label class="grid gap-1">
              <span>Image Large URL</span>
              <input class="p-2 border rounded" bind:value={backLarge} />
            </label>
            <label class="grid gap-1">
              <span>Image Normal URL</span>
              <input class="p-2 border rounded" bind:value={backNormal} />
            </label>
            <label class="grid gap-1">
              <span>Image Small URL</span>
              <input class="p-2 border rounded" bind:value={backSmall} />
            </label>
          </div>
          {#if backFull || backLarge || backNormal || backSmall}
            <div class="mt-2">
              <div class="text-sm text-muted-foreground mb-1">Back Image Preview</div>
              <img class="max-h-80 rounded border" src={(backNormal || backLarge || backSmall || backFull)} alt="Back preview" />
            </div>
          {/if}
          <div class="rounded border p-3 bg-muted/30 mt-2">
            <div class="text-sm font-medium mb-1">Back Rules Preview</div>
            <div class="prose text-sm">
              <ParsedCardText source={backRules} />
            </div>
          </div>
        {/if}
      </fieldset>

      <div class="flex gap-2">
        <Button type="submit" disabled={saving}>Save</Button>
        <a class="underline self-center" href="/admin/cards">Cancel</a>
      </div>
    </form>
  {/if}
</div>
