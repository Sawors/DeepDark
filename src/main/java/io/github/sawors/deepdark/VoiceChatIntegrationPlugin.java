package io.github.sawors.deepdark;


import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;

public class VoiceChatIntegrationPlugin implements VoicechatPlugin {
    
    private static VoicechatApi vcApi;
    public static VoicechatServerApi voicechatServerApi;
    
    /**
     * @return the unique ID for this voice chat plugin
     */
    @Override
    public String getPluginId() {
        return DeepDark.getPlugin().getName();
    }
    
    /**
     * Called when the voice chat initializes the plugin.
     *
     * @param api the voice chat API
     */
    @Override
    public void initialize(VoicechatApi api) {
        vcApi = api;
    }
    
    /**
     * Called once by the voice chat to register all events.
     *
     * @param registration the event registration
     */
    @Override
    public void registerEvents(EventRegistration registration) {
        // TODO register your events
        registration.registerEvent(MicrophonePacketEvent.class, NoiseManager::copySendPacket);
        registration.registerEvent(VoicechatServerStartedEvent.class, VoiceChatIntegrationPlugin::onServerStarted);
    }
    
    private static void onServerStarted(VoicechatServerStartedEvent event) {
        voicechatServerApi = event.getVoicechat();
    }
    
    public static VoicechatServerApi getVoicechatServerApi(){
        return voicechatServerApi;
    }
    
    public static VoicechatApi getVoicechatApi(){
        return vcApi;
    }
}
