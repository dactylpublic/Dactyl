package me.chloe.dactyl.module.impl.render;

import me.chloe.dactyl.injection.inj.access.IRenderManager;
import me.chloe.dactyl.util.MathUtil;
import me.chloe.dactyl.util.render.RenderUtil;
import me.chloe.dactyl.event.impl.world.Render3DEvent;
import me.chloe.dactyl.module.Module;
import me.chloe.dactyl.module.impl.client.Colors;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public class Trajectories extends Module {
    public Trajectories() {
        super("Trajectories", Category.RENDER);
    }

    private MathUtil.Result result = null;

    @Override
    public void onRender3D(Render3DEvent event) {
        if(mc.player == null || mc.world == null) {
            return;
        }
        MathUtil.Projectiles projectile = MathUtil.getProjectile(mc.player);
        if(projectile != null) result = MathUtil.calculate(mc.player, projectile);
        else result = null;
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
