import { z } from 'zod';

export const formSchema = z.object({
	username: z.string(),
	email: z.string().email(),
	password: z.string().min(4),
	confirmPassword: z.string().min(4),
	agree: z.boolean()
}).superRefine(({ password, confirmPassword }, ctx) => {
	if (confirmPassword != password) {
		ctx.addIssue({
			code: "custom",
			message: "Passwords do not match",
			path: ['confirmPassword']
		})
	}
});

export type FormSchema = typeof formSchema;
