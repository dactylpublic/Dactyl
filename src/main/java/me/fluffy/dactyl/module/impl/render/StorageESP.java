package me.fluffy.dactyl.module.impl.render;

import me.fluffy.dactyl.event.impl.world.Render3DEvent;
import me.fluffy.dactyl.injection.inj.access.IRenderManager;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.render.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.*;
import net.minecraft.util.math.AxisAlignedBB;

public class StorageESP extends Module {
    public Setting<Boolean> shulkerViewer = new Setting<Boolean>("ShulkerViewer", true);
    public Setting<Boolean> chests = new Setting<Boolean>("Chests", true);
    public Setting<Boolean> enderchests = new Setting<Boolean>("EChests", true);
    public Setting<Boolean> shulkers = new Setting<Boolean>("Shulkers", true);
    public Setting<Boolean> hoppers = new Setting<Boolean>("Hoppers", false);
    public Setting<Double> lineWidth = new Setting<Double>("Width", 1.0d, 0.1d, 3.0d);

    public static StorageESP INSTANCE;
    public StorageESP() {
        super("StorageESP", Category.RENDER);
        INSTANCE = this;
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (mc.player == null || mc.world == null || mc.world.loadedTileEntityList == null) {
            return;
        }

        GlStateManager.pushMatrix();
        for (TileEntity tileEntity : mc.world.loadedTileEntityList) {
            if(tileEntity == null) {
                return;
            }
            if(canDraw(tileEntity)) {
                double x = tileEntity.getPos().getX() - ((IRenderManager)mc.getRenderManager()).getRenderPosX();
                double y = tileEntity.getPos().getY() - ((IRenderManager)mc.getRenderManager()).getRenderPosY();
                double z = tileEntity.getPos().getZ() - ((IRenderManager)mc.getRenderManager()).getRenderPosZ();
                float[] color = getColor(tileEntity);
                AxisAlignedBB box = new AxisAlignedBB(x, y, z, x + 1D, y + 1D, z + 1D);

                if (tileEntity instanceof TileEntityChest)
                {
                    TileEntityChest chest = TileEntityChest.class.cast(tileEntity);

                    if (chest.adjacentChestZPos != null)
                    {
                        box = new AxisAlignedBB(x + 0.0625, y, z + 0.0625, x + 0.9375, y + 0.875, z + 1.9375);
                    }
                    else if (chest.adjacentChestXPos != null)
                    {
                        box = new AxisAlignedBB(x + 0.0625, y, z + 0.0625, x + 1.9375, y + 0.875, z + 0.9375);
                    }
                    else if (chest.adjacentChestZPos == null && chest.adjacentChestXPos == null && chest.adjacentChestZNeg == null && chest.adjacentChestXNeg == null)
                    {
                        box = new AxisAlignedBB(x + 0.0625, y, z + 0.0625, x + 0.9375, y + 0.875, z + 0.9375);
                    }
                    else
                    {
                        continue;
                    }
                }
                else if (tileEntity instanceof TileEntityEnderChest)
                {
                    box = new AxisAlignedBB(x + 0.0625, y, z + 0.0625, x + 0.9375, y + 0.875, z + 0.9375);
                }

                GlStateManager.color(color[0], color[1], color[2], 0.45F);
                RenderUtil.renderOne(lineWidth.getValue().floatValue());
                GlStateManager.color(color[0], color[1], color[2], 0.45F);
                RenderUtil.drawBBBox(box);
                RenderUtil.renderTwo();
                GlStateManager.color(color[0], color[1], color[2], 0.45F);
                RenderUtil.drawBBBox(box);
                RenderUtil.renderThree();
                GlStateManager.color(color[0], color[1], color[2], 0.45F);
                RenderUtil.drawBBBox(box);
                RenderUtil.renderFour();
                GlStateManager.color(color[0], color[1], color[2], 0.45F);
                RenderUtil.drawBBBox(box);
                RenderUtil.renderFive();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
        GlStateManager.popMatrix();
    }

    private float[] getColor(TileEntity tileEntity)
    {
        if (tileEntity instanceof TileEntityChest)
        {
            Block block = tileEntity.getBlockType();

            if (block == Blocks.CHEST)
            {
                return new float[] {0.8F, 0.7F, 0.22F};
            }
            else if (block == Blocks.TRAPPED_CHEST)
            {
                return new float[] {0.8F, 0.22F, 0.22F};
            }
        }

        if (tileEntity instanceof TileEntityEnderChest)
        {
            return new float[] {0.68F, 0, 1F};
        }

        if(tileEntity instanceof TileEntityShulkerBox) {
            return new float[] {1F, 0.36F, 1F};
        }

        if(tileEntity instanceof TileEntityHopper) {
            return new float[] {0.58F, 0.58F, 0.58F};
        }

        return new float[] {1, 1, 1};
    }




    private boolean canDraw(TileEntity entity) {
        if(entity instanceof TileEntityChest && chests.getValue()) {
            return true;
        }
        if(entity instanceof TileEntityEnderChest && enderchests.getValue()) {
            return true;
        }
        if(entity instanceof TileEntityShulkerBox && shulkers.getValue()) {
            return true;
        }
        if(entity instanceof TileEntityHopper && hoppers.getValue()) {
            return true;
        }
        return false;
    }
}
