package app.toddlerenglish.games

import app.toddlerenglish.content.Content
import app.toddlerenglish.content.Game
import app.toddlerenglish.content.Word
import kotlin.random.Random

/**
 * Pure rules for the mini-games. No Android, so every game is tested at every tier.
 *
 * Child rules (D-005): there is no failure outcome. A tap on something other than the target is answered by
 * naming what was tapped ([TapResult.Named]) and asking again. Rounds only ever move forward.
 */
sealed interface TapResult {
    /** The child found what was asked for. */
    data class Found(val word: Word, val roundComplete: Boolean) : TapResult

    /** The child tapped something else: we say its name and gently ask again. */
    data class Named(val word: Word) : TapResult
}

/** How each tier changes the games. Tier 1 = fewest, biggest, slowest. */
object TierRules {
    fun bubblesOnScreen(tier: Int) = when (tier) {
        1 -> 3
        2 -> 4
        else -> 5
    }

    /** Seconds for a bubble to float up the screen. */
    fun bubbleRiseSeconds(tier: Int) = when (tier) {
        1 -> 9f
        2 -> 7.5f
        else -> 6f
    }

    fun matchChoices(tier: Int) = when (tier) {
        1 -> 2
        2 -> 3
        else -> 4
    }

    fun feedChoices(tier: Int) = when (tier) {
        1 -> 2
        2 -> 3
        else -> 4
    }

    /** At tier 1 Pebble happily eats anything; from tier 2 Pebble asks for one food by name. */
    fun feedAsksForFood(tier: Int) = tier >= 2

    const val POPS_PER_ROUND = 8
    const val FINDS_PER_ROUND = 5
    const val FEEDS_PER_ROUND = 5
}

/** Words a game may use at [tier], from one deck. */
fun Game.deckWords(content: Content, deckId: String, tier: Int): List<Word> = content.wordsUpToTier(decks[deckId]?.words.orEmpty(), tier)

/** Picks a deck with enough words for [tier], so a round can always be played. */
fun Game.pickDeck(content: Content, tier: Int, need: Int, random: Random): String? = decks.keys.filter {
    deckWords(content, it, tier).size >=
        need
}.randomOrNull(random)

/** Pop the bubbles: every bubble is a word. Every pop counts. */
class BubblesRound(private val words: List<Word>, private val random: Random) {
    var pops = 0
        private set

    init {
        require(words.isNotEmpty())
    }

    /** The next word to float up, avoiding the ones already on screen when possible. */
    fun nextWord(onScreen: Collection<String>): Word = words.filter { it.id !in onScreen }.randomOrNull(random) ?: words.random(random)

    /** Returns true when this pop completes the round. */
    fun pop(): Boolean {
        pops++
        return pops % TierRules.POPS_PER_ROUND == 0
    }
}

/** Shared engine for "find the X" (match) and "feed me the X" (feed). */
class FindRound(private val words: List<Word>, private val choices: Int, private val perRound: Int, private val random: Random) {
    var target: Word
        private set
    var options: List<Word>
        private set
    var found = 0
        private set

    init {
        require(words.size >= choices) { "deck has ${words.size} words, needs $choices" }
        target = words.random(random)
        options = buildOptions(target)
    }

    fun tap(word: Word): TapResult {
        if (word.id != target.id) return TapResult.Named(word)
        found++
        val result = TapResult.Found(word, roundComplete = found % perRound == 0)
        next()
        return result
    }

    /** Tier 1 feeding: whatever the child picks is right. */
    fun acceptAny(word: Word): TapResult.Found {
        found++
        val result = TapResult.Found(word, roundComplete = found % perRound == 0)
        next()
        return result
    }

    private fun next() {
        val previous = target
        target = words.filter { it.id != previous.id }.randomOrNull(random) ?: previous
        options = buildOptions(target)
    }

    private fun buildOptions(t: Word): List<Word> = (
        listOf(t) + words.filter {
            it.id != t.id
        }.shuffled(random).take(choices - 1)
        ).shuffled(random)
}
