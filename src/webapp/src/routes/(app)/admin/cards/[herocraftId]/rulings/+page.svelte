<script lang="ts">
  import type { PageData } from './$types';

  interface Props { data: PageData }
  let { data }: Props = $props();

  let herocraftId: string = data.herocraftId;
  // Prefer showing the human-readable card name if available from the server load
  let cardName: string | undefined = (data as any).cardName;
  let rulings: any[] = $state(data.rulings ?? []);

  // Simple local form state
  function toLocalDateTimeInputString(d: Date): string {
    // Format as yyyy-MM-ddTHH:mm for input[type=datetime-local] (no timezone, no seconds)
    const pad = (n: number) => n.toString().padStart(2, '0');
    const year = d.getFullYear();
    const month = pad(d.getMonth() + 1);
    const day = pad(d.getDate());
    const hours = pad(d.getHours());
    const minutes = pad(d.getMinutes());
    return `${year}-${month}-${day}T${hours}:${minutes}`;
  }

  let form = $state({
    source: 'herocraft',
    sourceUrl: '',
    // Important: input[type=datetime-local] requires a local date-time string without timezone.
    // We still convert to an ISO instant (UTC) when sending to the backend.
    publishedAt: toLocalDateTimeInputString(new Date()),
    style: 'RULING',
    comment: '',
    question: '',
    answer: ''
  });

  const createRuling = async () => {
    const fd = new FormData();
    fd.set('cardHerocraftId', herocraftId);
    fd.set('source', form.source);
    if (form.sourceUrl) fd.set('sourceUrl', form.sourceUrl);
    // The datetime-local input returns a value without timezone. Normalize to ISO instant (UTC)
    try {
      const iso = form.publishedAt && !/Z$/.test(form.publishedAt)
        ? new Date(form.publishedAt).toISOString()
        : new Date(form.publishedAt).toISOString();
      fd.set('publishedAt', iso);
    } catch (_) {
      // Fallback to now if parsing fails
      fd.set('publishedAt', new Date().toISOString());
    }
    fd.set('style', form.style);
    if (form.comment) fd.set('comment', form.comment);
    if (form.question) fd.set('question', form.question);
    if (form.answer) fd.set('answer', form.answer);

    const res = await fetch('?/create', { method: 'POST', body: fd });
    // Treat any 2xx as success; SvelteKit actions may wrap JSON like { type: 'success', ... }
    // We still attempt to parse for potential error messages, but don't require a specific shape.
    const result = await res.json().catch(() => ({}));
    if (res.ok || result?.success === true || result?.type === 'success') {
      // reload data
      location.reload();
    } else {
      const msg = result?.error || (typeof result === 'string' ? result : undefined) || 'Failed to create ruling';
      alert(msg);
    }
  };

  const deleteRuling = async (id: string) => {
    if (!confirm('Delete this ruling?')) return;
    const fd = new FormData();
    fd.set('id', id);
    const res = await fetch('?/delete', { method: 'POST', body: fd });
    const result = await res.json().catch(() => ({}));
    if (res.ok || result?.success === true || result?.type === 'success') {
      rulings = rulings.filter((r) => r.id !== id);
    } else {
      const msg = result?.error || (typeof result === 'string' ? result : undefined) || 'Failed to delete ruling';
      alert(msg);
    }
  };
</script>

<svelte:head>
  <title>Admin · Rulings · {cardName ?? herocraftId}</title>
  <meta name="robots" content="noindex" />
</svelte:head>

<div class="mx-auto max-w-5xl p-4">
  <h1 class="text-2xl font-semibold mb-4">Rulings for {cardName ?? herocraftId}</h1>

  <section class="mb-8 p-4 border rounded-lg bg-white">
    <h2 class="text-lg font-medium mb-3">Create New Ruling</h2>
    <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
      <div>
        <label class="block text-sm text-gray-600 mb-1">Source</label>
        <select class="w-full border rounded px-2 py-1" bind:value={form.source}>
          <option value="discord">discord</option>
          <option value="luminary">luminary</option>
          <option value="herocraft">herocraft</option>
        </select>
      </div>
      <div>
        <label class="block text-sm text-gray-600 mb-1">Source URL (optional)</label>
        <input class="w-full border rounded px-2 py-1" type="url" bind:value={form.sourceUrl} />
      </div>
      <div>
        <label class="block text-sm text-gray-600 mb-1">Published At</label>
        <input class="w-full border rounded px-2 py-1" type="datetime-local" bind:value={form.publishedAt} />
      </div>
      <div>
        <label class="block text-sm text-gray-600 mb-1">Style</label>
        <select class="w-full border rounded px-2 py-1" bind:value={form.style}>
          <option value="RULING">RULING</option>
          <option value="QA">QA</option>
        </select>
      </div>
      {#if form.style === 'RULING'}
        <div class="md:col-span-2">
          <label class="block text-sm text-gray-600 mb-1">Comment (plaintext)</label>
          <textarea class="w-full border rounded px-2 py-1" rows={4} bind:value={form.comment} />
        </div>
      {:else}
        <div>
          <label class="block text-sm text-gray-600 mb-1">Question (plaintext)</label>
          <textarea class="w-full border rounded px-2 py-1" rows={3} bind:value={form.question} />
        </div>
        <div>
          <label class="block text-sm text-gray-600 mb-1">Answer (plaintext)</label>
          <textarea class="w-full border rounded px-2 py-1" rows={3} bind:value={form.answer} />
        </div>
      {/if}
    </div>
    <div class="mt-3">
      <button class="px-3 py-1.5 bg-blue-600 text-white rounded" on:click={createRuling}>Create</button>
    </div>
  </section>

  <section class="p-4 border rounded-lg bg-white">
    <h2 class="text-lg font-medium mb-3">Existing Rulings</h2>
    {#if rulings?.length}
      <ul class="divide-y">
        {#each rulings as r}
          <li class="py-3 flex gap-3 items-start">
            <div class="grow">
              <div class="text-sm text-gray-500 flex gap-2 items-center">
                <span class="inline-block px-2 py-0.5 rounded-full bg-gray-100 border text-gray-700 capitalize">{r.source}</span>
                {#if r.sourceUrl}
                  <a class="text-blue-600 hover:underline" href={r.sourceUrl} target="_blank" rel="noopener noreferrer">source</a>
                {/if}
                {#if r.publishedAt}
                  <span>{new Date(r.publishedAt).toLocaleString()}</span>
                {/if}
                <span class="ml-auto text-xs uppercase tracking-wide text-gray-400">{r.style}</span>
              </div>
              {#if r.style === 'RULING'}
                <p class="mt-1 whitespace-pre-wrap">{r.comment}</p>
              {:else}
                <div class="mt-1">
                  {#if r.question}<div><b>Q:</b> <span class="whitespace-pre-wrap">{r.question}</span></div>{/if}
                  {#if r.answer}<div><b>A:</b> <span class="whitespace-pre-wrap">{r.answer}</span></div>{/if}
                </div>
              {/if}
            </div>
            <div class="shrink-0">
              <button class="px-2 py-1 text-red-600 border-red-600 border rounded" on:click={() => deleteRuling(r.id)}>Delete</button>
            </div>
          </li>
        {/each}
      </ul>
    {:else}
      <p class="text-sm text-gray-500">No rulings yet.</p>
    {/if}
  </section>
</div>
