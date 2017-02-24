package org.inventivetalent.title;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.inventivetalent.apihelper.API;
import org.inventivetalent.apihelper.APIManager;
import org.inventivetalent.packetlistener.PacketListenerAPI;
import org.inventivetalent.playerversion.IPlayerVersion;
import org.inventivetalent.playerversion.PlayerVersion;
import org.inventivetalent.reflection.minecraft.Minecraft;
import org.inventivetalent.reflection.resolver.*;
import org.inventivetalent.reflection.resolver.minecraft.NMSClassResolver;

public class TitleAPI implements API {

	static ClassResolver    classResolver    = new ClassResolver();
	static NMSClassResolver nmsClassResolver = new NMSClassResolver();

	static Class<?> IChatBaseComponent = nmsClassResolver.resolveSilent("IChatBaseComponent");
	static Class<?> ChatSerializer     = nmsClassResolver.resolveSilent("ChatSerializer", "IChatBaseComponent$ChatSerializer");
	static Class<?> PacketPlayOutTitle = classResolver.resolveSilent("net.minecraft.server." + Minecraft.getVersion() + "PacketPlayOutTitle", "org.spigotmc.ProtocolInjector$PacketTitle");
	static Class<?> EnumTitleAction    = classResolver.resolveSilent("net.minecraft.server." + Minecraft.getVersion() + "PacketPlayOutTitle$EnumTitleAction", "net.minecraft.server." + Minecraft.getVersion() + "EnumTitleAction", "org.spigotmc.ProtocolInjector$PacketTitle$Action");
	static Class<?> PlayerConnection   = nmsClassResolver.resolveSilent("PlayerConnection");
	static Class<?> EntityPlayer       = nmsClassResolver.resolveSilent("EntityPlayer");
	static Class<?> NetworkManager     = nmsClassResolver.resolveSilent("NetworkManager");
	static Class<?> Channel            = classResolver.resolveSilent("net.minecraft.util.io.netty.channel.Channel", "io.netty.channel.Channel");

	static ConstructorResolver PacketTitleConstructorResolver = new ConstructorResolver(PacketPlayOutTitle);

	static FieldResolver EntityPlayerFieldResolver     = new FieldResolver(EntityPlayer);
	static FieldResolver PlayerConnectionFieldResolver = new FieldResolver(PlayerConnection);
	static FieldResolver NetworkManagerFieldResolver   = new FieldResolver(NetworkManager);

	static MethodResolver PlayerConnectionMethodResolver = new MethodResolver(PlayerConnection);
	static MethodResolver ChatSerailizerMethodResolver   = new MethodResolver(ChatSerializer);
	static MethodResolver NetworkManagerMethodResolver   = new MethodResolver(NetworkManager);

	//// Title

	/**
	 * Send a Title
	 *
	 * @param player {@link Player} to send the Title to
	 * @param json   JSON-String
	 */
	public static void sendTitle(Player player, String json) {
		if (player == null || json == null) { throw new IllegalArgumentException("null argument"); }
		PlayerVersion.Version version = getPlayerVersion().getVersion(player.getUniqueId());
		if (version == null || version.olderThan(PlayerVersion.Version.v1_8)) { return; }
		if (!json.startsWith("{") || !json.endsWith("}")) { throw new IllegalArgumentException("invalid json: " + json); }

		try {
			Object serialized = serialize(json);
			Object packetTitle = PacketTitleConstructorResolver.resolve(new Class[] {
					EnumTitleAction,
					IChatBaseComponent }).newInstance(EnumTitleAction.getEnumConstants()[0], serialized);
			sendPacket(player, packetTitle);
		} catch (Exception e) {
			throw new RuntimeException("Failed to send Title " + json + " to " + player, e);
		}
	}

	/**
	 * Send a Title
	 *
	 * @param player        {@link Player} to send the Title to
	 * @param baseComponent Title
	 */
	public static void sendTitle(Player player, BaseComponent baseComponent) {
		if (player == null || baseComponent == null) { throw new IllegalArgumentException("null argument"); }
		PlayerVersion.Version version = getPlayerVersion().getVersion(player.getUniqueId());
		if (version == null || version.olderThan(PlayerVersion.Version.v1_8)) { return; }
		String json = ComponentSerializer.toString(baseComponent);
		sendTitle(player, json);
	}

	/**
	 * Send a Title
	 *
	 * @param player        {@link Player} to send the Title to
	 * @param baseComponent Title
	 * @param fadeIn        Time it should take to fade In
	 * @param stay          Time the Title should stay on screen
	 * @param fadeOut       Time it should take to fade Out
	 */
	public static void sendTitle(Player player, BaseComponent baseComponent, int fadeIn, int stay, int fadeOut) {
		sendTimings(player, fadeIn, stay, fadeOut);
		sendTitle(player, baseComponent);
	}

	//// SubTitle

	/**
	 * Send a Subtitle
	 *
	 * @param player {@link Player} to send the Subtitle to
	 * @param json   JSON-String
	 */
	public static void sendSubTitle(Player player, String json) {
		if (player == null || json == null) { throw new IllegalArgumentException("null argument"); }
		PlayerVersion.Version version = getPlayerVersion().getVersion(player.getUniqueId());
		if (version == null || version.olderThan(PlayerVersion.Version.v1_8)) { return; }
		if (!json.startsWith("{") || !json.endsWith("}")) { throw new IllegalArgumentException("invalid json: " + json); }

		try {
			Object serialized = serialize(json);
			Object packetTitle = PacketTitleConstructorResolver.resolve(new Class[] {
					EnumTitleAction,
					IChatBaseComponent }).newInstance(EnumTitleAction.getEnumConstants()[1], serialized);
			sendPacket(player, packetTitle);
		} catch (Exception e) {
			throw new RuntimeException("Failed to send SubTitle " + json + " to " + player, e);
		}
	}

	/**
	 * Send a Subtitle
	 *
	 * @param player        {@link Player} to send the Subtitle to
	 * @param baseComponent Subtitle
	 */
	public static void sendSubTitle(Player player, BaseComponent baseComponent) {
		if (player == null || baseComponent == null) { throw new IllegalArgumentException("null argument"); }
		PlayerVersion.Version version = getPlayerVersion().getVersion(player.getUniqueId());
		if (version == null || version.olderThan(PlayerVersion.Version.v1_8)) { return; }

		String json = ComponentSerializer.toString(baseComponent);
		sendSubTitle(player, json);
	}

	/**
	 * Send a Subtitle
	 *
	 * @param player        {@link Player} to send the Subtitle to
	 * @param baseComponent Subtitle
	 * @param fadeIn        Time it should take to fade In
	 * @param stay          Time the Title should stay on screen
	 * @param fadeOut       Time it should take to fade Out
	 */
	public static void sendSubTitle(Player player, BaseComponent baseComponent, int fadeIn, int stay, int fadeOut) {
		sendTimings(player, fadeIn, stay, fadeOut);
		sendSubTitle(player, baseComponent);
	}

	//// Timings

	/**
	 * Set the Title Timings
	 *
	 * @param player  Player to Update the Timings
	 * @param fadeIn  Time it should take to fade In
	 * @param stay    Time the Title should stay on screen
	 * @param fadeOut Time it should take to fade Out
	 */
	public static void sendTimings(Player player, int fadeIn, int stay, int fadeOut) {
		if (player == null) { throw new IllegalArgumentException("null argument"); }
		PlayerVersion.Version version = getPlayerVersion().getVersion(player.getUniqueId());
		if (version == null || version.olderThan(PlayerVersion.Version.v1_8)) { return; }

		try {
			Object packetTitle;
			if (Minecraft.VERSION.olderThan(Minecraft.Version.v1_8_R1)) {
				packetTitle = PacketTitleConstructorResolver.resolve(new Class[] {
						EnumTitleAction,
						int.class,
						int.class,
						int.class }).newInstance(EnumTitleAction.getEnumConstants()[2], fadeIn, stay, fadeOut);
			} else {
				packetTitle = PacketTitleConstructorResolver.resolve(new Class[] {
						int.class,
						int.class,
						int.class }).newInstance(fadeIn, stay, fadeOut);
			}
			sendPacket(player, packetTitle);
		} catch (Exception e) {
			throw new RuntimeException("Failed to send Timings to " + player, e);
		}
	}

	//// Clear

	/**
	 * Clear the Player's Title
	 *
	 * @param player {@link Player} to be cleared
	 */
	public static void clear(Player player) {
		if (player == null) { throw new IllegalArgumentException("null argument"); }
		PlayerVersion.Version version = getPlayerVersion().getVersion(player.getUniqueId());
		if (version == null || version.olderThan(PlayerVersion.Version.v1_8)) { return; }

		try {
			Object packetTitle;
			if (Minecraft.VERSION.olderThan(Minecraft.Version.v1_8_R1)) {
				packetTitle = PacketTitleConstructorResolver.resolve(new Class[] {
						EnumTitleAction }).newInstance(EnumTitleAction.getEnumConstants()[3]);
			} else {
				packetTitle = PacketTitleConstructorResolver.resolve(new Class[] {
						EnumTitleAction,
						IChatBaseComponent }).newInstance(EnumTitleAction.getEnumConstants()[3], null);
			}
			sendPacket(player, packetTitle);
		} catch (Exception e) {
			throw new RuntimeException("Failed to send Clear to " + player, e);
		}
	}

	//// Reset

	/**
	 * Reset the Player's Timing, Title, SubTitle
	 *
	 * @param player {@link Player} to Reset
	 */
	public static void reset(Player player) {
		if (player == null) { throw new IllegalArgumentException("null argument"); }
		PlayerVersion.Version version = getPlayerVersion().getVersion(player.getUniqueId());
		if (version == null || version.olderThan(PlayerVersion.Version.v1_8)) { return; }

		try {
			Object packetTitle;
			if (Minecraft.VERSION.olderThan(Minecraft.Version.v1_8_R1)) {
				packetTitle = PacketTitleConstructorResolver.resolve(new Class[] {
						EnumTitleAction }).newInstance(EnumTitleAction.getEnumConstants()[4]);
			} else {
				packetTitle = PacketTitleConstructorResolver.resolve(new Class[] {
						EnumTitleAction,
						IChatBaseComponent }).newInstance(EnumTitleAction.getEnumConstants()[4], null);
			}
			sendPacket(player, packetTitle);
		} catch (Exception e) {
			throw new RuntimeException("Failed to send Reset to " + player, e);
		}
	}

	//Helper methods

	static Object serialize(String json) throws ReflectiveOperationException {
		return ChatSerailizerMethodResolver.resolve(new ResolverQuery("a", String.class)).invoke(null, json);
	}

	static void sendPacket(Player receiver, Object packet) throws ReflectiveOperationException {
		Object handle = Minecraft.getHandle(receiver);
		Object connection = EntityPlayerFieldResolver.resolve("playerConnection").get(handle);
		PlayerConnectionMethodResolver.resolve("sendPacket").invoke(connection, packet);
	}

	static IPlayerVersion getPlayerVersion() {
		if (TitlePlugin.playerVersion == null) { throw new RuntimeException("PlayerVersion could not be loaded!"); }
		return TitlePlugin.playerVersion;
	}

	@Override
	public void load() {
		APIManager.require(PacketListenerAPI.class, null);
		APIManager.require(PlayerVersion.class, null);
	}

	@Override
	public void init(Plugin plugin) {
		APIManager.initAPI(PacketListenerAPI.class);
		APIManager.initAPI(PlayerVersion.class);
		TitlePlugin.playerVersion = PlayerVersion.getInstance();
	}

	@Override
	public void disable(Plugin plugin) {
	}
}
