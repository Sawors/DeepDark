package io.github.sawors.deepdark;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.awt.*;

public class DeepDarkUtils {
    
    public static Component gradientText(String text, int fromColor, int toColor){
        String[] letters = text.split("");
        Color source = new Color(fromColor);
        Color target = new Color(toColor);
        Component output = Component.empty();
        int redStep = (target.getRed()-source.getRed())/letters.length;
        int greenStep = (target.getGreen()-source.getGreen())/letters.length;
        int blueStep = (target.getBlue()-source.getBlue())/letters.length;
        for(int i = 0; i<letters.length; i++){
            output = output.append(Component.text(letters[i]).color(TextColor.color(source.getRed()+(redStep*i),source.getGreen()+(greenStep*i),source.getBlue()+(blueStep*i))));
        }
        return output;
    }
}
