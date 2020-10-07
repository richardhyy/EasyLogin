package cc.eumc.easylogin.animation;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.control.ProgressBar;

import java.util.HashMap;
import java.util.Map;

public class NProgressBarAnimation {
    private static Map<ProgressBar, AnimationTimer> lastTimer = new HashMap<>();

    public static void setProgress(ProgressBar progressBar, double value) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> setProgress(progressBar, value));
            return;
        }

        if (lastTimer.containsKey(progressBar)) {
            lastTimer.get(progressBar).stop();
            lastTimer.remove(progressBar);
        }

        if (value == -1) {
            progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
            return;
        }

        double abs = Math.abs(progressBar.getProgress() - value);
        double step = abs / (abs > 0.3 ? 40 : 20);

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
            if (progressBar.getProgress() == value) {
                this.stop();
                lastTimer.remove(progressBar);
                return;
            }

            progressBar.setProgress(progressBar.getProgress() + (value>progressBar.getProgress()? step : -step));
            }
        };

        lastTimer.put(progressBar, timer);
        timer.start();
    }
}
