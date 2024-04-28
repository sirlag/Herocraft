<script lang="ts">
	import * as Form from '$lib/components/ui/form'
	import * as RadioGroup from '$lib/components/ui/radio-group'

	export let value
	export let text
</script>

	<Form.Control let:attrs>
		<div class="border rounded-lg px-3 py-2 flex items-center space-x-3 space-y-0" >
			<RadioGroup.Item value="{value}" {...attrs} />
			<Form.Label class="font-normal">{text}</Form.Label>
		</div>
	</Form.Control>
