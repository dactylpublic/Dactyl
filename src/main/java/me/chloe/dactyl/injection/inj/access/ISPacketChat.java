package me.chloe.dactyl.injection.inj.access;

import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SPacketChat.class)
public interface ISPacketChat {
    @Accessor("chatComponent")
    public void setChatComponent(ITextComponent textComponent);
}
