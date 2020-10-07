package cc.eumc.easylogin.animation;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class UniversalAnimation {
    public static void playMovementAnimation(Rectangle rectangle, double toY) {
        final Timeline timeline = new Timeline();
        final KeyValue kv = new KeyValue(rectangle.yProperty(), toY);
        final KeyFrame kf = new KeyFrame(Duration.millis(256), kv);
        timeline.getKeyFrames().add(kf);
        timeline.play();

        /*
        final double fromY = rectangle.getY();
        final double step = Math.abs(toY - fromY) / 10;
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                rectangle.setY(rectangle.getY() + (rectangle.getY()>toY ? -step : step));
                if ((fromY < toY && rectangle.getY() >= toY) || (fromY > toY && rectangle.getY() <= toY)) {
                    rectangle.setY(toY);
                    this.stop();
                }
            }
        };
        timer.start();*/
    }

    public static void playBlurAnimation(AnchorPane anchorPane, double fromRadius, int delta) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> playBlurAnimation(anchorPane, fromRadius, delta));
            return;
        }

        final int[] radius = {(int) fromRadius};
        boolean increment = (radius[0] == 0);
        final int[] delta_ = {delta};
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (delta_[0] == 0) {
                    this.stop();
                }
                else {
                    radius[0] += (increment?2:-2);
                    anchorPane.setEffect(new GaussianBlur(radius[0]));
                    delta_[0] -=2;
                }
            }
        };
        timer.start();
    }
}
