package app.toddlerenglish.audio

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

/** Parses `assets/audio/index.json` (clip path -> duration in ms), written by scripts/gen_audio.py. */
object AudioIndex {
    fun parse(text: String?): Map<String, Long> {
        if (text.isNullOrBlank()) return emptyMap()
        val obj = Json.parseToJsonElement(text) as? JsonObject ?: return emptyMap()
        return obj.mapValues { (_, v) -> v.jsonPrimitive.long }
    }
}
