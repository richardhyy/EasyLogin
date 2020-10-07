package cc.eumc.easylogin;

import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;

public class NEffect {
    /*
    public final static DropShadow primaryDropShadowSE = new DropShadow(BlurType.THREE_PASS_BOX, NColor.primaryButtonDarkColor, 14.5, 0, 5, 5);
    public final static DropShadow primaryDropShadowNW = new DropShadow(BlurType.THREE_PASS_BOX, NColor.primaryButtonLightColor, 14.5, 0, -3, -3);
    public final static DropShadow primaryNormalShadow;

    static {
        DropShadow primaryDropShadow = primaryDropShadowSE;
        primaryDropShadow.setInput(primaryDropShadowNW);
        primaryNormalShadow = primaryDropShadow;
    }

    public final static InnerShadow primaryInnerShadowSE = new InnerShadow(BlurType.THREE_PASS_BOX, NColor.primaryButtonDarkColor, 14.5, 0, 5, 5);
    public final static InnerShadow primaryInnerShadowNW = new InnerShadow(BlurType.THREE_PASS_BOX, NColor.primaryButtonLightColor, 14.5, 0, -3, -3);
    public final static InnerShadow primaryPressedShadow;

    static {
        InnerShadow primaryInnerShadow = primaryInnerShadowSE;
        primaryInnerShadow.setInput(primaryInnerShadowNW);
        primaryPressedShadow = primaryInnerShadow;
    }
    */
    public static DropShadow getDropShadow(double depth) {
        DropShadow primaryDropShadowSE = new DropShadow(BlurType.THREE_PASS_BOX, NColor.primaryButtonDarkColor, depth, 0, depth/2.9, depth/2.9);
        DropShadow primaryDropShadowNW = new DropShadow(BlurType.THREE_PASS_BOX, NColor.primaryButtonLightColor, depth, 0, depth/-4.7, depth/-4.7);

        primaryDropShadowSE.setInput(primaryDropShadowNW);
        return primaryDropShadowSE;
    }

    public static InnerShadow getInnerShadow(double depth) {
        InnerShadow primaryInnerShadowSE = new InnerShadow(BlurType.THREE_PASS_BOX, NColor.primaryButtonDarkColor, depth, 0, depth/3, depth/3);
        InnerShadow primaryInnerShadowNW = new InnerShadow(BlurType.THREE_PASS_BOX, NColor.primaryButtonLightColor, depth, 0, depth/-5, depth/-5);

        primaryInnerShadowSE.setInput(primaryInnerShadowNW);
        return primaryInnerShadowSE;
    }
}
