package de.md5lukas.signgui;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.block.TileState;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public class SignGUI {

  private static final int OFFSET = 4; // Needs to be closer to the player since 1.20
  private static final List<Component> EMPTY_LINES =
      List.of(Component.empty(), Component.empty(), Component.empty(), Component.empty());

  private final @NotNull Plugin plugin;
  private final @NotNull ProtocolManager protocolManager;
  private final @NotNull Player player;
  private final @NotNull Consumer<@NotNull String @NotNull []> onClose;

  private final @NotNull DyeColor color;
  private final @NotNull List<@NotNull Component> lines;

  private boolean open;

  private SignGUI(
      @NotNull Plugin plugin,
      @NotNull Player player,
      @NotNull Consumer<@NotNull String @NotNull []> onClose,
      @NotNull DyeColor color,
      @NotNull List<@NotNull Component> lines) {
    this.plugin = plugin;
    this.protocolManager = ProtocolLibrary.getProtocolManager();
    this.player = player;
    this.onClose = onClose;
    this.color = color;
    this.lines = lines;
    this.open = false;
  }

  private void open() {
    if (open) {
      return;
    }
    open = true;

    final var signLocation = getSignLocation();

    protocolManager.addPacketListener(
        new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Client.UPDATE_SIGN) {
          @Override
          public void onPacketReceiving(PacketEvent event) {
            if (event.getPlayer() != player) {
              return;
            }
            protocolManager.removePacketListener(this);
            event.setCancelled(true);
            open = false;
            final var realBlock = signLocation.getBlock();
            player.sendBlockChange(signLocation, realBlock.getBlockData());
            if (realBlock.getState() instanceof TileState tileState) {
              player.sendBlockUpdate(signLocation, tileState);
            }

            final var lines = event.getPacket().getStringArrays().read(0);

            for (int index = 0; index < 3; index++) {
              SignGUI.this.lines.set(index, fromPlain(lines[index]));
            }

            onClose.accept(lines);
          }
        });

    final var blockData = plugin.getServer().createBlockData(Material.OAK_SIGN);
    player.sendBlockChange(signLocation, blockData);

    if (lines != EMPTY_LINES) {
      final var blockState = (Sign) blockData.createBlockState();
      final var side = blockState.getSide(Side.FRONT);
      side.setColor(color);

      for (int index = 0; index < 4; index++) {
        side.line(index, lines.get(index));
      }

      player.sendBlockUpdate(signLocation, blockState);
    }

    final var openSignPacket = new PacketContainer(PacketType.Play.Server.OPEN_SIGN_EDITOR);
    openSignPacket
        .getBlockPositionModifier()
        .write(
            0,
            new BlockPosition(
                signLocation.getBlockX(), signLocation.getBlockY(), signLocation.getBlockZ()));
    openSignPacket.getBooleans().writeSafely(0, true); // 1.20 front
    protocolManager.sendServerPacket(player, openSignPacket);
  }

  private @NotNull Location getSignLocation() {
    final @NotNull var signLocation = player.getLocation().toBlockLocation();

    if (signLocation.getY() <= signLocation.getWorld().getMinHeight() + OFFSET) {
      signLocation.add(0, OFFSET, 0);
    } else {
      signLocation.subtract(0, OFFSET, 0);
    }
    return signLocation;
  }

  private @NotNull Component fromPlain(@NotNull String json) {
    return PlainTextComponentSerializer.plainText().deserialize(json);
  }

  public static @NotNull Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private Builder() {}

    private Plugin plugin;
    private Player player;
    private Consumer<@NotNull String @NotNull []> onClose;
    private @NotNull DyeColor color = DyeColor.BLACK;
    private @NotNull List<@NotNull Component> lines = EMPTY_LINES;

    public @NotNull Builder plugin(@NotNull Plugin plugin) {
      Preconditions.checkNotNull(plugin);
      this.plugin = plugin;
      return this;
    }

    public @NotNull Builder player(@NotNull Player player) {
      Preconditions.checkNotNull(player);
      this.player = player;
      return this;
    }

    public @NotNull Builder onClose(@NotNull Consumer<@NotNull String @NotNull []> onClose) {
      Preconditions.checkNotNull(onClose);
      this.onClose = onClose;
      return this;
    }

    public @NotNull Builder color(@NotNull DyeColor color) {
      Preconditions.checkNotNull(color);
      this.color = color;
      return this;
    }

    public @NotNull Builder lines(@NotNull @Unmodifiable List<@NotNull String> lines) {
      Preconditions.checkNotNull(lines);
      Preconditions.checkArgument(lines.size() == 4);
      this.lines = new ArrayList<>(4);
      for (final var line : lines) {
        this.lines.add(PlainTextComponentSerializer.plainText().deserialize(line));
      }
      return this;
    }

    public @NotNull SignGUI open() {
      Preconditions.checkNotNull(plugin);
      Preconditions.checkNotNull(player);
      Preconditions.checkNotNull(onClose);
      final var signGui = new SignGUI(plugin, player, onClose, color, lines);
      signGui.open();
      return signGui;
    }
  }
}
