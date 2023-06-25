package io.github.sawors.deepdark;

import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;

import java.util.*;

public class GameManager {
    
    //
    // STATIC
    //
    private static final Set<GameManager> liveGames = new HashSet<>();

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
    }
    
    public void close() {
        
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
    
    enum GameRole {
        SURVIVOR,
        WARDEN,
        SPECTATOR
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