package app.toddlerenglish.data

import app.toddlerenglish.content.Content
import app.toddlerenglish.content.ContentLoader
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RewardsTest {
    private val content: Content by lazy {
        val assets = File(System.getProperty("assets.dir") ?: "src/main/assets")
        ContentLoader { File(assets, "content/$it").takeIf { f -> f.isFile }?.readText() }.load()
    }

    @Test
    fun hearingFiveFarmWordsEarnsTheFirstFarmSticker() {
        val five = content.wordsInScene("farm").take(5).associate { it.id to 1 }
        val earned = Rewards.newlyEarned(Progress(heard = five), content).map { it.id }
        assertEquals(listOf("farm_5"), earned)
    }

    @Test
    fun hearingTheSameWordAgainDoesNotCountTwice() {
        val progress = Progress(heard = mapOf("cow" to 50))
        assertTrue(Rewards.newlyEarned(progress, content).none { it.id.startsWith("farm_") })
    }

    @Test
    fun stickersAlreadyOwnedAreNotAwardedAgain() {
        val five = content.wordsInScene("farm").take(5).associate { it.id to 1 }
        val progress = Progress(heard = five, stickers = listOf("farm_5"))
        assertTrue(Rewards.newlyEarned(progress, content).isEmpty())
    }

    @Test
    fun aFirstGameRoundEarnsASticker() {
        val earned = Rewards.newlyEarned(Progress(gameRounds = mapOf("bubbles" to 1)), content).map { it.id }
        assertEquals(listOf("bubbles_1"), earned)
    }

    @Test
    fun everyStickerCanBeEarned() {
        val all = Progress(
            heard = content.words.associate { it.id to 1 },
            gameRounds = content.stickers.mapNotNull { it.rule.game }.associateWith { 100 },
            songsFinished = content.songs.map { it.id }.toSet(),
        )
        val earned = Rewards.newlyEarned(all, content).map { it.id }.toSet()
        assertEquals(content.stickers.map { it.id }.toSet(), earned)
    }
}
