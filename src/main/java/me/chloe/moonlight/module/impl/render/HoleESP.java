package me.chloe.moonlight.module.impl.render;

import me.chloe.moonlight.util.CombatUtil;
import me.chloe.moonlight.util.render.RenderUtil;
import me.chloe.moonlight.event.impl.world.Render3DEvent;
import me.chloe.moonlight.module.Module;
import me.chloe.moonlight.module.impl.client.Colors;
import me.chloe.moonlight.setting.Setting;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

import java.awt.*;
import java.util.List;
import java.util.concurrent.*;

public class HoleESP extends Module {
    Setting<Integer> holes = new Setting<>("Holes", 50, 1, 50);
    Setting<Boolean> outline = new Setting<>("Outline", true);
    Setting<Double> lineWidth = new Setting<>("LineWidth", 1.5d, 0.5d, 3.0d, vi -> outline.getValue());
    Setting<Boolean> box = new Setting<>("Box", true);
    Setting<Boolean> betterBox = new Setting<>("BetterBox", false, v -> box.getValue());
    Setting<Boolean> reverseBox = new Setting<>("ReverseBox", false, v -> box.getValue() && betterBox.getValue());
    Setting<Integer> endOpacity = new Setting<>("EndAlpha", 120, 1, 255, v -> box.getValue() && betterBox.getValue());
    Setting<Integer> boxAlpha = new Setting<>("BoxAlpha", 45, 1, 255, vi -> box.getValue() && !betterBox.getValue());
    Setting<Double> renderRange = new Setting<>("RenderRange", 8.0d, 1.0d, 20.0d);
    Setting<Boolean> negativeTranslate = new Setting<>("NegTrans", false);
    Setting<Double> yTranslate = new Setting<>("TranslateY", 1.0d, 0.0d, 3.0d);
    Setting<Boolean> negativeExtrude = new Setting<>("NegExtrude", true);
    Setting<Double> yExtrude = new Setting<>("ExtrudeY", 1.0d, 0.0d, 3.0d);
    Setting<Boolean> obsidianHoles = new Setting<>("ObiHoles", true);
    Setting<Boolean> bedrockHoles = new Setting<>("BedrockHoles", true);
    Setting<Boolean> obiSync = new Setting<>("ObiSync", false);
    Setting<Integer> obiRed = new Setting<>("ObiRed", 5, 1, 255, v -> obsidianHoles.getValue() && !obiSync.getValue());
    Setting<Integer> obiGreen = new Setting<>("ObiGreen", 175, 1, 255, v -> obsidianHoles.getValue() && !obiSync.getValue());
    Setting<Integer> obiBlue = new Setting<>("ObiBlue", 255, 1, 255, v -> obsidianHoles.getValue() && !obiSync.getValue());
    Setting<Boolean> bedrockSync = new Setting<>("BedrockSync", false);
    Setting<Integer> bedrockRed = new Setting<>("B Red", 5, 1, 255, v -> bedrockHoles.getValue() && !bedrockSync.getValue());
    Setting<Integer> bedrockGreen = new Setting<>("B Green", 5, 1, 255, v -> bedrockHoles.getValue() && !bedrockSync.getValue());
    Setting<Integer> bedrockBlue = new Setting<>("B Blue", 5, 1, 255, v -> bedrockHoles.getValue() && !bedrockSync.getValue());
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
        if (mc.player == null || mc.world == null || this.renderHoles == null)
            return;

        if (this.renderHoles.isEmpty())
            return;

        this.renderHoles.forEach((blockPos, isBedrock) -> {
            double transY = negativeTranslate.getValue() ? (-yTranslate.getValue()) : yTranslate.getValue();
            double extY = negativeExtrude.getValue() ? (-yExtrude.getValue()) : yExtrude.getValue();
            if(box.getValue() && betterBox.getValue()) {
                if (isBedrock) {
                    RenderUtil.drawOffsetBoxOpacity(blockPos, transY, extY, bedrockSync.getValue() ? Colors.INSTANCE.convertHex(Colors.INSTANCE.getColor(1, false)) : new Color(bedrockRed.getValue(), bedrockGreen.getValue(), bedrockBlue.getValue(), 255), lineWidth.getValue().floatValue(), outline.getValue(), box.getValue(), endOpacity.getValue(), 0, reverseBox.getValue());
                } else {
                    RenderUtil.drawOffsetBoxOpacity(blockPos, transY, extY, obiSync.getValue() ? Colors.INSTANCE.convertHex(Colors.INSTANCE.getColor(1, false)) : new Color(obiRed.getValue(), obiGreen.getValue(), obiBlue.getValue(), 255), lineWidth.getValue().floatValue(), outline.getValue(), box.getValue(), endOpacity.getValue(), 0, reverseBox.getValue());
                }
            } else {
                if (isBedrock) {
                    RenderUtil.drawOffsetBox(blockPos, transY, extY, bedrockSync.getValue() ? Colors.INSTANCE.convertHex(Colors.INSTANCE.getColor(1, false)) : new Color(bedrockRed.getValue(), bedrockGreen.getValue(), bedrockBlue.getValue(), 255), lineWidth.getValue().floatValue(), outline.getValue(), box.getValue(), boxAlpha.getValue());
                } else {
                    RenderUtil.drawOffsetBox(blockPos, transY, extY, obiSync.getValue() ? Colors.INSTANCE.convertHex(Colors.INSTANCE.getColor(1, false)) : new Color(obiRed.getValue(), obiGreen.getValue(), obiBlue.getValue(), 255), lineWidth.getValue().floatValue(), outline.getValue(), box.getValue(), boxAlpha.getValue());
                }
            }
        });
    }

    private void updateHoles() {
        if (this.renderHoles == null)
            this.renderHoles = new ConcurrentHashMap<>();
        else
            this.renderHoles.clear();

        int range = (int)Math.ceil(this.renderRange.getValue());
        List<BlockPos> blockPosList = CombatUtil.getSphere(CombatUtil.flooredPos(), range, range, false, true, 0);
        for (BlockPos pos : blockPosList) {
            if (!mc.world.getBlockState(pos).getBlock().equals(Blocks.AIR))
                continue;

            if (!mc.world.getBlockState(pos.add(0, 1, 0)).getBlock().equals(Blocks.AIR))
                continue;

            if (!mc.world.getBlockState(pos.add(0, 2, 0)).getBlock().equals(Blocks.AIR))
                continue;

            boolean isSafe = true;
            boolean isBedrock = true;

            for (BlockPos offset : CombatUtil.surroundOffset) {
                Block block = mc.world.getBlockState(pos.add(offset)).getBlock();

                if (block != Blocks.BEDROCK && offset.getY() != -1)
                    isBedrock = false;

                if (block != Blocks.BEDROCK && block != Blocks.OBSIDIAN && block != Blocks.ENDER_CHEST && block != Blocks.ANVIL) {
                    isSafe = false;
                    break;
                }
            }

            if (isSafe) {
                if(renderHoles.size() <= holes.getValue()) {
                    this.renderHoles.put(pos, isBedrock);
                }
            }
        }
    }
}
