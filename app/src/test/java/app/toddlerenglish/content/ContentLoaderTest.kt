package app.toddlerenglish.content

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Loads the real bundled content, so a broken JSON file fails the unit tests as well as the validator. */
class ContentLoaderTest {
    private val assets = File(System.getProperty("assets.dir") ?: "src/main/assets")
    private val drawables = File(System.getProperty("res.dir") ?: "src/main/res", "drawable")

    private fun realContent(): Content = ContentLoader { name ->
        File(assets, "content/$name").takeIf { it.isFile }?.readText()
    }.load()

    @Test
    fun loadsOneHundredWordsInFiveScenes() {
        val content = realContent()
        assertEquals(100, content.words.size)
        assertEquals(5, content.scenes.size)
        content.scenes.forEach { scene ->
            assertTrue("scene ${scene.id} is nearly empty", content.wordsInScene(scene.id).size >= 15)
        }
    }

    @Test
    fun everyWordHasAudioAndArt() {
        realContent().words.forEach { word ->
            assertTrue("audio missing for ${word.id}", File(assets, word.audio).isFile)
            assertTrue("art missing for ${word.id}", File(drawables, "${word.image}.xml").isFile)
            assertTrue("tier out of range for ${word.id}", word.tier in 1..3)
        }
    }

    @Test
    fun everyPhraseHasAudio() {
        realContent().phrases.forEach { phrase ->
            assertTrue("audio missing for ${phrase.id}", File(assets, phrase.audio).isFile)
        }
    }

    @Test
    fun tierFilterIncludesEasierTiers() {
        val content = realContent()
        val farm = content.wordsInScene("farm").map { it.id }
        val tier1 = content.wordsUpToTier(farm, 1)
        val tier3 = content.wordsUpToTier(farm, 3)
        assertTrue(tier1.all { it.tier == 1 })
        assertEquals(farm.size, tier3.size)
        assertNotNull(content.word("cow"))
    }

    @Test
    fun missingOptionalFilesGiveEmptyLists() {
        val files = mapOf(
            "words.json" to """{"schemaVersion":1,"words":[]}""",
            "phrases.json" to """{"schemaVersion":1,"phrases":[]}""",
            "scenes.json" to """{"schemaVersion":1,"scenes":[]}""",
        )
        val content = ContentLoader { files[it] }.load()
        assertTrue(content.games.isEmpty())
        assertTrue(content.songs.isEmpty())
    }
}
