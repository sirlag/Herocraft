<script lang="ts">
	import type { PageData } from './$types';

	export let data: PageData;

	let user = data.user;
</script>

{user.email}
{user.verified}

