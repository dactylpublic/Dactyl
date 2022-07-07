package me.chloe.dactyl.module.impl.misc;

import me.chloe.dactyl.event.impl.network.PacketEvent;
import me.chloe.dactyl.injection.inj.access.IRenderManager;
import me.chloe.dactyl.module.impl.client.Colors;
import me.chloe.dactyl.setting.Setting;
import me.chloe.dactyl.util.TimeUtil;
import me.chloe.dactyl.event.impl.world.Render3DEvent;
import me.chloe.dactyl.module.Module;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector3d;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

public class Blink extends Module {
    public Setting<Boolean> breadCrumbs = new Setting<Boolean>("BreadCrumbs", true);
    public Blink() {
        super("Blink", Category.PLAYER, "Holds packets so yo ass can do hoodini shit");
    }

    private ArrayList<Packet> packets = new ArrayList<>();

    private ArrayList<Vector3d> locations = new ArrayList<>();

    private final TimeUtil timer = new TimeUtil();

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if (mc.player == null) {
            return;
        }
        if (event.getType() == PacketEvent.PacketType.OUTGOING) {
            if ((mc.player.posX != mc.player.prevPosX || mc.player.posY != mc.player.prevPosY || mc.player.posZ != mc.player.prevPosZ)) {
                this.locations.add(new Vector3d(mc.player.posX, mc.player.posY, mc.player.posZ));
            }
            this.packets.add(event.getPacket());
            event.setCanceled(true);
        }
    }

    @Override
    public void onClientUpdate() {
        this.setModuleInfo(String.valueOf(timer.getPassedTime()));
    }

    @Override
    public void onLogout() {
        this.toggle();
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (mc.player == null) {
            return;
        }
        if (!this.locations.isEmpty() && breadCrumbs.getValue()) {
            GL11.glPushMatrix();
            GL11.glLineWidth(3.0F);
            GL11.glDisable(2929);
            GL11.glDisable(3553);
            GL11.glColor3d(1.0D, 1.0D, 1.0D);
            GL11.glEnable(2848);
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);
            GL11.glBegin(3);
            Color currentColor = Colors.INSTANCE.convertHex(Colors.INSTANCE.getColor(1, false));
            GL11.glColor4f(currentColor.getRed()/255f, currentColor.getGreen()/255f, currentColor.getBlue()/255f, 1.0f);
            for (Vector3d vector : this.locations) {
                GL11.glVertex3d(vector.x - ((IRenderManager) mc.getRenderManager()).getRenderPosX(), vector.y - ((IRenderManager) mc.getRenderManager()).getRenderPosY(), vector.z - ((IRenderManager) mc.getRenderManager()).getRenderPosZ());
            }
            GL11.glEnd();
            GL11.glDisable(3042);
            GL11.glDisable(2848);
            GL11.glEnable(3553);
            GL11.glEnable(2929);
            GL11.glPopMatrix();
        }
    }

    @Override
    public void onEnable() {
        if (mc.world == null || mc.player == null) {
            return;
        }
        if(mc.isSingleplayer()) {
            this.toggle();
            return;
        }
        Vector3d startVector = new Vector3d(mc.player.posX, mc.player.posY, mc.player.posZ);
        EntityOtherPlayerMP entityOtherPlayerMP = new EntityOtherPlayerMP(mc.world, mc.player.getGameProfile());
        entityOtherPlayerMP.inventory = mc.player.inventory;
        entityOtherPlayerMP.inventoryContainer = mc.player.inventoryContainer;
        entityOtherPlayerMP.setPositionAndRotation(mc.player.posX, (mc.player.getEntityBoundingBox()).minY, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch);
        entityOtherPlayerMP.rotationYawHead = mc.player.rotationYawHead;
        entityOtherPlayerMP.setSneaking(mc.player.isSneaking());
        mc.world.addEntityToWorld(-666420, entityOtherPlayerMP);
        this.packets.clear();
    }

    public void onDisable() {
        if (mc.world == null || packets == null || mc.player == null || mc.player.connection == null || locations == null) {
            return;
        }
        if(mc.isSingleplayer()) {
            return;
        }
        mc.world.removeEntityFromWorld(-666420);
        Iterator<Packet> iterator = packets.iterator();
        while (iterator.hasNext()) {
            Packet currentPacket = iterator.next();
            mc.player.connection.sendPacket(currentPacket);
            iterator.remove();
        }
        this.packets.clear();
        this.locations.clear();
    }

    @Override
    public void onToggle() {
        timer.reset();
    }
}
