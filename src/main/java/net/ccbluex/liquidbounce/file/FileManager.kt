package net.ccbluex.liquidbounce.file

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.ccbluex.liquidbounce.CrossSine
import net.ccbluex.liquidbounce.features.macro.Macro
import net.ccbluex.liquidbounce.features.module.EnumAutoDisableType
import net.ccbluex.liquidbounce.file.configs.*
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import java.io.*

class FileManager : MinecraftInstance() {
    val dir = File(mc.mcDataDir, "CrossSine")
    val fontsDir = File(dir, "fonts")
    val configsDir = File(dir, "configs")
    val hertaSoundDir = File(dir, "sounds/herta")
    val legacySettingsDir = File(dir, "legacy-settings.json")
    val themesDir = File(dir, "themes")
    val accountsConfig = AccountsConfig(File(dir, "accounts.json"))
    var friendsConfig = FriendsConfig(File(dir, "friends.json"))
    val xrayConfig = XRayConfig(File(dir, "xray-blocks.json"))
    val hudConfig = HudConfig(File(dir, "hud.json"))
    val subscriptsConfig = ScriptConfig(File(dir, "subscripts.json"))
    val specialConfig = SpecialConfig(File(dir, "special.json"))
    val themeConfig = ThemeConfig(File(dir, "themeColor.json"))
    /**
     * Setup everything important
     */
    init {
        setupFolder()
    }

    /**
     * Setup folder
     */
    fun setupFolder() {
        if (!dir.exists()) {
            dir.mkdir()
        }

        if (!fontsDir.exists()) {
            fontsDir.mkdir()
        }

        if (!configsDir.exists()) {
            configsDir.mkdir()
        }
        if (!hertaSoundDir.exists()) {
            hertaSoundDir.mkdir()
        }

        if (!themesDir.exists()) {
            themesDir.mkdir();
        }

    }

    /**
     * Load a list of configs
     *
     * @param configs list
     */
    fun loadConfigs(vararg configs: FileConfig) {
        if (CrossSine.destruced) return
        for (fileConfig in configs)
            loadConfig(fileConfig)
    }

    /**
     * Load one config
     *
     * @param config to load
     */
    fun loadConfig(config: FileConfig) {
        if (CrossSine.destruced) return
        if (!config.hasConfig()) {
            ClientUtils.logInfo("[FileManager] Skipped loading config: " + config.file.name + ".")
            saveConfig(config, true)
            return
        }
        try {
            config.loadConfig(config.loadConfigFile())
            ClientUtils.logInfo("[FileManager] Loaded config: " + config.file.name + ".")
        } catch (t: Throwable) {
            ClientUtils.logError("[FileManager] Failed to load config file: " + config.file.name + ".", t)
        }
    }

    /**
     * Save all configs in file manager
     */
    fun saveAllConfigs() {
        if (CrossSine.destruced) return
        for (field in javaClass.declaredFields) {
            try {
                field.isAccessible = true
                val obj = field[this]
                if (obj is FileConfig) {
                    saveConfig(obj)
                }
            } catch (e: IllegalAccessException) {
                ClientUtils.logError("[FileManager] Failed to save config file of field " + field.name + ".", e)
            }
        }
    }

    /**
     * Save a list of configs
     *
     * @param configs list
     */
    fun saveConfigs(vararg configs: FileConfig) {
        if (CrossSine.destruced) return
        for (fileConfig in configs) saveConfig(fileConfig)
    }

    /**
     * Save one config
     *
     * @param config to save
     */
    fun saveConfig(config: FileConfig) {
        if (CrossSine.destruced) return
        saveConfig(config, true)
    }

    /**
     * Save one config
     *
     * @param config         to save
     * @param ignoreStarting check starting
     */
    private fun saveConfig(config: FileConfig, ignoreStarting: Boolean) {
        if (CrossSine.destruced) return
        if (!ignoreStarting && CrossSine.isStarting) return
        try {
            if (!config.hasConfig()) config.createConfig()
            config.saveConfigFile(config.saveConfig())
            ClientUtils.logInfo("[FileManager] Saved config: " + config.file.name + ".")
        } catch (t: Throwable) {
            ClientUtils.logError("[FileManager] Failed to save config file: " + config.file.name + ".", t)
        }
    }

    /**
     * Load background for background
     */

    @Throws(IOException::class)
    fun loadLegacy(): Boolean {
        if (CrossSine.destruced) return false
        var modified = false
        val modulesFile = File(dir, "modules.json")
        if (modulesFile.exists()) {
            modified = true
            val fr = FileReader(modulesFile)
            try {
                val jsonElement = JsonParser().parse(BufferedReader(fr))
                for ((key, value) in jsonElement.asJsonObject.entrySet()) {
                    val module = CrossSine.moduleManager.getModule(key)
                    if (module != null) {
                        val jsonModule = value as JsonObject
                        module.state = jsonModule["State"].asBoolean
                        module.keyBind = jsonModule["KeyBind"].asInt
                        if (jsonModule.has("Array")) module.array = jsonModule["Array"].asBoolean
                        if (jsonModule.has("AutoDisable")) module.autoDisable =
                            EnumAutoDisableType.valueOf(jsonModule["AutoDisable"].asString)
                    }
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }
            try {
                fr.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            ClientUtils.logInfo("Deleted Legacy config " + modulesFile.name + " " + modulesFile.delete())
        }

        val valuesFile = File(dir, "values.json")
        if (valuesFile.exists()) {
            modified = true
            val fr = FileReader(valuesFile)
            try {
                val jsonObject = JsonParser().parse(BufferedReader(fr)).asJsonObject
                for ((key, value) in jsonObject.entrySet()) {
                    val module = CrossSine.moduleManager.getModule(key)
                    if (module != null) {
                        val jsonModule = value as JsonObject
                        for (moduleValue in module.values) {
                            val element = jsonModule[moduleValue.name]
                            if (element != null) moduleValue.fromJson(element)
                        }
                    }
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }
            try {
                fr.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            ClientUtils.logInfo("Deleted Legacy config " + valuesFile.name + " " + valuesFile.delete())
        }

        val macrosFile = File(dir, "macros.json")
        if (macrosFile.exists()) {
            modified = true
            val fr = FileReader(macrosFile)
            try {
                val jsonArray = JsonParser().parse(BufferedReader(fr)).asJsonArray
                for (jsonElement in jsonArray) {
                    val macroJson = jsonElement.asJsonObject
                    CrossSine.macroManager.macros
                        .add(Macro(macroJson["key"].asInt, macroJson["command"].asString))
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }
            try {
                fr.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            ClientUtils.logInfo("Deleted Legacy config " + macrosFile.name + " " + macrosFile.delete())
        }

        val shortcutsFile = File(dir, "shortcuts.json")
        if (shortcutsFile.exists()) shortcutsFile.delete()

        return modified
    }

    companion object {
        val PRETTY_GSON = GsonBuilder().setPrettyPrinting().create()
    }
}
