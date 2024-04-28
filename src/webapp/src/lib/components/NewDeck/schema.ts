import { z } from "zod";

export const formSchema = z.object( {
	name: z.string({
		required_error: "Name is required",
	}),
	format: z.enum(["constructed", "paragon", "other"], {
		required_error: "You must select a format"
	}),
	visibility: z.enum(["public", "unlisted", "private"], {
		required_error: "You must select a Visibility"
	})
})

export type FormSchema = typeof formSchema;