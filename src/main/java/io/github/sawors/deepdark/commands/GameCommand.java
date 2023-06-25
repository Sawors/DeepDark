package io.github.sawors.deepdark.commands;

import io.github.sawors.deepdark.DeepDarkUtils;
import io.github.sawors.deepdark.GameManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class GameCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length >= 1){
            String sub = args[0];
            switch (sub) {
                case "create" -> {
                    GameManager manager = new GameManager();
                    sender.sendMessage(
                            Component.text("New game created ! (id "+manager.getGameId().toString()+")")
                            .hoverEvent(Component.text("Click to join"))
                            .clickEvent(ClickEvent.runCommand("/deepdark join "+manager.getGameId()))
                    );
                }
                case "close" -> {
                
                }
                case "join" -> {
                    if(sender instanceof Player p && args.length >= 2){
                        String gameId = args[1];
                        GameManager manager = GameManager.getLiveGames().stream().filter(m -> m.getGameId().toString().startsWith(gameId)).findFirst().orElse(null);
                        if(manager != null){
                            sender.sendMessage(Component.text("You successfully joined game "+gameId));
                            manager.addPlayer(p);
                        } else {
                            sender.sendMessage(Component.text("Game "+gameId+" not found !").color(NamedTextColor.RED));
                        }
                        return true;
                    }
                }
                case "leave", "quit" -> {
                    if(sender instanceof Player player) {
                        GameManager.getLiveGames().stream().filter(m -> m.getPlayerList().containsKey(player.getUniqueId())).findFirst().ifPresent(manager -> manager.removePlayer(player.getUniqueId()));
                    }
                    return true;
                }
                case "edit" -> {
                
                }
                case "list" -> {
                
                }
                case "players" -> {
                
                }
                case "test" -> {
                    //TextColor.color(0x10272e),
                    //                TextColor.color(0x153b48),
                    //                TextColor.color(0x1f5a54),
                    //                TextColor.color(0x2e8a8c),
                    //                TextColor.color(0x51dde9)
                    sender.sendMessage(DeepDarkUtils.gradientText(args.length >= 2 ? Arrays.stream(Arrays.copyOfRange(args,1,args.length)).reduce((s1,s2) -> s1+" "+s2).orElse(""): "Sawors",0x153b48,0x51dde9,0x1f5a54));
                }
            }
        }
        return false;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length == 1){
            return List.of(
                    "create",
                    "join",
                    "edit",
                    "list",
                    "players",
                    "close",
                    "leave",
                    "test"
            );
        }
        return null;
    }
}
