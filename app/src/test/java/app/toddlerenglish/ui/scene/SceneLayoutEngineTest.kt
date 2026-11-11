package app.toddlerenglish.ui.scene

import app.toddlerenglish.content.Content
import app.toddlerenglish.content.ContentLoader
import app.toddlerenglish.content.Placement
import app.toddlerenglish.content.SceneLayout
import app.toddlerenglish.content.Word
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SceneLayoutEngineTest {
    private fun word(id: String, tier: Int = 1) = Word(id, id, "farm", tier, "audio/words/$id.ogg", "img_$id")

    private fun realContent(): Content {
        val assets = File(System.getProperty("assets.dir") ?: "src/main/assets")
        return ContentLoader { File(assets, "content/$it").takeIf { f -> f.isFile }?.readText() }.load()
    }

    @Test
    fun placedWordsGoWhereTheLayoutSays() {
        val layout = SceneLayout("farm", mapOf("cow" to Placement(0.3f, 0.4f, 0.2f)))
        val out = SceneLayoutEngine.place(listOf(word("cow")), layout, maxTier = 3)
        assertEquals(PlacedWord(word("cow"), 0.3f, 0.4f, 0.2f), out.single())
    }

    @Test
    fun aWordAddedOnlyToWordsJsonStillAppears() {
        val content = realContent()
        val farm = content.wordsInScene("farm")
        val layout = content.layouts.getValue("farm")
        val newWord = word("llama", tier = 1)
        val out = SceneLayoutEngine.place(farm + newWord, layout, maxTier = 3)
        val placed = out.firstOrNull { it.word.id == "llama" }
        assertNotNull("new word was not placed", placed)
        assertTrue(placed!!.x in 0f..1f && placed.y in 0f..1f)
    }

    @Test
    fun autoPlacedWordsDoNotStack() {
        val words = (1..8).map { word("w$it") }
        val out = SceneLayoutEngine.place(words, SceneLayout("farm"), maxTier = 1)
        val spots = out.map { it.x to it.y }.toSet()
        assertEquals(8, spots.size)
    }

    @Test
    fun tierLimitHidesHarderWords() {
        val words = listOf(word("a", 1), word("b", 2), word("c", 3))
        assertEquals(listOf("a"), SceneLayoutEngine.place(words, SceneLayout("farm"), 1).map { it.word.id })
        assertEquals(3, SceneLayoutEngine.place(words, SceneLayout("farm"), 3).size)
    }

    @Test
    fun hitTestIsForgivingAndPicksTheNearest() {
        val items = listOf(PlacedWord(word("cow"), 0.25f, 0.5f, 0.2f), PlacedWord(word("pig"), 0.75f, 0.5f, 0.2f))
        // 1000 x 1000 scene: cow radius 100px at (250, 500)
        assertEquals("cow", SceneLayoutEngine.hitTest(items, 250f, 500f, 1000f, 1000f)?.word?.id)
        assertEquals("cow", SceneLayoutEngine.hitTest(items, 360f, 500f, 1000f, 1000f)?.word?.id) // just outside the art
        assertEquals("pig", SceneLayoutEngine.hitTest(items, 700f, 520f, 1000f, 1000f)?.word?.id)
        assertNull(SceneLayoutEngine.hitTest(items, 500f, 100f, 1000f, 1000f))
    }

    @Test
    fun tinyItemsStillHaveAFullSizeTarget() {
        val items = listOf(PlacedWord(word("egg"), 0.5f, 0.5f, 0.05f))
        // 25px radius art, but 64px minimum radius
        assertNotNull(SceneLayoutEngine.hitTest(items, 560f, 500f, 1000f, 1000f, minRadiusPx = 64f))
    }

    @Test
    fun everyRealSceneShowsAllItsWordsAtTierThree() {
        val content = realContent()
        content.scenes.forEach { scene ->
            val words = content.wordsInScene(scene.id)
            val out = SceneLayoutEngine.place(words, content.layouts.getValue(scene.id), 3)
            assertEquals(scene.id, words.size, out.size)
        }
    }
}
