package de.md5lukas.waypoints.config.general

import de.md5lukas.konfig.ConfigPath
import de.md5lukas.konfig.Configurable
import de.md5lukas.konfig.TypeAdapter
import de.md5lukas.konfig.UseAdapter
import de.md5lukas.waypoints.api.Type
import de.md5lukas.waypoints.util.Expression
import de.md5lukas.waypoints.util.MathParser
import java.time.Duration
import org.bukkit.configuration.ConfigurationSection

@Configurable
class TeleportConfiguration {

  var standStillTime: Duration = Duration.ZERO
    private set

  @ConfigPath("visitedRadius")
  var visitedRadiusSquared = 0
    private set(value) {
      field = value * value
    }

  val private = TypedTeleportConfiguration()
  val death = TypedTeleportConfiguration()
  val public = TypedTeleportConfiguration()
  val permission = TypedTeleportConfiguration()
}

@Configurable
class TypedTeleportConfiguration {

  var cooldown: Duration = Duration.ZERO
    private set

  var alsoApplyCooldownTo: List<Type> = emptyList()
    private set

  var mustVisit: Boolean? = false
    private set

  var paymentType: TeleportPaymentType = TeleportPaymentType.XP
    private set

  var perCategory: Boolean = false
    private set

  var maxCost: Long = 1
    private set

  @UseAdapter(MathAdapter::class)
  var formula: Expression = MathParser.parse("1")
    private set

  var onlyLastWaypoint: Boolean? = false
    private set

  private class MathAdapter : TypeAdapter<Expression> {
    override fun get(section: ConfigurationSection, path: String) =
        section.getString(path)?.let { MathParser.parse(it, "n") }
  }
}

enum class TeleportPaymentType {
  DISABLED,
  FREE,
  XP,
  XP_POINTS,
  VAULT
}
