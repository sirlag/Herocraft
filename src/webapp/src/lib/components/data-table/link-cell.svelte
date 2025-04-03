<script lang="ts">
	interface Props {
		text: string;
		link: string;
	}

	let { text, link }: Props = $props();
</script>

<a href={link}>{text}</a>
