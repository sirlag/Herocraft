<script lang="ts">
  import { goto } from '$app/navigation';
  import { MeURLs } from '$lib/routes';

  function back() {
    goto('/account/settings');
  }

  let currentPassword = '';
  let newPassword = '';
  let confirm = '';
  let saving = false;
  let message: string | null = null;
  let error: string | null = null;

  async function changePassword(e: Event) {
    e.preventDefault();
    message = null;
    error = null;

    const curr = currentPassword.trim();
    const next = newPassword.trim();
    const conf = confirm.trim();

    if (!curr || !next) {
      error = 'Both current and new password are required.';
      return;
    }
    if (next !== conf) {
      error = 'Passwords do not match.';
      return;
    }

    saving = true;
    try {
      const res = await fetch(MeURLs.changePassword, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ currentPassword: curr, newPassword: next })
      });
      if (res.status === 204) {
        message = 'Password updated successfully';
        currentPassword = '';
        newPassword = '';
        confirm = '';
      } else {
        const data = await res.json().catch(() => undefined);
        error = data?.message ?? 'Failed to change password.';
      }
    } catch (err) {
      console.error(err);
      error = 'Failed to change password.';
    } finally {
      saving = false;
    }
  }
</script>

<div class="container mx-auto px-4 py-6 space-y-6">
  <div class="flex items-center justify-between">
    <h1 class="text-2xl font-semibold">Login & Security</h1>
    <button class="px-3 py-2 rounded border" onclick={back}>Back</button>
  </div>

  <div class="space-y-6 max-w-2xl">
    <section class="rounded-lg border p-4">
      <h2 class="text-lg font-medium mb-2">Username</h2>
      <p class="text-sm opacity-80 mb-4">Change your username. Weekly change limit applies.</p>
      <div class="text-sm opacity-60">Coming soon</div>
    </section>

    <section class="rounded-lg border p-4">
      <h2 class="text-lg font-medium mb-2">Email</h2>
      <p class="text-sm opacity-80 mb-4">Update your account email. Verification required.</p>
      <div class="text-sm opacity-60">Coming soon</div>
    </section>

    <section class="rounded-lg border p-4">
      <h2 class="text-lg font-medium mb-2">Password</h2>
      <p class="text-sm opacity-80 mb-4">Change your password. Current password required.</p>

      <form class="space-y-4" on:submit|preventDefault={changePassword}>
        <div class="flex flex-col gap-1">
          <label class="text-sm" for="currentPassword">Current password</label>
          <input id="currentPassword" type="password" class="border rounded px-3 py-2 bg-transparent" bind:value={currentPassword} autocomplete="current-password" />
        </div>
        <div class="flex flex-col gap-1">
          <label class="text-sm" for="newPassword">New password</label>
          <input id="newPassword" type="password" class="border rounded px-3 py-2 bg-transparent" bind:value={newPassword} autocomplete="new-password" />
        </div>
        <div class="flex flex-col gap-1">
          <label class="text-sm" for="confirm">Confirm new password</label>
          <input id="confirm" type="password" class="border rounded px-3 py-2 bg-transparent" bind:value={confirm} autocomplete="new-password" />
        </div>
        <button class="btn btn-primary px-3 py-2 rounded bg-blue-600 text-white disabled:opacity-50" disabled={saving}>
          {saving ? 'Saving…' : 'Change password'}
        </button>
        {#if message}
          <p class="text-green-600">{message}</p>
        {/if}
        {#if error}
          <p class="text-red-600">{error}</p>
        {/if}
      </form>
    </section>
  </div>
</div>
