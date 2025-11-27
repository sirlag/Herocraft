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
	import * as Sheet from '$lib/components/ui/sheet/index.ts';
	import * as Command from '$lib/components/ui/command/index.js';
	import CommandPalette from '$lib/components/command/CommandPalette.refactored.svelte';
	import { Menu, Search } from 'lucide-svelte';
	import { cn } from '$lib/utils.ts';
	import { Button } from '$lib/components/ui/button';

	interface Props {
		showSearch: boolean;
		deckForm: SuperValidated<Infer<NewDeckSchema>>;
	}

	let { showSearch, deckForm = $bindable() }: Props = $props();

 let { session, user } = $derived(page.data);
	let search: string = $state('');

	type ListItemProps = HTMLAttributes<HTMLAnchorElement> & {
		title: string;
		href: string;
		content: string;
	};

	let open = $state(false);
	let newDeckOpen = $state(false);
	let mobileMenuOpen = $state(false);

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


<!-- Mobile Header -->
<header class="md:hidden border-b">
	<div class="container mx-auto px-4 py-3 flex items-center justify-between gap-2">
		<!-- Mobile Menu Button -->
		<Sheet.Root bind:open={mobileMenuOpen}>
			<Sheet.Trigger asChild>
				{#snippet child({ props })}
					<Button {...props} variant="ghost" size="icon">
						<Menu class="h-5 w-5" />
						<span class="sr-only">Toggle menu</span>
					</Button>
				{/snippet}
			</Sheet.Trigger>
			<Sheet.Content side="left" class="w-[300px] sm:w-[400px]">
				<Sheet.Header>
					<Sheet.Title>Menu</Sheet.Title>
				</Sheet.Header>
				<nav class="flex flex-col gap-4 mt-6">
					<!-- Decks Section -->
					<div class="space-y-3">
						<h3 class="font-semibold text-sm text-muted-foreground px-2">Decks</h3>
						<a href="/decks/public" class="block px-2 py-2 rounded-md hover:bg-accent" onclick={() => mobileMenuOpen = false}>
							<div class="font-medium">Preconstructed Decks</div>
							<p class="text-xs text-muted-foreground">A great place to start if you're new to Ivion.</p>
						</a>
						<a href="/decks/private" class="block px-2 py-2 rounded-md hover:bg-accent" onclick={() => mobileMenuOpen = false}>
							<div class="font-medium">Your Decks</div>
							<p class="text-xs text-muted-foreground">View and Manage the decks you've created.</p>
						</a>
						<a href="/decks/liked" class="block px-2 py-2 rounded-md hover:bg-accent" onclick={() => mobileMenuOpen = false}>
							<div class="font-medium">Decks You Liked</div>
							<p class="text-xs text-muted-foreground">See what you've liked and share them with others.</p>
						</a>
					</div>

					<!-- Other Links -->
					<div class="space-y-2 pt-4 border-t">
						<a href="/cards" class="block px-2 py-2 rounded-md hover:bg-accent font-medium" onclick={() => mobileMenuOpen = false}>
							Cards
						</a>
						<a href="/docs/syntax" class="block px-2 py-2 rounded-md hover:bg-accent font-medium" onclick={() => mobileMenuOpen = false}>
							Syntax
						</a>
					</div>

					<!-- Account Section -->
					<div class="space-y-2 pt-4 border-t">
						{#if session?.isAuthenticated}
							<h3 class="font-semibold text-sm text-muted-foreground px-2">Your Account</h3>
							<a href="/decks/personal" class="block px-2 py-2 rounded-md hover:bg-accent" onclick={() => mobileMenuOpen = false}>
								Your Decks
							</a>
							<a href="/account" class="block px-2 py-2 rounded-md hover:bg-accent" onclick={() => mobileMenuOpen = false}>
								Profile
							</a>
							<a href="/account/settings" class="block px-2 py-2 rounded-md hover:bg-accent" onclick={() => mobileMenuOpen = false}>
								Settings
							</a>
							{#if user?.isAdmin}
								<a href="/admin/cards" class="block px-2 py-2 rounded-md hover:bg-accent" onclick={() => mobileMenuOpen = false}>
									Admin: Cards
								</a>
							{/if}
							<a href="/account/logout" class="block px-2 py-2 rounded-md hover:bg-accent text-destructive"
								data-sveltekit-preload-data={false}
								data-sveltekit-reload
								onclick={() => mobileMenuOpen = false}>
								Logout
							</a>
						{:else}
							<div class="flex flex-col gap-2 px-2">
								<Button href="/account/signin" variant="outline" class="w-full rounded-full" onclick={() => mobileMenuOpen = false}>
									Sign In
								</Button>
								<Button href="/account/register" variant="default" class="w-full rounded-full" onclick={() => mobileMenuOpen = false}>
									Register
								</Button>
							</div>
						{/if}
					</div>
				</nav>
			</Sheet.Content>
		</Sheet.Root>

		<!-- Logo -->
		<a href="/" class="flex-1 flex justify-center">
			<div class="h-6 w-20 hover:fill-red-600 hover:color-red-600">
				<HerocraftWordmark />
			</div>
		</a>

		<!-- Search Button (Mobile) -->
		<button
			type="button"
			class="inline-flex h-10 w-10 items-center justify-center rounded-md hover:bg-accent hover:text-accent-foreground"
			onclick={() => (open = true)}
			aria-label="Search"
		>
			<Search class="h-5 w-5" />
		</button>
	</div>
</header>

<!-- Desktop Header -->
<NavigationMenu.Root
	class="hidden md:block container py-2 mx-auto px-6 md:px-8">
	<div class="flex items-center justify-between gap-4">
		<!-- Logo -->
		<div class="flex-shrink-0">
			<a href="/" class="block h-8 w-28 pt-1 hover:fill-red-600 hover:color-red-600"
				aria-current={page.url.pathname === '/' ? 'page' : undefined}>
				<HerocraftWordmark />
			</a>
		</div>

		<!-- Desktop Navigation -->
		<div class="flex-shrink-0">
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
			</NavigationMenu.List>
		</div>

		<!-- Search Bar (Desktop) -->
		<div class="flex-1 max-w-2xl mx-auto">
			<button
				class="w-full rounded-full border border-input bg-background px-5 py-2.5 text-sm text-muted-foreground hover:bg-accent hover:text-accent-foreground focus:outline-none focus:ring-2 focus:ring-ring/30 inline-flex items-center gap-2 justify-center"
				aria-haspopup="dialog"
				aria-expanded={open}
				onclick={() => (open = true)}
			>
				<Search class="h-4 w-4" />
				<span class="hidden lg:inline">Search Cards, Decks, and Commands</span>
				<span class="lg:hidden">Search</span>
				<kbd
					class="ml-2 hidden md:inline-flex h-5 select-none items-center gap-1 rounded border bg-muted px-1.5 font-mono text-[10px] font-medium text-muted-foreground">
					/
				</kbd>
			</button>
		</div>

		<!-- User Menu (Desktop) -->
		<div class="flex-shrink-0">
			{#if !session || !session.isAuthenticated}
				<div class="flex items-center gap-2">
					<Button href="/account/signin" variant="outline" size="sm" class="rounded-full">Sign In</Button>
					<Button href="/account/register" variant="default" size="sm" class="rounded-full">Register</Button>
				</div>
			{:else}
				<DropdownMenu.Root>
					<DropdownMenu.Trigger
						class="uppercase font-medium h-full text-sm py-2 tracking-widest"
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
						{#if user?.isAdmin}
						<DropdownMenu.Group>
							<DropdownMenu.GroupHeading>Administration</DropdownMenu.GroupHeading>
							<a href="/admin/cards">
								<DropdownMenu.Item>Cards</DropdownMenu.Item>
							</a>
						</DropdownMenu.Group>
						{/if}
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
			{/if}
		</div>
	</div>
</NavigationMenu.Root>

<!-- Command Palette & Dialogs (Global - outside both mobile and desktop headers) -->
<CommandPalette bind:open onNewDeck={() => { newDeckOpen = true; }} />
<NewDeckDialog form={deckForm} bind:open={newDeckOpen} />
