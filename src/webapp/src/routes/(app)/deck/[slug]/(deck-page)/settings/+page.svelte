<script lang="ts">
	import { HeroImage } from '$lib/components/image/hero-image';
	import ShortHeader from '../../short-header.svelte';
	import type { PageData } from '../../../../../../../.svelte-kit/types/src/routes';
	import { DeckSettingsForm } from '$lib/components/deck-settings';
	import { Button } from '$lib/components/ui/button';
	import { enhance } from '$app/forms';

	export let data: PageData;

	$: ({ deckList: deck, settingsForm, preFilled } = data);
</script>

<ShortHeader page="Settings" deckList={deck} />

<div class="bg-neutral-50 h-full flex justify-center">
	<div class="max-w-7xl w-full h-full py-4 px-8">
		<div class="max-w-3xl">
			<DeckSettingsForm data={preFilled} />
			<div>
				<form action="/decks/personal?/delete" method="POST" use:enhance>
					<input name="id" value={deck.id} aria-hidden="true" class="visually-hidden hidden" />

					<Button type="submit" variant="destructive">Delete</Button>
				</form>
			</div>
		</div>
	</div>
</div>
