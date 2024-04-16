package app.herocraft.core.models

import java.util.UUID

class Deck(
    val id: UUID,
    val name: String,
    val list: List<IvionCard>
)