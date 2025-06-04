<script lang="ts">
	import * as Tooltip from '$lib/components/ui/tooltip';
	import { Button } from '$lib/components/ui/button';

	import type { SvelteComponent } from 'svelte';

	interface Props {
		Icon: SvelteComponent;
		count: number | null;
		label: string;
		action: ((state: boolean) => void) | null;
		hoverStyle: string;
		activeStyle: string;
		state: boolean;
	}

	let { Icon, count, label, state = false, action = null, hoverStyle = '', activeStyle = '' }: Props = $props();

</script>

{#if action}
	<Tooltip.Root>
		<Tooltip.Trigger>
			<Button variant="round-ghost" size={count ? "default" : "icon" } class="group" onclick={() => action(state)}>
				<span class="flex flex-row items-center gap-2">
					<Icon class={"w-4 h-4 " + hoverStyle + " " + (state ? activeStyle: "")} /> {count}
				</span>
			</Button>
		</Tooltip.Trigger>
		<Tooltip.Content side="bottom">
			{label}
		</Tooltip.Content>
	</Tooltip.Root>
{:else}
	<Tooltip.Root>
		<Tooltip.Trigger>
			<span class="flex flex-row items-center gap-2">
				<Icon class="w-4 h-4" /> {count}
			</span>
		</Tooltip.Trigger>
		<Tooltip.Content side="bottom">
			{label}
		</Tooltip.Content>
	</Tooltip.Root>
{/if}

