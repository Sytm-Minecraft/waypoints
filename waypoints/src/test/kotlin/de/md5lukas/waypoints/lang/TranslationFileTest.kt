package de.md5lukas.waypoints.lang

import de.md5lukas.waypoints.WaypointsPlugin
import kotlin.test.asserter
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.configuration.file.YamlConfiguration
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class TranslationFileTest {

  companion object {
    lateinit var required: List<String>

    @BeforeAll
    @JvmStatic
    fun collectUsedKeys() {
      val result = mutableListOf<String>()

      Translations(
          object : TranslationLoader {
            override val plugin: WaypointsPlugin
              get() = throw UnsupportedOperationException()

            override val itemMiniMessage: MiniMessage
              get() = MiniMessage.miniMessage()

            override fun get(key: String): String = throw UnsupportedOperationException()

            override fun registerTranslationWrapper(translation: AbstractTranslation) {
              result.addAll(translation.getKeys())
            }
          })

      required = result
    }
  }

  private fun getDefinedKeys(language: String): List<String> {
    val config =
        YamlConfiguration.loadConfiguration(
            javaClass.classLoader.getResourceAsStream("lang/$language.yml")!!.bufferedReader())

    return config.getKeys(true).filter(config::isString).toList()
  }

  @ParameterizedTest
  @ValueSource(strings = ["en", "de" /*, "zh-simplified", "zh-hk"*/])
  fun verifyLanguageFileCompleteness(language: String) {
    val defined = getDefinedKeys(language)

    assertAll(
        "Missing configuration keys for language $language",
        required
            .filterNot(defined::contains)
            .map { { asserter.fail("'$it' is missing") } }
            .toList())
  }
}
