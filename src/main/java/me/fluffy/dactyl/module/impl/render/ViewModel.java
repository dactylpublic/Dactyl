package me.fluffy.dactyl.module.impl.render;

import me.fluffy.dactyl.injection.inj.access.IItemRenderer;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import net.minecraft.util.EnumHand;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ViewModel extends Module {
    public Setting<Boolean> fovChanger = new Setting<Boolean>("Fov", true);
    public Setting<Integer> fovAmount = new Setting<Integer>("FovAmount", 141, 60, 180, v->fovChanger.getValue());
    public Setting<Boolean> keepItemFov = new Setting<Boolean>("KeepItemFov", false, v->fovChanger.getValue());

    public Setting<Boolean> modifyHands = new Setting<Boolean>("ModifyHands", false);
    public Setting<Boolean> modifyMainHand = new Setting<Boolean>("ModifyMain", true, v->modifyHands.getValue());
    public Setting<Double> mainOffset = new Setting<Double>("Mainhand", 1.0d, 0.1d, 3.0d, v->modifyHands.getValue() && modifyMainHand.getValue());
    public Setting<Boolean> modifyOffHand = new Setting<Boolean>("ModifyOffhand", true, v->modifyHands.getValue());
    public Setting<Double> offOffset = new Setting<Double>("Offhand", 0.7d, 0.1d, 3.0d, v->modifyHands.getValue() && modifyOffHand.getValue());

    public static ViewModel INSTANCE;
    public ViewModel() {
        super("ViewModel", Category.RENDER);
        INSTANCE = this;
    }

    @SubscribeEvent
    public void onViewRender(EntityViewRenderEvent.FOVModifier event) {
        if(keepItemFov.getValue()) {
            event.setFOV(fovAmount.getValue().floatValue());
        }
    }


    @Override
    public void onClientUpdate() {
        if(mc.player == null || mc.gameSettings == null) {
            return;
        }
        if(!keepItemFov.getValue()) {
            mc.gameSettings.fovSetting = fovAmount.getValue().floatValue();
        }

        if(modifyHands.getValue()) {
            if(modifyMainHand.getValue()) {
                ((IItemRenderer)mc.entityRenderer.itemRenderer).setProgressMain(mainOffset.getValue().floatValue());
            }
            if(modifyOffHand.getValue()) {
                ((IItemRenderer)mc.entityRenderer.itemRenderer).setProgreessOff(offOffset.getValue().floatValue());
            }
        }
    }

    @Override
    public void onDisable() {
        mc.gameSettings.fovSetting = 120;
    }

    public void updateEquipped() {
        ((IItemRenderer)mc.entityRenderer.itemRenderer).setItemStackMain(mc.player.getHeldItem(EnumHand.MAIN_HAND));
        ((IItemRenderer)mc.entityRenderer.itemRenderer).setItemStackOff(mc.player.getHeldItem(EnumHand.OFF_HAND));
    }
}
