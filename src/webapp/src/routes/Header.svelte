<script lang="ts">
	import { page } from '$app/state';
	import { NewDeckDialog } from '$lib/components/NewDeck';
	import HerocraftWordmark from '$lib/images/herocraft.svelte';
	import type { NewDeckSchema } from '$lib/components/NewDeck/schema.ts';
	import type { Infer, SuperValidated } from 'sveltekit-superforms';
	import type { HTMLAttributes } from 'svelte/elements';

	import * as NavigationMenu from '$lib/components/ui/navigation-menu/index.js';
	import { navigationMenuTriggerStyle } from '$lib/components/ui/navigation-menu/navigation-menu-trigger.svelte';

	import * as DropdownMenu from '$lib/components/ui/dropdown-menu/index.ts';
	import * as Command from '$lib/components/ui/command/index.js';
	import CommandPalette from '$lib/components/command/CommandPalette.svelte';
	import { Menu, Search } from 'lucide-svelte';
	import { cn } from '$lib/utils.ts';
	import { Button } from '$lib/components/ui/button';

	interface Props {
		showSearch: boolean;
		deckForm: SuperValidated<Infer<NewDeckSchema>>;
	}

	let { showSearch, deckForm = $bindable() }: Props = $props();

	let { session } = $derived(page.data);
	let search: string = $state('');

	type ListItemProps = HTMLAttributes<HTMLAnchorElement> & {
		title: string;
		href: string;
		content: string;
	};

	let open = $state(false);
	let newDeckOpen = $state(false);

	function isTypingInEditable(target: EventTarget | null): boolean {
		const el = target as HTMLElement | null;
		if (!el) return false;
		const tag = el.tagName?.toLowerCase();
		if (tag === 'input' || tag === 'textarea') return true;
		if (el.isContentEditable) return true;
		return false;
	}

	function handleKeydown(e: KeyboardEvent) {
		const isMetaK = (e.key.toLowerCase() === 'k' && (e.ctrlKey || e.metaKey));
		if (isMetaK) {
			e.preventDefault();
			open = true;
			return;
		}
		if (e.key === '/') {
			if (isTypingInEditable(e.target)) return; // don't hijack typing
			e.preventDefault();
			open = !open;
		}
	}

</script>

<svelte:document onkeydown={handleKeydown} />

{#snippet ListItem({
										 title,
										 content,
										 href,
										 class: className,
										 ...restProps
									 }: ListItemProps)}
	<li>
		<NavigationMenu.Link>
			{#snippet child()}
				<a
					{href}
					class={cn(
            "hover:bg-accent hover:text-accent-foreground focus:bg-accent focus:text-accent-foreground block select-none space-y-1 rounded-md p-3 leading-none no-underline outline-none transition-colors",
            className
          )}
					{...restProps}
				>
					<div class="text-sm font-medium leading-none">{title}</div>
					<p class="text-muted-foreground line-clamp-2 text-sm leading-snug">
						{content}
					</p>
				</a>
			{/snippet}
		</NavigationMenu.Link>
	</li>
{/snippet}


<NavigationMenu.Root
	class="container py-2 mx-auto px-6 md:px-8 grid h-16 grid-cols-[auto_1fr_minmax(360px,640px)_1fr_auto] items-center gap-2">
	<div>
		<ul>
			<li
				class="h-8 w-28 pt-1 hover:fill-red-600 hover:color-red-600"
				aria-current={page.url.pathname === '/' ? 'page' : undefined}
			>
				<a class="hover:fill-red-600 hover:color-red-600" href="/">
					<HerocraftWordmark />
				</a>
			</li>
		</ul>
	</div>
	<div class="justify-self-start">
		<NavigationMenu.List class="justify-start">
   <NavigationMenu.Item>
        <NavigationMenu.Trigger>Decks</NavigationMenu.Trigger>
				<NavigationMenu.Content>
					<ul class="grid gap-2 p-2 md:w-[400px] lg:w-[500px] lg:grid-cols-[.75fr_1fr]">
						<li class="row-span-3">
							<NavigationMenu.Link
								class="from-muted/50 to-muted bg-linear-to-b outline-hidden flex h-full w-full select-none flex-col justify-end rounded-md p-6 no-underline focus:shadow-md"
							>
								{#snippet child({ props })}
									<a {...props} href="/decks/public">
										<div class="mb-2 mt-4 text-lg font-medium">Decks</div>
										<p class="text-muted-foreground text-sm leading-tight">
											View the latest and greatest decks from the community.
										</p>
									</a>
								{/snippet}
							</NavigationMenu.Link>
						</li>
						{@render ListItem({
							href: "/decks/public",
							title: "Preconstructed Decks",
							content: "A great place to start if you're new to Ivion."
						})}
						{@render ListItem({
							href: "/decks/private",
							title: "Your Decks",
							content: "View and Manage the decks you've created."
						})}
						{@render ListItem({
							href: "/decks/liked",
							title: "Decks You Liked",
							content: "See what you've liked and share them with others."
						})}
					</ul>
				</NavigationMenu.Content>
			</NavigationMenu.Item>
			<NavigationMenu.Item>
				<NavigationMenu.Link>
					{#snippet child()}
						<a href="/cards" class={navigationMenuTriggerStyle()}>Cards</a>
					{/snippet}
				</NavigationMenu.Link>
			</NavigationMenu.Item>
			<NavigationMenu.Item>
				<NavigationMenu.Link>
					{#snippet child()}
						<a href="/docs/syntax" class={navigationMenuTriggerStyle()}>Syntax</a>
					{/snippet}
				</NavigationMenu.Link>
      </NavigationMenu.Item>
      <NavigationMenu.Item>
        <a class={navigationMenuTriggerStyle()} href="/admin/cards">Admin</a>
      </NavigationMenu.Item>
    </NavigationMenu.List>
  </div>
	<!-- Center command trigger pill -->
	<div class="justify-self-center">
		<button
			class="rounded-full border border-input bg-background px-5 py-2.5 text-sm text-muted-foreground hover:bg-accent hover:text-accent-foreground focus:outline-none focus:ring-2 focus:ring-ring/30 inline-flex items-center gap-2"
			aria-haspopup="dialog"
			aria-expanded={open}
			on:click={() => (open = true)}
		>
			<Search class="h-4 w-4" />
			<span class="hidden sm:inline">Search Cards, Decks, and Commands</span>
			<kbd
				class="ml-2 hidden md:inline-flex h-5 select-none items-center gap-1 rounded border bg-muted px-1.5 font-mono text-[10px] font-medium text-muted-foreground">
				/
			</kbd>
		</button>
	</div>
	<!-- Spacer column -->
	<div></div>
	<!-- Keep the palette mounted nearby so focus returns properly -->
	<CommandPalette bind:open onNewDeck={() => { newDeckOpen = true; }} />
	<NewDeckDialog form={deckForm} bind:open={newDeckOpen} />

	<div class="flex-none">
		{#if !session || !session.isAuthenticated}
			<div class="flex items-center gap-2">
				<Button href="/account/signin" variant="outline" size="sm" class="rounded-full">Sign In</Button>
				<Button href="/account/register" variant="default" size="sm" class="rounded-full">Register</Button>
			</div>
		{:else}
			<ul>
				<li>
					<DropdownMenu.Root>
						<DropdownMenu.Trigger
							class="uppercase font-medium h-full text-sm py-2 tracking-widest test "
						>
							<Menu />
						</DropdownMenu.Trigger>
						<DropdownMenu.Content>
							<DropdownMenu.Group>
								<DropdownMenu.GroupHeading>Your Stuff</DropdownMenu.GroupHeading>
								<a href="/decks/personal">
									<DropdownMenu.Item>Decks</DropdownMenu.Item>
								</a>
								<DropdownMenu.Separator />
							</DropdownMenu.Group>
							<DropdownMenu.Group>
								<DropdownMenu.GroupHeading>Profile & Settings</DropdownMenu.GroupHeading>
								<a href="/account">
									<DropdownMenu.Item>Profile</DropdownMenu.Item>
								</a>
								<a href="/account/settings">
									<DropdownMenu.Item>Settings</DropdownMenu.Item>
								</a>
							</DropdownMenu.Group>
							<!--									<DropdownMenu.Group>-->
							<!--										<DropdownMenu.GroupHeading>Utilities</DropdownMenu.GroupHeading>-->
							<!--										&lt;!&ndash; Placeholder theme toggle item; wire to theme system later &ndash;&gt;-->
							<!--										<DropdownMenu.Item on:click={() => { /* TODO: theme toggle */ }}>-->
							<!--											Toggle Theme-->
							<!--										</DropdownMenu.Item>-->
							<!--										<a href="/notifications">-->
							<!--											<DropdownMenu.Item>Notifications</DropdownMenu.Item>-->
							<!--										</a>-->
							<!--									</DropdownMenu.Group>-->
							<DropdownMenu.Group>
								<DropdownMenu.Separator />
								<a href="/account/logout"
									 data-sveltekit-preload-data={false}
									 data-sveltekit-reload
								>
									<DropdownMenu.Item>Logout</DropdownMenu.Item>
								</a>
							</DropdownMenu.Group>
						</DropdownMenu.Content>
					</DropdownMenu.Root>
				</li>
			</ul>
		{/if}
	</div>

</NavigationMenu.Root>

