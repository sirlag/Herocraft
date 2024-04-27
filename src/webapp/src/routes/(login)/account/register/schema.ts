import { z } from "zod";

export const formSchema = z.object({
	username: z.string(),
	email: z.string().email(),
	password: z.string(),
	confirmPassword: z.string(),
	agree: z.boolean()
})

export type FormSchema = typeof formSchema;