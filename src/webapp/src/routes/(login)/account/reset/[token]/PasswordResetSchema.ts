import { z } from 'zod';

export const resetPasswordSchema = z.object({
	password: z.string().min(4),
	confirmPassword: z.string().min(4)
}).superRefine(({ password, confirmPassword }, ctx) => {
	if (confirmPassword != password) {
		ctx.addIssue({
			code: "custom",
			message: "Passwords do not match",
			path: ['confirmPassword']
		})
	}
})

export type ResetPasswordSchema = typeof resetPasswordSchema;