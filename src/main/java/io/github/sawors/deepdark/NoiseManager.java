package io.github.sawors.deepdark;

import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import de.maxhenkel.voicechat.api.packets.MicrophonePacket;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.GameEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class NoiseManager implements Listener {
    
    private static final double threshold = -45; // the minimum volume from which voice is considered
    private static final double highVolume = -20; // tier 3 loudness (in dB relative to 0, lower values mean easier activation)
    private static final double midVolume = -25; // tier 2 loudness (in dB relative to 0, lower values mean easier activation)
    private static final double lowVolume = -30; // tier 1 loudness (in dB relative to 0, lower values mean easier activation)
    private static final int indicatorRefreshRate = 1;
    
    private final Map<UUID, BukkitTask> voiceIndicators = new HashMap<>();
    private final Map<UUID, List<Double>> volumeBuffer = new HashMap<>();
    
    protected OpusEncoder encoder = null;
    protected OpusDecoder decoder = null;
    
    private final GameManager manager;
    
    public NoiseManager(GameManager manager){
        this.manager = manager;
    }
    
    @EventHandler
    public static void onPlayerConnect(PlayerJoinEvent event){
        
        GameManager playerManager = GameManager.getLiveGames().stream().filter(m -> m.getPlayerList().containsKey(event.getPlayer().getUniqueId())).findFirst().orElse(null);
        if(playerManager == null){
            return;
        }
        
        NoiseManager noiseManager = playerManager.getNoiseManager();
        
        BukkitTask runner = new BukkitRunnable() {
            
            final Player tracked = event.getPlayer();
            
            @Override
            public void run() {
                if(!tracked.isOnline()){
                    this.cancel();
                    return;
                }
                
                
                List<Double> buffer = List.copyOf(playerManager.getNoiseManager().volumeBuffer.getOrDefault(tracked.getUniqueId(),new ArrayList<>()));
                
                double mean = buffer.stream().reduce(Double::sum).orElse(threshold-5)/Math.max(buffer.size(),1);
                int loudness = mean >= highVolume ? 3 : mean >= midVolume ? 2 : mean >= lowVolume ? 1 : mean >= threshold ? 0 : -1;
                GameEvent sculkTrigger = null;
                if(loudness == 2) {
                    sculkTrigger = GameEvent.RESONATE_8;
                } else if(loudness == 3){
                    sculkTrigger = GameEvent.RESONATE_12;
                }
                tracked.sendActionBar(buildIndicator(loudness).decoration(TextDecoration.BOLD, TextDecoration.State.TRUE));
                noiseManager.volumeBuffer.put(tracked.getUniqueId(),new ArrayList<>());
                if(sculkTrigger != null){
                    tracked.getWorld().sendGameEvent(tracked,sculkTrigger,tracked.getLocation().toVector());
                }
            }
        }.runTaskTimer(DeepDark.getPlugin(),10,indicatorRefreshRate);
        noiseManager.voiceIndicators.put(event.getPlayer().getUniqueId(),runner);
    }
    
    public static void copySendPacket(MicrophonePacketEvent event){
        
        if(Bukkit.getOnlinePlayers().size() < 1){
            return;
        }
        
        // The connection might be null if the event is caused by other means
        if (event.getSenderConnection() == null) {
            return;
        }
        // Cast the generic player object of the voice chat API to an actual bukkit player
        // This object should always be a bukkit player object on bukkit based servers
        if ((event.getSenderConnection().getPlayer().getPlayer() instanceof Player player)) {
            
            GameManager playerManager = GameManager.getLiveGames().stream().filter(m -> m.getPlayerList().containsKey(player.getUniqueId())).findFirst().orElse(null);
            if(playerManager == null){
                return;
            }
            
            VoicechatServerApi api = event.getVoicechat();
            MicrophonePacket packet = event.getPacket();
            if(decoder == null){
                decoder = api.createDecoder();
            }
            if(encoder == null){
                encoder = api.createEncoder();
            }
            short[] samples = decoder.decode(packet.getOpusEncodedData());
            double rms = 0.0;
            
            
            for (short value : samples) {
                double sample = (double) value / (double) Short.MAX_VALUE;
                rms += sample * sample;
            }
            
            int sampleCount = samples.length / 2;
            
            rms = (sampleCount == 0) ? 0 : Math.sqrt(rms / sampleCount);
            
            double db;
            
            if (rms > 0D) {
                db = Math.min(Math.max(20D * Math.log10(rms), -127D), 0D);
            } else {
                db = -127D;
            }
            if(db > threshold){
                List<Double> buffer = volumeBuffer.get(player.getUniqueId());
                if(buffer == null){
                    buffer = new ArrayList<>();
                    buffer.add(db);
                    volumeBuffer.put(player.getUniqueId(),buffer);
                } else {
                    buffer.add(db);
                }
            }
            
            //api.sendStaticSoundPacketTo(connection, event.getPacket().staticSoundPacketBuilder().build());
        }
    }
    
    private static Component buildIndicator(int level){
        
        TextColor[] colors = new TextColor[]{
                TextColor.color(0x10272e),
                TextColor.color(0x153b48),
                TextColor.color(0x1f5a54),
                TextColor.color(0x2e8a8c),
                TextColor.color(0x51dde9)
        };
        
        if(level < 0){
            return Component.text("-").color(colors[0]);
        } else if (level == 0){
            return Component.text("O").color(colors[1]);
        }
        
        Component left  = Component.text("(");
        Component core  = buildIndicator(level-1);
        Component right = Component.text(")");
        
        
        return left.color(colors[level+1]).append(core).append(right.color(colors[level+1]));
    }
}
