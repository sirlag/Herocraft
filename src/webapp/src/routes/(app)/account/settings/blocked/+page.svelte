<script lang="ts">
  import { goto } from '$app/navigation';
  function back() { goto('/account/settings'); }
</script>

<div class="container mx-auto px-4 py-6 space-y-6">
  <div class="flex items-center justify-between">
    <h1 class="text-2xl font-semibold">Blocked Users</h1>
    <button class="px-3 py-2 rounded border" on:click={back}>Back</button>
  </div>

  <div class="rounded-lg border p-6">
    <p class="opacity-70">Coming soon</p>
  </div>
</div>
