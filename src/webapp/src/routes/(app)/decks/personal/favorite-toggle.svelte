<script lang="ts">
    import { Star } from 'lucide-svelte';
    import { DeckURLs } from '$lib/routes';
    import { invalidateAll } from '$app/navigation';

    export let id: string;
    export let favorite: boolean;

    async function toggleFavorite() {
        const method = favorite ? 'DELETE' : 'POST';
        
        try {
            const response = await fetch(DeckURLs.favorite(id), {
                method,
                credentials: 'include'
            });
            
            if (response.ok) {
                // Invalidate all data to refresh the page
                await invalidateAll();
            } else {
                console.error('Failed to toggle favorite status');
            }
        } catch (error) {
            console.error('Error toggling favorite status:', error);
        }
    }
</script>

<button 
    onclick={toggleFavorite} 
    class="focus:outline-none"
    aria-label={favorite ? "Remove from favorites" : "Add to favorites"}
>
    <Star 
        size={20} 
        class={favorite ? "fill-yellow-400 text-yellow-400" : "text-gray-300 hover:text-gray-400"} 
    />
</button>