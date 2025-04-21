<script>
	let year = new Date().getFullYear();
</script>

<footer class="bg-neutral-900">
	<div class="min-w-[100vw] max-w-[100vw] pt-4 pb-12">
		<div class="flex flex-row space-x-3 justify-center text-neutral-300 text-base font-medium pb-6">
			<div>
				<h6 class="border-b font-thin">Cards</h6>
				<ul>
					<li>
						<a href="/cards">All cards</a>
					</li>
				</ul>
			</div>
			<div>
				<h6 class="border-b font-thin">Account</h6>
				<ul>
					<li>
						<a href="/account">Your Account</a>
					</li>
					<li>
						<a href="/account/register">Register</a>
					</li>
					<li>
						<a href="/about/terms">Terms of Service</a>
					</li>
					<li>
						<a href="/about/privacy">Privacy & Security</a>
					</li>
				</ul>
			</div>
			<div>
				<h6 class="border-b font-thin">More</h6>
				<ul>
					<li>
						<a href="https://discord.gg/U68czmeZy9">Discord</a>
					</li>
					<li>
						<a href="https://github.com/sirlag/Herocraft">Github</a>
					</li>
				</ul>
			</div>
		</div>
		<div class="container text-sm text-neutral-700">
			<p class="pb-2">Ivion and the Herocrafting system are trademarks of Luminary Games LLC</p>
			<p class="pb-2">
				Literal and graphical information presented on this site about Ivion, including card images
				and iconography, is copyright Luminary Games LLC. Herocraft is not affiliated with,
				endorsed, sponsored, or approved by Luminary Games LLC.
			</p>
			<p>
				All other content © {year} Herocraft
			</p>
		</div>
	</div>
</footer>

<style lang="postcss">
	footer {
		display: flex;
		flex-direction: column;
		justify-content: center;
		align-items: center;
		padding: 12px;
	}

	@media (min-width: 480px) {
		footer {
			padding: 12px 0;
		}
	}
</style>
