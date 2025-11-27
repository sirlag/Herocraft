<script lang="ts">
  import { goto } from '$app/navigation';

  function go(path: string) {
    goto(path);
  }
</script>

<div class="container mx-auto px-4 py-6 space-y-6">
  <h1 class="text-2xl font-semibold">Account Settings</h1>

  <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
    <div class="rounded-lg border p-4 shadow-sm bg-white/5">
      <h2 class="text-lg font-medium mb-2">Profile</h2>
      <p class="text-sm opacity-80 mb-4">Update your public display name.</p>
      <button class="btn btn-primary px-3 py-2 rounded bg-blue-600 text-white" onclick={() => go('/account/settings/profile')}>
        Edit profile
      </button>
    </div>

    <div class="rounded-lg border p-4 shadow-sm bg-white/5">
      <h2 class="text-lg font-medium mb-2">Login & Security</h2>
      <p class="text-sm opacity-80 mb-4">Manage username, email, and password.</p>
      <button class="btn btn-primary px-3 py-2 rounded bg-blue-600 text-white" onclick={() => go('/account/settings/security')}>
        Manage security
      </button>
    </div>

    <div class="rounded-lg border p-4 shadow-sm bg-white/5">
      <h2 class="text-lg font-medium mb-2">Blocked Users</h2>
      <p class="text-sm opacity-80 mb-4">View and manage blocked users.</p>
      <button class="btn px-3 py-2 rounded border" onclick={() => go('/account/settings/blocked')}>
        Open
      </button>
    </div>
  </div>
</div>
