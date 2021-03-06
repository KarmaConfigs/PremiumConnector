package eu.horyzon.premiumconnector.listeners;

import java.sql.SQLException;

import eu.horyzon.premiumconnector.PremiumConnector;
import eu.horyzon.premiumconnector.session.PlayerSession;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent.Reason;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class ServerConnectListener implements Listener {
	private PremiumConnector plugin;

	public ServerConnectListener(PremiumConnector plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onServerConnect(ServerConnectEvent event) {
		if (event.isCancelled())
			return;

		if (event.getReason() != Reason.JOIN_PROXY) {
			event.setCancelled(plugin.isBlockServerSwitch() && plugin.getRedirectionRequests().containsKey(event.getPlayer().getName().toLowerCase()));
			return;
		}

		ProxiedPlayer player = event.getPlayer();
		String name = player.getName();
		if (!player.getPendingConnection().isOnlineMode()) {
			plugin.getRedirectionRequests().put(name.toLowerCase(), event.getTarget());
			plugin.getLogger().fine("Cracked player " + name + " was redirected on the cracked server " + plugin.getCrackedServer().getName());
			event.setTarget(plugin.getCrackedServer());
		}

		try {
			String address = player.getPendingConnection().getSocketAddress().toString();
			PlayerSession session = plugin.getPlayerSession().remove(name + address.substring(1, address.indexOf(':')));
			if (session != null)
				session.update();
		} catch (SQLException e) {
			e.printStackTrace();
			plugin.getLogger().warning("SQL error on updating player" + name);
		}
	}
}