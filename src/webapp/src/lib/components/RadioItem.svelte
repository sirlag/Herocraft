<script lang="ts">
	import * as Form from '$lib/components/ui/form';
	import * as RadioGroup from '$lib/components/ui/radio-group';

	export let value;
	export let text;
</script>

<div class="border rounded-lg px-3 py-2 flex items-center space-x-3 space-y-0">
	<Form.Control let:attrs>
		<RadioGroup.Item {value} {...attrs} />
		<Form.Label class="font-normal">{text}</Form.Label>
	</Form.Control>
</div>
