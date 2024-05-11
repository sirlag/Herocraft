import { string, z } from 'zod';

export const formSchema = z.object({
	deckBuilderSave: z
		// .any(),
		// .array(z.custom<File>()),
		.custom<File>()
		.refine(
			(file) => file instanceof File,
			(file) => {
				return {
					message: `Expected a file, found ${JSON.stringify(file)}`
				};
			}
		),
	defaultVisibility: z.enum(['public', 'unlisted', 'private'], {
		required_error: 'You must select a Visibility'
	}),
	decks: z.array(z.string())
});

export type FormSchema = typeof formSchema;
