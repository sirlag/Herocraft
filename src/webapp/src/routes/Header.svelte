<script lang="ts">
	import { page } from '$app/state';
	import { NewDeckDialog } from '$lib/components/NewDeck';
	import { Input } from '$lib/components/ui/input';
	import HerocraftWordmark from '$lib/images/herocraft.svelte';
	import type { NewDeckSchema } from '$lib/components/NewDeck/schema.ts';
	import type { Infer, SuperValidated } from 'sveltekit-superforms';

	import * as DropdownMenu from '$lib/components/ui/dropdown-menu/index.ts';
	import { Menu } from 'lucide-svelte';

	interface Props {
		showSearch: boolean;
		deckForm: SuperValidated<Infer<NewDeckSchema>>;
	}

	let { showSearch, deckForm = $bindable() }: Props = $props();

	let { session } = $derived(page.data);
	let search: string = $state('');
</script>

<nav class="flex w-full bg-blue-700">
	<div class="container">
		<div>
			<ul>
				<li
					class="h-8 w-28 pt-1 hover:fill-red-600"
					aria-current={page.url.pathname === '/' ? 'page' : undefined}
				>
					<a href="/">
						<HerocraftWordmark />
					</a>
				</li>
			</ul>
		</div>
		{#if showSearch}
			<div class="w-full h-10">
				<form method="GET" action="/cards">
					<Input name="q" bind:value={search} class="w-full" />
					<button type="submit" class="visually-hidden"></button>
				</form>
			</div>
		{/if}
		{#if session && session.isAuthenticated}
			<div>
				<ul>
					<li>
						<a href="/decks/personal">Your Decks</a>
					</li>
					<li>
						<NewDeckDialog form={deckForm}>
							<!-- svelte-ignore a11y_missing_attribute -->
							<a>New Deck</a>
						</NewDeckDialog>
					</li>
				</ul>
			</div>
		{/if}
		<div>
			{#if !session || !session.isAuthenticated}
				<ul>
					<li>
						<a href="/account/signin">Sign In</a>
					</li>
					<li>
						<a href="/account/register">Register</a>
					</li>
				</ul>
			{:else}
				<ul>
					<li>
						<!--						    nav a {
										padding: 0 0.5rem;
										color: var(--color-text);
										font-weight: 700;
										font-size: 0.8rem;
										text-transform: uppercase;
										letter-spacing: 0.1em;
										text-decoration: none;
										transition: color 0.2s linear;
								}
						-->
						<DropdownMenu.Root>
							<DropdownMenu.Trigger
								class="uppercase font-medium h-full text-sm py-2 tracking-widest test "
							>
								<Menu />
							</DropdownMenu.Trigger>
							<DropdownMenu.Content>
								<DropdownMenu.Group>
									<DropdownMenu.GroupHeading>Your Stuff</DropdownMenu.GroupHeading>
									<a href="/decks/personal"><DropdownMenu.Item>Decks</DropdownMenu.Item></a>
									<DropdownMenu.Separator />
								</DropdownMenu.Group>
								<DropdownMenu.Group>
									<DropdownMenu.GroupHeading>Profile & Settings</DropdownMenu.GroupHeading>
									<a href="/account">
										<DropdownMenu.Item>Profile</DropdownMenu.Item>
									</a>
									<a href="/account">
										<DropdownMenu.Item>Settings</DropdownMenu.Item>
									</a>
								</DropdownMenu.Group>
								<DropdownMenu.Group>
									<DropdownMenu.Separator />
									<a href="/account/logout" data-sveltekit-preload-data={false}>
										<DropdownMenu.Item>Logout</DropdownMenu.Item>
									</a>
								</DropdownMenu.Group>
							</DropdownMenu.Content>
						</DropdownMenu.Root>
					</li>
				</ul>
			{/if}
		</div>
	</div>
</nav>

<style>
    .container {
        display: flex;
        flex-wrap: inherit;
        align-items: center;
        justify-content: space-between;
    }


    nav {
        display: flex;
        justify-content: center;
        width: 100%;
        background: rgba(255, 255, 255, 0.7);
    }

    ul {
        position: relative;
        padding: 0;
        margin: 0;
        height: 3em;
        display: flex;
        justify-content: space-around;
        align-items: center;
        list-style: none;
        background: var(--background);
        background-size: contain;
    }

    li {
        /*position: relative;*/
        height: 100%;
    }

    nav a {
        display: flex;
        height: 100%;
        align-items: center;
        padding: 0 0.5rem;
        color: var(--color-text);
        font-weight: 700;
        font-size: 0.8rem;
        text-transform: uppercase;
        letter-spacing: 0.1em;
        text-decoration: none;
        transition: color 0.2s linear;
    }

    .test {
        color: var(--color-text);
        transition: color 0.2s linear;

    }

    .test:hover {
        color: var(--color-theme-1);
    }

    a:hover {
        color: var(--color-theme-1);
        fill: var(--color-theme-1);
    }
</style>
