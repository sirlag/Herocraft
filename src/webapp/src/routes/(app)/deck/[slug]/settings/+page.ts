import type { PageLoad } from '../../../../../../.svelte-kit/types/src/routes';
import { superValidate } from 'sveltekit-superforms';
import { zod } from 'sveltekit-superforms/adapters';

import { DeckSettingsSchema } from '$lib/components/image/hero-image';

export const load: PageLoad = async ({ parent }) => {
	let settingsForm = await superValidate(zod(DeckSettingsSchema));

	let data = await parent();

	let { name, visibility, format, id } = data.deckList;

	let preFilled = {
		name: name,
		visibility: visibility.toLowerCase(),
		format: format.toLowerCase(),
		id: id
	};

	return {
		settingsForm,
		preFilled
	};
};
