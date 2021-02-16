package me.fluffy.dactyl.module.impl.render;

import me.fluffy.dactyl.event.impl.world.Render3DEvent;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.module.impl.client.Colors;
import me.fluffy.dactyl.module.impl.combat.AutoCrystal;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.CombatUtil;
import me.fluffy.dactyl.util.render.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.awt.*;
import java.util.List;
import java.util.concurrent.*;

public class HoleESP extends Module {
    Setting<Integer> holes = new Setting<Integer>("Holes", 50, 1, 50);
    Setting<Boolean> outline = new Setting<Boolean>("Outline", true);
    Setting<Double> lineWidth = new Setting<Double>("LineWidth", 1.5d, 0.5d, 3.0d, vi->outline.getValue());
    Setting<Boolean> box = new Setting<Boolean>("Box", true);
    Setting<Boolean> betterBox = new Setting<Boolean>("BetterBox", false, v->box.getValue());
    Setting<Boolean> reverseBox = new Setting<Boolean>("ReverseBox", false, v->box.getValue()&&betterBox.getValue());
    Setting<Integer> endOpacity = new Setting<Integer>("EndAlpha", 120, 1, 255, v->box.getValue()&&betterBox.getValue());
    Setting<Integer> boxAlpha = new Setting<Integer>("BoxAlpha", 45, 1, 255, vi->box.getValue()&&!betterBox.getValue());
    Setting<Double> renderRange = new Setting<Double>("RenderRange", 8.0d, 1.0d, 20.0d);
    Setting<Boolean> negativeTranslate = new Setting<Boolean>("NegTrans", false);
    Setting<Double> yTranslate = new Setting<Double>("TranslateY", 1.0d, 0.0d, 3.0d);
    Setting<Boolean> negativeExtrude = new Setting<Boolean>("NegExtrude", true);
    Setting<Double> yExtrude = new Setting<Double>("ExtrudeY", 1.0d, 0.0d, 3.0d);
    Setting<Boolean> obsidianHoles = new Setting<Boolean>("ObiHoles", true);
    Setting<Boolean> bedrockHoles = new Setting<Boolean>("BedrockHoles", true);
    Setting<Boolean> obiSync = new Setting<Boolean>("ObiSync", false);
    Setting<Integer> obiRed = new Setting<Integer>("ObiRed", 5, 1, 255, v->obsidianHoles.getValue()&&!obiSync.getValue());
    Setting<Integer> obiGreen = new Setting<Integer>("ObiGreen", 175, 1, 255, v->obsidianHoles.getValue()&&!obiSync.getValue());
    Setting<Integer> obiBlue = new Setting<Integer>("ObiBlue", 255, 1, 255, v->obsidianHoles.getValue()&&!obiSync.getValue());
    Setting<Boolean> bedrockSync = new Setting<Boolean>("BedrockSync", false);
    Setting<Integer> bedrockRed = new Setting<Integer>("B Red", 5, 1, 255, v->bedrockHoles.getValue()&&!bedrockSync.getValue());
    Setting<Integer> bedrockGreen = new Setting<Integer>("B Green", 5, 1, 255, v->bedrockHoles.getValue()&&!bedrockSync.getValue());
    Setting<Integer> bedrockBlue = new Setting<Integer>("B Blue", 5, 1, 255, v->bedrockHoles.getValue()&&!bedrockSync.getValue());
    public HoleESP() {
        super("HoleESP", Category.RENDER);
    }

    private ConcurrentHashMap<BlockPos, Boolean> renderHoles;

    @Override
    public void onClientUpdate() {
        if(mc.player == null || mc.world == null) {
            return;
        }
        this.updateHoles();
    }


    @Override
    public void onRender3D(Render3DEvent event) {
        if (mc.player == null || mc.world == null || this.renderHoles == null) {
            return;
        }
        if (this.renderHoles.isEmpty()) {
            return;
        }
        this.renderHoles.forEach((blockPos, isBedrock) -> {
            BlockPos pos = blockPos;
            double transY = negativeTranslate.getValue() ? (-yTranslate.getValue()) : yTranslate.getValue();
            double extY = negativeExtrude.getValue() ? (-yExtrude.getValue()) : yExtrude.getValue();
            if(box.getValue() && betterBox.getValue()) {
                if (isBedrock) {
                    RenderUtil.drawOffsetBoxOpacity(pos, transY, extY, bedrockSync.getValue() ? Colors.INSTANCE.convertHex(Colors.INSTANCE.getColor(1, false)) : new Color(bedrockRed.getValue(), bedrockGreen.getValue(), bedrockBlue.getValue(), 255), lineWidth.getValue().floatValue(), outline.getValue(), box.getValue(), endOpacity.getValue(), 0, reverseBox.getValue());
                } else {
                    RenderUtil.drawOffsetBoxOpacity(pos, transY, extY, obiSync.getValue() ? Colors.INSTANCE.convertHex(Colors.INSTANCE.getColor(1, false)) : new Color(obiRed.getValue(), obiGreen.getValue(), obiBlue.getValue(), 255), lineWidth.getValue().floatValue(), outline.getValue(), box.getValue(), endOpacity.getValue(), 0, reverseBox.getValue());
                }
            } else {
                if (isBedrock) {
                    RenderUtil.drawOffsetBox(pos, transY, extY, bedrockSync.getValue() ? Colors.INSTANCE.convertHex(Colors.INSTANCE.getColor(1, false)) : new Color(bedrockRed.getValue(), bedrockGreen.getValue(), bedrockBlue.getValue(), 255), lineWidth.getValue().floatValue(), outline.getValue(), box.getValue(), boxAlpha.getValue());
                } else {
                    RenderUtil.drawOffsetBox(pos, transY, extY, obiSync.getValue() ? Colors.INSTANCE.convertHex(Colors.INSTANCE.getColor(1, false)) : new Color(obiRed.getValue(), obiGreen.getValue(), obiBlue.getValue(), 255), lineWidth.getValue().floatValue(), outline.getValue(), box.getValue(), boxAlpha.getValue());
                }
            }
        });
    }

    private void updateHoles() {
        if (this.renderHoles == null) {
            this.renderHoles = new ConcurrentHashMap<>();
        } else {
            this.renderHoles.clear();
        }
        int range = (int)Math.ceil(((Double)this.renderRange.getValue()).doubleValue());
        List<BlockPos> blockPosList = CombatUtil.getSphere(CombatUtil.flooredPos(), range, range, false, true, 0);
        for (BlockPos pos : blockPosList) {
            if (!mc.world.getBlockState(pos).getBlock().equals(Blocks.AIR)) {
                continue;
            }
            if (!mc.world.getBlockState(pos.add(0, 1, 0)).getBlock().equals(Blocks.AIR)) {
                continue;
            }
            if (!mc.world.getBlockState(pos.add(0, 2, 0)).getBlock().equals(Blocks.AIR)) {
                continue;
            }
            boolean isSafe = true;
            boolean isBedrock = true;
            for (BlockPos offset : CombatUtil.surroundOffset) {
                Block block = mc.world.getBlockState(pos.add((Vec3i)offset)).getBlock();
                if (block != Blocks.BEDROCK)
                    isBedrock = false;
                if (block != Blocks.BEDROCK && block != Blocks.OBSIDIAN && block != Blocks.ENDER_CHEST && block != Blocks.ANVIL) {
                    isSafe = false;
                    break;
                }
            }
            if (isSafe) {
                if(renderHoles.size() <= holes.getValue()) {
                    this.renderHoles.put(pos, Boolean.valueOf(isBedrock));
                }
            }
        }
    }
}
