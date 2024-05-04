package de.md5lukas.waypoints.integrations

import de.md5lukas.commons.paper.placeholder
import de.md5lukas.waypoints.WaypointsPlugin
import de.md5lukas.waypoints.config.integrations.GeyserConfiguration
import de.md5lukas.waypoints.lang.Translations
import de.md5lukas.waypoints.util.asPlainText
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.entity.Player
import org.geysermc.cumulus.form.SimpleForm
import org.geysermc.cumulus.util.FormImage
import org.geysermc.geyser.api.GeyserApi

class GeyserIntegration(private val plugin: WaypointsPlugin) {

  private val translations: Translations
    get() = plugin.translations

  private val config: GeyserConfiguration
    get() = plugin.waypointsConfig.integrations.geyser

  fun setupGeyser(): Boolean {
    return plugin.server.pluginManager.getPlugin("Geyser-Spigot") !== null
  }

  fun isBedrockPlayer(player: Player): Boolean {
    return GeyserApi.api().isBedrockPlayer(player.uniqueId)
  }

  fun sendTrackingRequest(
      player: Player,
      from: Player,
      validForResolver: TagResolver,
      onAccept: () -> Unit
  ) {
    GeyserApi.api()
        .sendForm(
            player.uniqueId,
            SimpleForm.builder()
                .title(translations.MESSAGE_TRACKING_REQUEST_GEYSER_TITLE.text.asPlainText())
                .content(
                    translations.MESSAGE_TRACKING_REQUEST_GEYSER_MESSAGE.withReplacements(
                            "from" placeholder from.displayName(),
                            validForResolver,
                        )
                        .asPlainText())
                .button(
                    translations.MESSAGE_TRACKING_REQUEST_GEYSER_ACCEPT.text.asPlainText(),
                    FormImage.Type.PATH,
                    config.acceptIcon)
                .button(
                    translations.MESSAGE_TRACKING_REQUEST_GEYSER_DECLINE.text.asPlainText(),
                    FormImage.Type.PATH,
                    config.declineIcon)
                .validResultHandler { response ->
                  @Suppress(
                      "KotlinConstantConditions") // Annotation is messed up. The id is 0 based
                  if (response.clickedButtonId() == 0) {
                    onAccept()
                  }
                })
  }
}
