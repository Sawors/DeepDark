package io.github.sawors.deepdark;

import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

import java.util.*;

public class GameManager {
    
    //
    // STATIC
    //
    private static final Set<GameManager> liveGames = new HashSet<>();
    
    private static final String titleChar = "\uEff1";

    //
    // INSTANCE
    //
    private final Map<UUID, GameRole> playerList;
    private final UUID gameId;
    private final NoiseManager noiseManager;
    
    public GameManager(){
        playerList = new HashMap<>();
        UUID id = UUID.randomUUID();
        while(playerList.containsKey(id)){
            id = UUID.randomUUID();
        }
        gameId = id;
        noiseManager = new NoiseManager(this);
        
        // registering the game
        liveGames.add(this);
        
//        DeepDark.getProtocolManager().addPacketListener(new PacketAdapter(
//                DeepDark.getPlugin(),
//                ListenerPriority.NORMAL,
//                PacketType.Play.Server.PLAYER_INFO)
//            {
//                @Override
//                public void onPacketSending(PacketEvent event) {
//                    PacketContainer packet = event.getPacket().deepClone();
//
//                    logAdmin("SINGLE FIELD");
//                    StructureModifier<EnumWrappers.PlayerInfoAction> action = packet.getPlayerInfoAction();
//                    for(EnumWrappers.PlayerInfoAction accessor : action.getValues()){
//                        logAdmin(" single field",accessor.toString());
//                    }
//                    logAdmin("DATA FIELDS");
//                    for(List<PlayerInfoData> dataList : packet.getPlayerInfoDataLists().getValues()){
//                        logAdmin(" dataList",dataList);
//                        for(PlayerInfoData data : dataList){
//                            logAdmin(" data",data);
//                        }
//                    }
//                }
//            }
//        );
    }
    
    public void close() {
        
        for(UUID p : playerList.keySet()){
            removePlayer(p);
        }
        
        liveGames.remove(this);
        
        OpusDecoder decoder = getNoiseManager().decoder;
        if(decoder != null){
            decoder.close();
        }
        OpusEncoder encoder = getNoiseManager().encoder;
        if(encoder != null){
            encoder.close();
        }
    }
    
    public void addPlayer(Player player){
        noiseManager.trackPlayer(player);
        playerList.put(player.getUniqueId(),GameRole.LOBBY);
        player.sendPlayerListHeader(Component.text(titleChar).append(Component.newline()).append(Component.newline()).append(Component.text("       by Sawors       ").append(Component.newline()).color(TextColor.color(0x2e8a8c))));
        player.playerListName(Component.text("Lobby - ").append(player.displayName()).color(NamedTextColor.GRAY));
        player.sendPlayerListFooter(Component.text("game : "+gameId.toString().substring(0,gameId.toString().indexOf("-"))).color(NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, TextDecoration.State.TRUE));
    }
    
    /**
     *
     * @param playerId The Bukkit UUID of the player to remove. Using UUIDs allows to remove offline players too
     */
    public void removePlayer(UUID playerId){
        playerList.remove(playerId);
    }
    
    enum GameRole {
        SURVIVOR,
        WARDEN,
        SPECTATOR,
        LOBBY
    }
    
    /**
     *
     * @return An immutable copy of the player list and their roles
     */
    public Map<UUID, GameRole> getPlayerList() {
        return Map.copyOf(playerList);
    }
    
    public UUID getGameId() {
        return gameId;
    }
    
    public NoiseManager getNoiseManager() {
        return noiseManager;
    }
    
    /**
     *
     * @return a MUTABLE view of the games currently live. Every change done to this set will be done on the original set too.
     */
    public static Set<GameManager> getLiveGames(){
        return liveGames;
    }
}
