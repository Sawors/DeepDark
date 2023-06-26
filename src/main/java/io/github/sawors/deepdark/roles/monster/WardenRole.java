package io.github.sawors.deepdark.roles.monster;

import io.github.sawors.deepdark.DeepDark;
import io.github.sawors.deepdark.roles.GameRole;
import io.papermc.paper.event.entity.EntityMoveEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.SculkShrieker;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockReceiveGameEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class WardenRole extends GameRole {
    
    
    public WardenRole(Player player){
        this.holder = player;
        this.roleName = "Warden";
        this.type = GameRoleType.WARDEN;
    }
    
    @EventHandler
    public static void sendPing(BlockReceiveGameEvent event){
        if(event.getBlock().getType().equals(Material.SCULK_SHRIEKER) && !(event.getEntity() instanceof Bat)){
            Player p = Bukkit.getPlayer("Sawors");
            if(p != null && p.isOnline()){
                if(event.getBlock().getLocation().distance(p.getLocation()) <= 10*16 && !((SculkShrieker)event.getBlock().getBlockData()).isShrieking()){
                    WardenRole warden = new WardenRole(p);
                    warden.sendPing(event.getBlock().getLocation(), 3,PingType.BLOCK);
                }
            }
        } else if(event.getBlock().getType().equals(Material.SCULK_SENSOR) && event.getEntity() != null){
            if(event.getEntity() instanceof Player p && p.getUniqueId().toString().startsWith("f9")){
                //event.setCancelled(true);
            } else {
            
            }
            Player p = Bukkit.getPlayer("Sawors");
            if(p != null && p.isOnline()){
                WardenRole warden = new WardenRole(p);
                warden.sendPing(event.getEntity().getLocation().add(0,.25,0), 1,PingType.BLOCK);
            }
        }
    }
    
    public enum PingType {
        BLOCK,
        ENTITY
    }
    
    public void sendPing(Location pingLocation, int level, PingType type){
        if(level == 3){
            this.holder.playSound(holder.getLocation().add(pingLocation.toVector().subtract(holder.getLocation().toVector()).normalize().multiply(5)), Sound.BLOCK_SCULK_SHRIEKER_SHRIEK,.75f,.75f);
        }
        Block ref = pingLocation.getBlock();
        pingLocation.getWorld().spawnEntity(level == 3 ? ref.getLocation().add(.5,0,.5) : pingLocation, EntityType.ITEM_DISPLAY, CreatureSpawnEvent.SpawnReason.CUSTOM, e -> {
            if(e instanceof ItemDisplay display){
                
                display.setGlowing(true);
                if(level == 3){
                    display.setGlowColorOverride(Color.RED);
                    //display.setGlowColorOverride(Color.ORANGE);
                    display.setItemStack(new ItemStack(Material.ENDER_EYE));
                } else if(level == 1){
                    display.setGlowColorOverride(Color.fromRGB(0x51DEE8));
                    //display.setGlowColorOverride(Color.ORANGE);
                    display.setItemStack(new ItemStack(Material.ENDER_PEARL));
                }
                display.setBillboard(Display.Billboard.CENTER);
                display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.GROUND);
                display.getPersistentDataContainer().set(getPingEntityKey(), PersistentDataType.STRING,type.toString());
                
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        e.remove();
                    }
                }.runTaskLater(DeepDark.getPlugin(),20*(level == 3 ? 20 : 3));
            }
        });
    }
    
    @EventHandler
    public static void trackStep(PlayerJoinEvent event){
        //getTrackingRunnable(event.getPlayer()).runTaskLater(DeepDark.getPlugin(),5);
    }
    
    @EventHandler
    public static void track(EntityMoveEvent event){
        if(event.hasChangedBlock()){
            Entity follow = event.getEntity();
            if(follow.getType().equals(EntityType.VILLAGER)){
                Block bellow = follow.getLocation().subtract(0,1,0).getBlock();
                Location sourceLoc = follow.getLocation();
                Location floorLoc = new Location(sourceLoc.getWorld(),sourceLoc.x(),bellow.getY()+1,sourceLoc.z());
                if(bellow.getType().isSolid()){
                    follow.getWorld().spawnEntity(floorLoc, EntityType.ITEM_DISPLAY, CreatureSpawnEvent.SpawnReason.CUSTOM, e -> {
                        if(e instanceof ItemDisplay display){
                            
                            display.setGlowing(true);
                            //display.setGlowColorOverride(Color.fromRGB(0x51DEE8));
                            display.setGlowColorOverride(Color.ORANGE);
                            display.setItemStack(new ItemStack(Material.NETHERITE_SCRAP));
                            display.setBillboard(Display.Billboard.FIXED);
                            display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
                            display.getPersistentDataContainer().set(getPingEntityKey(), PersistentDataType.STRING,PingType.ENTITY.toString());
                            display.setRotation(follow.getLocation().getYaw(),90);
                            
                            new BukkitRunnable(){
                                @Override
                                public void run() {
                                    e.remove();
                                }
                            }.runTaskLater(DeepDark.getPlugin(),20*2);
                        }
                    });
                }
            }
        }
    }
    
    private static BukkitRunnable getTrackingRunnable(Player tracked){
        return new BukkitRunnable(){
            final Player follow = tracked;
            
            @Override
            public void run() {
                if(!follow.isOnline()){
                    this.cancel();
                    return;
                }
                Block bellow = follow.getLocation().subtract(0,1,0).getBlock();
                if(bellow.getType().isSolid()){
                    follow.getWorld().spawnEntity(follow.getLocation(), EntityType.ITEM_DISPLAY, CreatureSpawnEvent.SpawnReason.CUSTOM, e -> {
                        if(e instanceof ItemDisplay display){
                            
                            display.setGlowing(true);
                            //display.setGlowColorOverride(Color.fromRGB(0x51DEE8));
                            display.setGlowColorOverride(Color.ORANGE);
                            display.setItemStack(new ItemStack(Material.NETHERITE_SCRAP));
                            display.setBillboard(Display.Billboard.FIXED);
                            display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
                            display.getPersistentDataContainer().set(getPingEntityKey(), PersistentDataType.STRING,PingType.ENTITY.toString());
                            display.setRotation(follow.getBodyYaw(),90);
                            
                            new BukkitRunnable(){
                                @Override
                                public void run() {
                                    e.remove();
                                }
                            }.runTaskLater(DeepDark.getPlugin(),20*2);
                        }
                    });
                }
                getTrackingRunnable(follow).runTaskLater(DeepDark.getPlugin(),follow.isSneaking() ? 12 : follow.isSprinting() ? 4 : 5);
            }
        };
    }
    
    @Override
    public void spawnRoleSelector(Location location, Vector direction) {
    
    }
    
    public static NamespacedKey getPingEntityKey(){
        return new NamespacedKey(DeepDark.getPlugin(),"ping_entity");
    }
}
