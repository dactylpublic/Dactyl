package me.fluffy.dactyl.injection.core;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import javax.annotation.Nullable;
import java.util.Map;

@IFMLLoadingPlugin.TransformerExclusions(value = "me.coderguy.dactyl.injection.core")
@IFMLLoadingPlugin.Name(value = "Dactyl")
@IFMLLoadingPlugin.MCVersion(value = "1.12.2")
public class DactylInjectionCore implements IFMLLoadingPlugin{
    public DactylInjectionCore() {
        /*MixinBootstrap.init();
        Mixins.addConfiguration("mixins.yuno.json");
        MixinEnvironment.getDefaultEnvironment().setSide(MixinEnvironment.Side.CLIENT);*/
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
        return DactylAccessTransformer.class.getName();
    }
}
