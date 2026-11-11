package app.toddlerenglish.games

import app.toddlerenglish.content.Content
import app.toddlerenglish.content.ContentLoader
import java.io.File
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** S5 "done when": each game works at all three tiers, with the real content. */
class GameLogicTest {
    private val content: Content by lazy {
        val assets = File(System.getProperty("assets.dir") ?: "src/main/assets")
        ContentLoader { File(assets, "content/$it").takeIf { f -> f.isFile }?.readText() }.load()
    }

    private fun game(id: String) = content.games.first { it.id == id }

    @Test
    fun bubblesWorkAtEveryTierForEveryDeck() {
        val g = game("bubbles")
        for (tier in 1..3) {
            g.decks.keys.forEach { deck ->
                val words = g.deckWords(content, deck, tier)
                assertTrue("bubbles/$deck tier $tier", words.size >= TierRules.bubblesOnScreen(tier))
                assertTrue(words.all { it.tier <= tier })
                val round = BubblesRound(words, Random(tier))
                val onScreen = mutableListOf<String>()
                repeat(TierRules.bubblesOnScreen(tier)) { onScreen += round.nextWord(onScreen).id }
                assertEquals("bubbles on screen should differ", onScreen.size, onScreen.toSet().size)
                var completed = 0
                repeat(TierRules.POPS_PER_ROUND) { if (round.pop()) completed++ }
                assertEquals(1, completed)
            }
        }
    }

    @Test
    fun matchWorksAtEveryTierForEveryDeck() {
        val g = game("match")
        for (tier in 1..3) {
            g.decks.keys.forEach { deck ->
                playFindRound(g.deckWords(content, deck, tier), TierRules.matchChoices(tier), TierRules.FINDS_PER_ROUND, tier)
            }
        }
    }

    @Test
    fun feedWorksAtEveryTier() {
        val g = game("feed")
        for (tier in 1..3) {
            val deck = g.pickDeck(content, tier, TierRules.feedChoices(tier), Random(1))
            assertNotNull("no feed deck for tier $tier", deck)
            val words = g.deckWords(content, deck!!, tier)
            assertTrue(words.all { "food" in it.tags })
            playFindRound(words, TierRules.feedChoices(tier), TierRules.FEEDS_PER_ROUND, tier)
        }
    }

    @Test
    fun singAlongHasASongForEveryTier() {
        assertTrue(content.songs.isNotEmpty())
        content.songs.forEach { song ->
            val tokens = song.lines.sumOf { it.tokens.size }
            assertEquals("timings for ${song.id}", tokens, song.timings.size)
            assertTrue(song.timings.zipWithNext().all { (a, b) -> a.startMs <= b.startMs })
        }
    }

    @Test
    fun tappingTheWrongThingNeverFailsAndNeverEndsTheRound() {
        val words = game("match").deckWords(content, "farm", 3)
        val round = FindRound(words, 4, 5, Random(9))
        repeat(50) {
            val other = round.options.first { it.id != round.target.id }
            val r = round.tap(other)
            assertTrue(r is TapResult.Named)
        }
        assertEquals(0, round.found)
    }

    private fun playFindRound(words: List<app.toddlerenglish.content.Word>, choices: Int, perRound: Int, tier: Int) {
        assertTrue("need $choices words at tier $tier, have ${words.size}", words.size >= choices)
        val round = FindRound(words, choices, perRound, Random(tier * 31))
        var completes = 0
        repeat(perRound) {
            assertEquals(choices, round.options.size)
            assertTrue(round.target in round.options)
            assertEquals(choices, round.options.map { it.id }.toSet().size)
            val r = round.tap(round.target) as TapResult.Found
            if (r.roundComplete) completes++
        }
        assertEquals(1, completes)
    }
}
