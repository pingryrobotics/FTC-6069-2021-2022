package teamcode;

import com.qualcomm.ftccommon.SoundPlayer;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class Radio {
    private HardwareMap hardwareMap;

    public Radio(HardwareMap hardwareMap) {
        this.hardwareMap = hardwareMap;
    }

    public void playSound(SoundFiles soundFile) {
        int soundId = hardwareMap.appContext.getResources().getIdentifier(soundFile.filename, "raw", hardwareMap.appContext.getPackageName());
        if (soundId != 0) {
            SoundPlayer.getInstance().startPlaying(hardwareMap.appContext, soundId);
        }

    }

    public enum SoundFiles {
        FreightDetected("domination_sound");

        public final String filename;
        SoundFiles(String filename) {
            this.filename = filename;
        }

    }
}
