package me.chloe.moonlight.util;

import net.minecraft.client.Minecraft;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.File;

public class SoundUtil {
    private static File file = new File(Minecraft.getMinecraft().gameDir.getAbsolutePath(), "\\Dactyl\\startup.wav");

    public static void playStartupSound() {
        try
        {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            FloatControl gainControl =
                    (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            gainControl.setValue(-27.5f);
            clip.start();
        }

        catch (Exception ex)
        {
        }
    }
}
