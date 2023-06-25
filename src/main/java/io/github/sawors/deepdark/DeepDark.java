package io.github.sawors.deepdark;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import io.github.sawors.deepdark.commands.GameCommand;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.sql.Time;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public final class DeepDark extends JavaPlugin {
    
    private static Plugin instance;
    private static VoicechatPlugin vcplugin = null;
    private static ProtocolManager protocolManager = null;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        BukkitVoicechatService vcservice = getServer().getServicesManager().load(BukkitVoicechatService.class);
        if(vcservice != null){
            vcplugin = new VoiceChatIntegrationPlugin();
            vcservice.registerPlugin(vcplugin);
            logAdmin("Simple Voice Chat plugin detected, integration enabled");
        }
        
        protocolManager = ProtocolLibrary.getProtocolManager();
        
        getServer().getPluginManager().registerEvents(new NoiseManager(null),this);
        Objects.requireNonNull(getServer().getPluginCommand("deepdark")).setExecutor(new GameCommand());
        
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        for(GameManager manager : GameManager.getLiveGames()){
            manager.close();
        }
    }
    
    public static Plugin getPlugin(){
        return instance;
    }
    
    public static ProtocolManager getProtocolManager() {
        return protocolManager;
    }
    
    public static void logAdmin(Object msg) throws IllegalStateException{
        logAdmin(null,msg);
    }
    public static void logAdmin(@Nullable Object title, Object msg) throws IllegalStateException{
        String pluginname = getPlugin().getName();
        String inter = "";
        if(title != null && title.toString().length() > 0){
            inter = title+" : ";
        }
        
        String output = "["+ ChatColor.YELLOW+pluginname+" DEBUG"+ChatColor.WHITE+"-"+ Time.valueOf(LocalTime.now()) + "] "+inter+msg;
        Bukkit.getLogger().log(Level.INFO, output);
        for(Player p : Bukkit.getOnlinePlayers()){
            if(p.isOp()){
                p.sendMessage(Component.text(output));
            }
        }
    }
    
    private static final Set<UUID> toReload = new HashSet<>();
    @EventHandler
    public static void sendPlayerResourcePack(PlayerJoinEvent event){
        Player p = event.getPlayer();
        UUID pid = p.getUniqueId();
        p.setResourcePack("","");
    }
}
