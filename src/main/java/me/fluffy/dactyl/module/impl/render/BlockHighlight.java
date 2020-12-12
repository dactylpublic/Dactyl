package me.fluffy.dactyl.module.impl.render;

import me.fluffy.dactyl.event.impl.world.Render3DEvent;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.module.impl.client.Colors;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.render.RenderUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

import java.awt.*;

public class BlockHighlight extends Module {
    Setting<Boolean> colorSync = new Setting<Boolean>("ColorSync", false);
    Setting<Integer> red = new Setting<Integer>("Red", 5, 1, 255, v->!colorSync.getValue());
    Setting<Integer> green = new Setting<Integer>("Green", 5, 1, 255, v->!colorSync.getValue());
    Setting<Integer> blue = new Setting<Integer>("Blue", 5, 1, 255, v->!colorSync.getValue());
    Setting<Double> width = new Setting<Double>("Width", 1.5d, 0.1d, 3.0d);
    public BlockHighlight() {
        super("BlockHighlight", Category.RENDER);
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if(mc.objectMouseOver == null) {
            return;
        }
        RayTraceResult rayTraceResult = mc.objectMouseOver;
        if (rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
            BlockPos blockpos = rayTraceResult.getBlockPos();
            if(blockpos != null) {
                if(colorSync.getValue()) {
                    RenderUtil.drawBoxESP(blockpos, Colors.INSTANCE.convertHex(Colors.INSTANCE.getColor(1, false)), width.getValue().floatValue(), true, false, 0);
                } else {
                    RenderUtil.drawBoxESP(blockpos, new Color(red.getValue(), green.getValue(), blue.getValue(), 255), width.getValue().floatValue(), true, false, 0);
                }
            }
        }
    }
}
