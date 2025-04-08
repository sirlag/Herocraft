import { z } from "zod";

export const forgotSchema = z.object({
	email: z.string().email(),
})

export type ForgotSchema = typeof forgotSchema;