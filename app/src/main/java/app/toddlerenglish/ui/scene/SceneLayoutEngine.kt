package app.toddlerenglish.ui.scene

import app.toddlerenglish.content.Placement
import app.toddlerenglish.content.SceneLayout
import app.toddlerenglish.content.Word
import kotlin.math.hypot

/** A word with its final spot in the scene, in 0..1 scene coordinates. */
data class PlacedWord(val word: Word, val x: Float, val y: Float, val size: Float)

/**
 * Turns scene data into positions. Pure Kotlin: no Android, fully unit-tested.
 *
 * - A word with a placement in the layout file goes there.
 * - A word without one goes to the first free auto slot, so adding a word to words.json is enough to
 *   make it appear (S3 "done when").
 * - Only words up to the parent's tier are shown, so first-word scenes stay calm.
 */
object SceneLayoutEngine {
    const val AUTO_SIZE = 0.15f
    private const val MIN_GAP = 0.12f

    /** Auto slots: a gentle grid over the lower, open part of a scene, centre first. */
    val autoSlots: List<Pair<Float, Float>> = buildList {
        val ys = listOf(0.60f, 0.78f, 0.42f, 0.90f, 0.25f)
        val xs = listOf(0.5f, 0.3f, 0.7f, 0.12f, 0.88f, 0.4f, 0.6f, 0.2f, 0.8f)
        for (y in ys) for (x in xs) add(x to y)
    }

    fun place(words: List<Word>, layout: SceneLayout, maxTier: Int): List<PlacedWord> {
        val visible = words.filter { it.tier <= maxTier }
        val placed = mutableListOf<PlacedWord>()
        visible.forEach { w ->
            layout.placements[w.id]?.let { p -> placed += PlacedWord(w, p.x, p.y, p.size) }
        }
        visible.filter { it.id !in layout.placements }.forEach { w ->
            val slot = autoSlots.firstOrNull { (x, y) -> placed.none { hypot(it.x - x, it.y - y) < MIN_GAP } }
                ?: autoSlots[placed.size % autoSlots.size]
            placed += PlacedWord(w, slot.first, slot.second, AUTO_SIZE)
        }
        return placed
    }

    /**
     * Forgiving hit test for small fingers. Finds the item whose centre is nearest the tap, if the tap is within
     * [reach] times the item's radius. Coordinates are in pixels; [minRadiusPx] keeps tiny items at least 64dp.
     * The topmost (last drawn) item wins ties.
     */
    fun hitTest(
        items: List<PlacedWord>,
        tapX: Float,
        tapY: Float,
        width: Float,
        height: Float,
        minRadiusPx: Float = 0f,
        reach: Float = 1.25f,
    ): PlacedWord? {
        val side = minOf(width, height)
        var best: PlacedWord? = null
        var bestScore = Float.MAX_VALUE
        items.forEachIndexed { index, item ->
            val radius = maxOf(item.size * side / 2f, minRadiusPx)
            val d = hypot(item.x * width - tapX, item.y * height - tapY)
            if (d <= radius * reach) {
                // Normalised distance; later items win exact ties because they are drawn on top.
                val score = d / radius - index * 1e-4f
                if (score < bestScore) {
                    bestScore = score
                    best = item
                }
            }
        }
        return best
    }

    /** Convenience for tests and tools. */
    fun placementOf(item: PlacedWord) = Placement(item.x, item.y, item.size)
}
