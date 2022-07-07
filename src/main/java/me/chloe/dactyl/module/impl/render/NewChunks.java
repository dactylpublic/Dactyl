package me.chloe.dactyl.module.impl.render;

import me.chloe.dactyl.event.impl.network.PacketEvent;
import me.chloe.dactyl.util.render.RenderUtil;
import me.chloe.dactyl.event.impl.world.Render3DEvent;
import me.chloe.dactyl.module.Module;
import me.chloe.dactyl.module.impl.client.Colors;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class NewChunks extends Module {
    public NewChunks() {
        super("NewChunks", Category.RENDER);
    }

    private ICamera frustum = new Frustum();

    private List<Chunk> chunkList = new ArrayList<>();

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if(event.getType() == PacketEvent.PacketType.INCOMING) {
            if(event.getPacket() instanceof SPacketChunkData) {
                SPacketChunkData packetChunkData = (SPacketChunkData)event.getPacket();
                if(!packetChunkData.isFullChunk()) {
                    Chunk chunk = new Chunk(packetChunkData.getChunkX() * 16, packetChunkData.getChunkZ() * 16);
                    if (!this.chunkList.contains(chunk)) {
                        this.chunkList.add(chunk);
                    }
                }
            }
        }
    }

    @Override
    public void onToggle() {
        chunkList.clear();
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if(mc.player == null || mc.world == null || mc.getRenderManager() == null) {
            return;
        }

        for (Chunk chunkData : new ArrayList<Chunk>(this.chunkList)) {
            if (chunkData != null) {
                this.frustum.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);

                AxisAlignedBB bb = new AxisAlignedBB(chunkData.x, 0, chunkData.z, chunkData.x + 16, 1, chunkData.z + 16);

                if (frustum.isBoundingBoxInFrustum(bb)) {
                    RenderUtil.drawPlane(chunkData.x - mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY,
                            chunkData.z - mc.getRenderManager().viewerPosZ, new AxisAlignedBB(0, 0, 0, 16, 1, 16), 1, Colors.INSTANCE.getColor(1, false));
                }
            }
        }
    }


    public static class Chunk {
        private int x;
        private int z;

        public Chunk(int x, int z)
        {
            this.x = x;
            this.z = z;
        }

        public int getX()
        {
            return x;
        }

        public void setX(int x)
        {
            this.x = x;
        }

        public int getZ()
        {
            return z;
        }

        public void setZ(int z)
        {
            this.z = z;
        }
    }
}
