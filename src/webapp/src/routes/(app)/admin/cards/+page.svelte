<script lang="ts">
  import { onMount } from 'svelte';
  import { Button } from '$lib/components/ui/button';
  import { API_BASE_URL } from '$lib/routes';

  type Uuid = string;
  type IvionCard = {
    id: Uuid;
    collectorsNumber: string | null;
    format: string | null;
    name: string;
    archetype: string | null;
    actionCost: number | null;
    powerCost: number | null;
    range: number | null;
    health: number | null;
    heroic: boolean;
    slow: boolean;
    silence: boolean;
    disarm: boolean;
    extraType: string | null;
    rulesText: string | null;
    flavorText: string | null;
    artist: string;
    ivionUUID: Uuid;
    secondUUID: Uuid | null;
    colorPip1: string | null;
    colorPip2: string | null;
    season: string;
    type: string | null;
  };

  let cards: IvionCard[] = $state([]);
  let loading = $state(true);
  let error: string | null = $state(null);

  async function loadCards() {
    loading = true;
    error = null;
    try {
      const res = await fetch(`${API_BASE_URL}/admin/cards`);
      if (!res.ok) throw new Error(`Failed to load cards: ${res.status}`);
      cards = await res.json();
    } catch (e) {
      error = (e as Error).message;
    } finally {
      loading = false;
    }
  }

  async function deleteCard(id: string) {
    if (!confirm('Delete this card?')) return;
    const res = await fetch(`${API_BASE_URL}/admin/cards/${id}`, { method: 'DELETE' });
    if (res.ok) {
      cards = cards.filter((c) => c.id !== id);
    } else {
      alert('Failed to delete card');
    }
  }

  onMount(loadCards);
</script>

<div class="container mx-auto p-4 space-y-4">
  <div class="flex items-center justify-between">
    <h1 class="text-2xl font-semibold">Admin • Cards</h1>
    <a href="/admin/cards/new"><Button>Create Card</Button></a>
  </div>

  {#if loading}
    <p>Loading…</p>
  {:else if error}
    <p class="text-red-600">{error}</p>
  {:else}
    <div class="overflow-x-auto">
      <table class="min-w-full text-sm">
        <thead>
          <tr class="text-left border-b">
            <th class="p-2">Name</th>
            <th class="p-2">Format</th>
            <th class="p-2">Archetype</th>
            <th class="p-2">Season</th>
            <th class="p-2">Actions</th>
          </tr>
        </thead>
        <tbody>
          {#each cards as c}
            <tr class="border-b hover:bg-accent/50">
              <td class="p-2">{c.name}</td>
              <td class="p-2">{c.format}</td>
              <td class="p-2">{c.archetype}</td>
              <td class="p-2">{c.season}</td>
              <td class="p-2 space-x-2">
                <a href={`/admin/cards/${c.id}`} class="underline">Edit</a>
                {#if c.ivionUUID}
                  <a href={`/admin/cards/${c.ivionUUID}/rulings`} class="underline">Rulings</a>
                {/if}
                <button class="text-red-600 underline" on:click={() => deleteCard(c.id)}>Delete</button>
              </td>
            </tr>
          {/each}
        </tbody>
      </table>
    </div>
  {/if}
</div>
