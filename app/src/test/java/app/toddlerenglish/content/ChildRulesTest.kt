package app.toddlerenglish.content

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the child rules (D-005) that can be checked from files:
 * no negative or failure words in anything Pebble says, no failure/timer UI words in child screens.
 */
class ChildRulesTest {
    private val assets = File(System.getProperty("assets.dir") ?: "src/main/assets")
    private val sources = File(System.getProperty("assets.dir") ?: "src/main/assets").parentFile.resolve("java")
    private val negative =
        Regex(
            """\b(no|not|wrong|oops|uh[- ]oh|fail\w*|lose|lost|bad|incorrect|try again|game over|hurry|quick)\b""",
            RegexOption.IGNORE_CASE,
        )

    @Test
    fun pebbleNeverSaysAnythingNegative() {
        val content = ContentLoader { File(assets, "content/$it").takeIf { f -> f.isFile }?.readText() }.load()
        content.phrases.forEach { p ->
            assertTrue("phrase ${p.id} is negative: \"${p.text}\"", !negative.containsMatchIn(p.text))
        }
    }

    @Test
    fun childScreensHaveNoFailureOrTimerStates() {
        val childDirs = listOf("ui", "games").map { sources.resolve("app/toddlerenglish/$it") }
        val banned = Regex("""(?i)(WrongAnswer|GameOver|Mood\.Sad|lives\s*=|countdown|streak)""")
        childDirs.filter { it.exists() }.flatMap { it.walkTopDown().filter { f -> f.extension == "kt" }.toList() }.forEach { f ->
            val hit = banned.find(f.readText())
            assertTrue("${f.name} contains '${hit?.value}'", hit == null)
        }
    }
}
