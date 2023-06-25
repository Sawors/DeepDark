package io.github.sawors.deepdark;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import io.github.sawors.deepdark.commands.GameCommand;
import io.github.sawors.deepdark.roles.GameRole;
import io.github.sawors.deepdark.roles.monster.WardenRole;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Time;
import java.time.LocalTime;
import java.util.*;
import java.util.logging.Level;

public final class DeepDark extends JavaPlugin implements Listener {
    
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
        getServer().getPluginManager().registerEvents(this,this);
        getServer().getPluginManager().registerEvents(new GameRole() {@Override public void spawnRoleSelector(Location location, Vector direction) {}}, this);
        Objects.requireNonNull(getServer().getPluginCommand("deepdark")).setExecutor(new GameCommand());
        
        
        logAdmin("TRIGGER");
        for(World w : Bukkit.getWorlds()){
            logAdmin("world",w.getName());
            for(ItemDisplay e : w.getEntitiesByClass(ItemDisplay.class)){
                logAdmin("EEE");
                if(e.getPersistentDataContainer().has(WardenRole.getPingEntityKey())){
                    logAdmin("TO REMOVE");
                    e.remove();
                }
            }
        }
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
    private static final String packUrl = "https://github.com/Sawors/DeepDark/raw/main/resourcepack/DeepDarkRP.zip";
    private static final String packHashUrl = "https://raw.githubusercontent.com/Sawors/DeepDark/main/resourcepack/sha1.txt";
    @EventHandler
    public static void sendPlayerResourcePack(PlayerJoinEvent event){
        Player p = event.getPlayer();
        p.sendPlayerListHeader(Component.text(" "+GameManager.titleChar+" ").append(Component.newline()).append(Component.newline()).append(DeepDarkUtils.gradientText("by Sawors",0x153b48,0x51dde9,0x1f5a54,0x1f5a54)).append(Component.newline()));
        try(InputStream in = new URL(packHashUrl).openStream(); Scanner reader = new Scanner(in, StandardCharsets.UTF_8)){
            p.setResourcePack(packUrl,reader.next());
        } catch (IOException e){
            e.printStackTrace();
        }
        toReload.add(p.getUniqueId());
        
        new BukkitRunnable(){
            @Override
            public void run() {
                VoicechatConnection vcCo = VoiceChatIntegrationPlugin.getVoicechatServerApi().getConnectionOf(p.getUniqueId());
                if(vcCo != null && !vcCo.isInstalled()){
                    p.kick(Component.text("Please install the Simple Voice Chat mod in order to play this gamemode !\nhttps://modrinth.com/plugin/simple-voice-chat/versions").color(NamedTextColor.RED));
                    this.cancel();
                } else if(vcCo != null && vcCo.isConnected()){
                    p.sendMessage(Component.text("Voice chat connection established !").color(NamedTextColor.GREEN));
                    this.cancel();
                }
            }
        }.runTaskTimer(getPlugin(),40,20);
    }
    
    @EventHandler
    public static void reloadPack(PlayerResourcePackStatusEvent event){
        Player p = event.getPlayer();
        logAdmin(event.getStatus());
        if(toReload.contains(p.getUniqueId()) && event.getStatus().equals(PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED)){
            try(InputStream in = new URL(packHashUrl).openStream(); Scanner reader = new Scanner(in, StandardCharsets.UTF_8)){
                p.setResourcePack(packUrl,reader.next());
            } catch (IOException e){
                e.printStackTrace();
            }
            toReload.remove(p.getUniqueId());
        }
    }
}
