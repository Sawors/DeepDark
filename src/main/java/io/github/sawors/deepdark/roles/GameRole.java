package io.github.sawors.deepdark.roles;

import io.github.sawors.deepdark.DeepDark;
import io.github.sawors.deepdark.roles.monster.WardenRole;
import io.github.sawors.deepdark.roles.survivor.MinerRole;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.Vector;

import java.util.Locale;

public abstract class GameRole implements Listener {
    
    protected GameRoleType type;
    protected String roleName;
    protected Player holder;
    
    public abstract void spawnRoleSelector(Location location, Vector direction);
    
    public ItemStack getRoleItemSelector(){
        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(getId().hashCode());
        
        item.setItemMeta(meta);
        return item;
    }
    
    public enum GameRoleType {
        SURVIVOR,
        WARDEN,
        SPECTATOR,
        LOBBY
    }
    
    public String getId(){
        return roleName.toLowerCase(Locale.ROOT).replace(" ","_");
    }
    
    @EventHandler
    public static void registerEvents(PluginEnableEvent event){
        if(event.getPlugin().equals(DeepDark.getPlugin())){
            PluginManager registerer = Bukkit.getPluginManager();
            registerer.registerEvents(new WardenRole(null),DeepDark.getPlugin());
            registerer.registerEvents(new MinerRole(),DeepDark.getPlugin());
        }
    }
}
