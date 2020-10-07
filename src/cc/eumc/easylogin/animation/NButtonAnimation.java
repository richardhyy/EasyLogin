package cc.eumc.easylogin.animation;

import javafx.animation.AnimationTimer;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class NButtonAnimation {
    public static void playToggleShadowAnimation(DropShadow shadow, boolean show) {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if ((show && shadow.getColor().getOpacity() >= 0.95) || (!show && shadow.getColor().getOpacity() <= 0.05)) {
                    System.out.println("stopped");
                    this.stop();
                }
                else {
                    shadow.setColor(Color.color(shadow.getColor().getRed(), shadow.getColor().getGreen(), shadow.getColor().getBlue(), show?shadow.getColor().getOpacity()*2 : shadow.getColor().getOpacity()/2));
                }
            }
        };
        timer.start();
    }

    public static void playToggleShadowAnimation(InnerShadow shadow, boolean show) {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if ((show && shadow.getColor().getOpacity() >= 0.95) || (!show && shadow.getColor().getOpacity() <= 0.05)) {
                    System.out.println("stopped");
                    this.stop();
                }
                else {
                    shadow.setColor(Color.color(shadow.getColor().getRed(), shadow.getColor().getGreen(), shadow.getColor().getBlue(), show?shadow.getColor().getOpacity()*2 : shadow.getColor().getOpacity()/2));
                }
            }
        };
        timer.start();
    }
}
