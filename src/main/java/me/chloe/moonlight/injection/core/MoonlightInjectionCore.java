package me.chloe.moonlight.injection.core;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import javax.annotation.Nullable;
import java.util.Map;

@IFMLLoadingPlugin.Name(value = "Moonlight")
@IFMLLoadingPlugin.MCVersion(value = "1.12.2")
public class MoonlightInjectionCore implements IFMLLoadingPlugin{
    public MoonlightInjectionCore() {
        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.moonlight.json");
        MixinEnvironment.getDefaultEnvironment().setSide(MixinEnvironment.Side.CLIENT);
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return MoonlightAccessTransformer.class.getName();
    }
}
