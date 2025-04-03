<script lang="ts">
	import * as Form from '$lib/components/ui/form';
	import * as RadioGroup from '$lib/components/ui/radio-group';

	interface Props {
		value: any;
		text: any;
	}

	let { value, text }: Props = $props();
</script>

<div class="border rounded-lg px-3 py-2 flex items-center space-x-3 space-y-0">
	<Form.Control >
		{#snippet children({ attrs })}
				<RadioGroup.Item {value} {...attrs} />
			<Form.Label class="font-normal">{text}</Form.Label>
					{/snippet}
		</Form.Control>
</div>
