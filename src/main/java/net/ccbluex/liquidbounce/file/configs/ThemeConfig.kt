package net.ccbluex.liquidbounce.file.configs

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.ccbluex.liquidbounce.file.FileConfig
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.ui.client.gui.colortheme.ClientTheme
import java.io.File

class ThemeConfig(file: File) : FileConfig(file) {

    override fun loadConfig(config: String) {
        val json = JsonParser().parse(config).asJsonObject
        if (json.has("Theme")) {
            ClientTheme.ClientColorMode.set(json.get("Theme").asString)
        }
        if (json.has("Fade-Speed")) {
            ClientTheme.fadespeed.set(json.get("Fade-Speed").asInt)
        }
        if (json.has("Fade-Type")) {
            ClientTheme.updown.set(json.get("Fade-Type").asBoolean)
        }
            if (json.has("Text-Static")) {
                ClientTheme.textValue.set(json.get("Text-Static").asBoolean)
            }
    }

    override fun saveConfig(): String {
        val json = JsonObject()
        json.addProperty("Theme", ClientTheme.ClientColorMode.get())
        json.addProperty("Fade-Speed", ClientTheme.fadespeed.get())
        json.addProperty("Fade-Type", ClientTheme.updown.get())
        json.addProperty("Text-Static", ClientTheme.textValue.get())
        return FileManager.PRETTY_GSON.toJson(json)
    }
}