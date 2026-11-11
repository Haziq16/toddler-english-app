package app.toddlerenglish.ui.theme

import org.junit.Assert.assertTrue
import org.junit.Test

class TokensTest {
    @Test
    fun touchTargetIsAtLeast64dp() {
        assertTrue(Dimens.TouchTarget.value >= 64f)
        assertTrue(Dimens.BigTarget.value >= Dimens.TouchTarget.value)
    }

    @Test
    fun everySceneHasATint() {
        listOf("home", "farm", "food", "bedtime", "outdoors").forEach {
            assertTrue("missing tint for $it", Palette.sceneTints.containsKey(it))
        }
    }
}
