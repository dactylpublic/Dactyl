package me.fluffy.dactyl.module.impl.render;

import me.fluffy.dactyl.event.impl.world.Render3DEvent;
import me.fluffy.dactyl.injection.inj.access.IRenderManager;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.module.impl.client.Colors;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.MathUtil;
import me.fluffy.dactyl.util.render.RenderUtil;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class Trajectories extends Module {
    public Trajectories() {
        super("Trajectories", Category.RENDER);
    }

    private MathUtil.Result result = null;

    @Override
    public void onClientUpdate() {
        if(mc.player == null || mc.world == null) {
            return;
        }
        MathUtil.Projectiles projectile = MathUtil.getProjectile(mc.player);
        if(projectile != null) result = MathUtil.calculate(mc.player, projectile);
        else result = null;
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if(result != null) {
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_LIGHTING);

            Vec3d prevPoint = null;
            for(Vec3d point: result.getPoints()) {
                if(prevPoint != null) RenderUtil.drawLine(point.x - ((IRenderManager) mc.getRenderManager()).getRenderPosX(), point.y - ((IRenderManager) mc.getRenderManager()).getRenderPosY(), point.z - ((IRenderManager) mc.getRenderManager()).getRenderPosZ(),prevPoint.x - ((IRenderManager) mc.getRenderManager()).getRenderPosX(), prevPoint.y - ((IRenderManager) mc.getRenderManager()).getRenderPosY(), prevPoint.z - ((IRenderManager) mc.getRenderManager()).getRenderPosZ(), 2, Colors.INSTANCE.convertHex(Colors.INSTANCE.getColor(1, false)));
                prevPoint = point;
            }
        }
    }

}
