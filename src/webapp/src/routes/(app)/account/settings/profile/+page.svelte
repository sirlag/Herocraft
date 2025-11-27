<script lang="ts">
  import { onMount } from 'svelte';
  import { MeURLs } from '$lib/routes';

  let displayName = '';
  let loading = true;
  let saving = false;
  let message: string | null = null;
  let error: string | null = null;

  onMount(async () => {
    try {
      const res = await fetch(MeURLs.me, { credentials: 'include' });
      if (res.ok) {
        const data = await res.json();
        displayName = data.displayName ?? '';
      } else if (res.status === 401) {
        // redirect to login
        window.location.href = '/account/signin';
        return;
      }
    } catch (e) {
      console.error(e);
      error = 'Failed to load profile.';
    } finally {
      loading = false;
    }
  });

  async function submit(e: Event) {
    e.preventDefault();
    error = null;
    message = null;

    const trimmed = displayName.trim();
    if (trimmed.length < 1 || trimmed.length > 32) {
      error = 'Display name must be 1–32 characters.';
      return;
    }

    saving = true;
    try {
      const res = await fetch(MeURLs.updateProfile, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ displayName: trimmed })
      });
      if (res.ok) {
        const data = await res.json();
        displayName = data.displayName ?? trimmed;
        message = 'Profile updated';
      } else {
        const data = await res.json().catch(() => undefined);
        error = data?.message ?? 'Failed to update profile';
      }
    } catch (e) {
      console.error(e);
      error = 'Failed to update profile.';
    } finally {
      saving = false;
    }
  }
</script>

<div class="container mx-auto px-4 py-6 space-y-6">
  <h1 class="text-2xl font-semibold">Profile</h1>

  {#if loading}
    <p>Loading…</p>
  {:else}
    <form class="space-y-4 max-w-md" on:submit|preventDefault={submit}>
      <div class="flex flex-col gap-1">
        <label class="text-sm" for="displayName">Display name</label>
        <input
          id="displayName"
          class="border rounded px-3 py-2 bg-transparent"
          bind:value={displayName}
          maxlength={32}
          autocomplete="off"
        />
      </div>
      <button class="btn btn-primary px-3 py-2 rounded bg-blue-600 text-white disabled:opacity-50" disabled={saving}>
        {saving ? 'Saving…' : 'Save changes'}
      </button>

      {#if message}
        <p class="text-green-600">{message}</p>
      {/if}
      {#if error}
        <p class="text-red-600">{error}</p>
      {/if}
    </form>
  {/if}
</div>
