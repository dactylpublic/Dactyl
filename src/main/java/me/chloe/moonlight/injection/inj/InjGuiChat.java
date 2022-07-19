package me.chloe.moonlight.injection.inj;

import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiChat.class)
public class InjGuiChat extends GuiScreen {
    @Shadow
    public GuiTextField inputField;

    public InjGuiChat() {
        InjGuiChat injGuiChat;
    }

    int curr = 0;
    int divisor = 0;
    boolean finished = false;
    @Inject(method = "initGui", at = @At("INVOKE"))
    private void initGui(CallbackInfo callbackInfo) {
        curr = 0;
        divisor = 0;
        finished = false;
    }

    @Redirect(method={"drawScreen"}, at=@At(value="INVOKE", target="net/minecraft/client/gui/GuiChat.drawRect(IIIII)V"))
    private void drawScreen(int n6, int n2, int n3, int n4, int n5) {
        if (!finished) {
            inputField.setFocused(Display.isActive());
            divisor++;
            if(divisor >= 100) {
                divisor = 0;
            }
            if(divisor % 2 == 0.0) {
                curr++;
            }
            if(curr >= 14) {
                finished = true;
            }
            InjGuiChat.drawRect((int)n6, (int)((this.height - Math.min(curr, 14))), (int)n3, (int)n4, (int)n5);
        } else {
            InjGuiChat.drawRect((int)n6, (int)n2, (int)n3, (int)n4, (int)n5);
        }
    }
}
