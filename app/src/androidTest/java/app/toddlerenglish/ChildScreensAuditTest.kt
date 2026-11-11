package app.toddlerenglish

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * S7 audit on a real device: on every child screen, everything a child can tap
 * (1) has a label TalkBack can read and (2) is at least 64dp in both directions.
 */
@RunWith(AndroidJUnit4::class)
class ChildScreensAuditTest {
    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        // Pebble breathes forever, so the test clock is driven by hand.
        rule.mainClock.autoAdvance = false
        settle()
    }

    private fun settle(ms: Long = 1_500) {
        rule.mainClock.advanceTimeBy(ms)
        rule.waitForIdle()
    }

    private fun clickables(): List<SemanticsNode> = rule.onAllNodes(hasClickAction(), useUnmergedTree = false).fetchSemanticsNodes()

    private fun audit(screen: String) {
        val nodes = clickables()
        assertTrue("$screen: nothing to tap", nodes.isNotEmpty())
        val minPx = with(rule.density) { 64.dp.toPx() } - 1f
        nodes.forEach { n ->
            val label = n.config.getOrNull(SemanticsProperties.ContentDescription)?.joinToString()
                ?: n.config.getOrNull(SemanticsProperties.Text)?.joinToString()
                ?: n.config.getOrNull(SemanticsActions.OnClick)?.label
            assertTrue("$screen: a tappable node has no label (${n.boundsInRoot})", !label.isNullOrBlank())
            // Real size, not the part left visible by a scrolling list.
            val w = n.size.width
            val h = n.size.height
            assertTrue("$screen: '$label' is ${w}x${h}px, under 64dp", w >= minPx && h >= minPx)
        }
    }

    private fun open(description: String) {
        val target = hasContentDescription(description) and hasClickAction()
        if (rule.onAllNodes(target).fetchSemanticsNodes().isEmpty()) {
            // Not composed yet: jump (no animation) to the end of the home grid, where the activity tiles are.
            rule.onAllNodes(hasScrollToIndexAction()).onFirst().performScrollToIndex(HOME_GRID_LAST_INDEX)
            settle(300)
        }
        // The accessibility click (what TalkBack uses) also works for tiles partly below the fold.
        rule.onAllNodes(target)
            .onFirst()
            .performSemanticsAction(SemanticsActions.OnClick)
        settle()
    }

    private fun back() {
        rule.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
        settle()
    }

    @Test
    fun homeScreen() = audit("home")

    @Test
    fun everyScene() {
        listOf("Farm", "Food", "Home", "Bedtime", "Outdoors").forEach { scene ->
            open(scene)
            audit("scene $scene")
            back()
        }
    }

    @Test
    fun gamesAndSongs() {
        open("Games")
        audit("games menu")
        listOf("Bubbles", "Find it", "Feed Pebble").forEach { game ->
            open(game)
            settle(3_000)
            audit(game)
            back()
            open("Games")
        }
        back()
        open("Songs")
        audit("songs list")
    }

    @Test
    fun stickerBookAndGate() {
        open("Stickers")
        audit("sticker book")
        back()
        open("Grown-ups")
        audit("parental gate")
        // Back from the gate returns to play, never into the parent corner.
        back()
        assertTrue(rule.onAllNodesWithContentDescription("Grown-ups").fetchSemanticsNodes().isNotEmpty())
    }

    private companion object {
        /** 5 scenes, 1 spacer, 3 activity tiles. */
        const val HOME_GRID_LAST_INDEX = 8
    }
}
