package me.fluffy.dactyl.module.impl.render;

import me.fluffy.dactyl.event.impl.world.Render3DEvent;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.module.impl.client.Colors;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.CombatUtil;
import me.fluffy.dactyl.util.render.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class VoidESP extends Module {
    Setting<Integer> holes = new Setting<Integer>("Holes", 50, 1, 50);
    Setting<Boolean> outline = new Setting<Boolean>("Outline", true);
    Setting<Double> lineWidth = new Setting<Double>("LineWidth", 1.5d, 0.5d, 3.0d, vi->outline.getValue());
    Setting<Boolean> box = new Setting<Boolean>("Box", true);
    Setting<Integer> boxAlpha = new Setting<Integer>("BoxAlpha", 45, 1, 255, vi->box.getValue());
    Setting<Double> renderRange = new Setting<Double>("RenderRange", 8.0d, 1.0d, 20.0d);
    Setting<Boolean> negativeTranslate = new Setting<Boolean>("NegTrans", false);
    Setting<Double> yTranslate = new Setting<Double>("TranslateY", 1.0d, 0.0d, 3.0d);
    Setting<Boolean> negativeExtrude = new Setting<Boolean>("NegExtrude", true);
    Setting<Double> yExtrude = new Setting<Double>("ExtrudeY", 1.0d, 0.0d, 3.0d);
    Setting<Boolean> colorSync = new Setting<Boolean>("ColorSync", false);
    Setting<Integer> red = new Setting<Integer>("Red", 5, 1, 255, v->!colorSync.getValue());
    Setting<Integer> green = new Setting<Integer>("Green", 175, 1, 255, v->!colorSync.getValue());
    Setting<Integer> blue = new Setting<Integer>("Blue", 255, 1, 255, v->!colorSync.getValue());

    private ArrayList<BlockPos> voidPositions;

    public VoidESP() {
        super("VoidESP", Category.RENDER);
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (mc.player == null || mc.world == null || this.voidPositions == null) {
            return;
        }
        if (this.voidPositions.isEmpty()) {
            return;
        }
        for(BlockPos voidHole : voidPositions) {
            double transY = negativeTranslate.getValue() ? (-yTranslate.getValue()) : yTranslate.getValue();
            double extY = negativeExtrude.getValue() ? (-yExtrude.getValue()) : yExtrude.getValue();
            RenderUtil.drawOffsetBox(voidHole, transY, extY, colorSync.getValue() ? Colors.INSTANCE.convertHex(Colors.INSTANCE.getColor(1, false)) : new Color(red.getValue(), green.getValue(), blue.getValue(), 255), lineWidth.getValue().floatValue(), outline.getValue(), box.getValue(), boxAlpha.getValue());
        }
    }

    @Override
    public void onClientUpdate() {
        if(mc.player == null || mc.world == null) {
            return;
        }
        updateHoles();
    }

    private void updateHoles() {
        if (this.voidPositions == null) {
            this.voidPositions = new ArrayList<>();
        } else {
            this.voidPositions.clear();
        }
        int range = (int)Math.ceil(((Double)this.renderRange.getValue()).doubleValue());
        List<BlockPos> blockPosList = CombatUtil.getSphere(CombatUtil.flooredPos(), range, range, false, true, 0);
        for (BlockPos pos : blockPosList) {
            if(mc.world.getBlockState(pos).getBlock().equals(Blocks.AIR) && pos.getY() < 1 && pos.getY() >= 0 && mc.world.getBlockState(pos.add(0, 1, 0)).getBlock().equals(Blocks.AIR)) {
                if (voidPositions.size() <= holes.getValue()) {
                    voidPositions.add(pos);
                }
            }
        }
    }
}
