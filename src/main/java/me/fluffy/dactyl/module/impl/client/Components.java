package me.fluffy.dactyl.module.impl.client;

import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.module.impl.combat.AutoCrystal;
import me.fluffy.dactyl.module.impl.combat.Killaura;
import me.fluffy.dactyl.module.impl.render.Nametags;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.EntityUtil;
import me.fluffy.dactyl.util.TimeUtil;
import me.fluffy.dactyl.util.render.RenderUtil;
import me.fluffy.dactyl.util.render.font.CFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.text.TextFormatting;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

public class Components extends Module {
    public Setting<AddonPage> page = new Setting<AddonPage>("Page", AddonPage.TARGETHUD);

    // targethud
    public Setting<Boolean> targetHud = new Setting<Boolean>("TargetHUD", false, v->page.getValue()==AddonPage.TARGETHUD);
    public Setting<Boolean> targetArmor = new Setting<Boolean>("TArmor", true, v->targetHud.getValue() && page.getValue()==AddonPage.TARGETHUD);
    public Setting<Boolean> targetPing = new Setting<Boolean>("TPing", true, v->targetHud.getValue() && page.getValue()==AddonPage.TARGETHUD);
    public Setting<Boolean> targetHealth = new Setting<Boolean>("THealth", true, v->targetHud.getValue() && page.getValue()==AddonPage.TARGETHUD);
    public Setting<Boolean> targetPopped = new Setting<Boolean>("TPopped", true, v->targetHud.getValue() && page.getValue()==AddonPage.TARGETHUD);
    public Setting<THudPrio> targetHudPrio = new Setting<THudPrio>("TPrio", THudPrio.CAURA, v->targetHud.getValue() && page.getValue()==AddonPage.TARGETHUD);
    public Setting<Boolean> targetBackground = new Setting<Boolean>("TBorder", true, v->targetHud.getValue() && page.getValue()==AddonPage.TARGETHUD);
    public Setting<String> targetHudX = new Setting<String>("TargetX", "1", v->targetHud.getValue() && page.getValue()==AddonPage.TARGETHUD);
    public Setting<String> targetHudY = new Setting<String>("TargetY", "30", v->targetHud.getValue() && page.getValue()==AddonPage.TARGETHUD);

    public Components() {
        super("Addons", Category.CLIENT, true);
    }

    private final CFontRenderer font = new CFontRenderer(new Font("Verdana", Font.PLAIN, 18), true, true);

    @Override
    public void onScreen() {
        if (mc.player == null) {
            return;
        }
        doTargetHud();
    }

    private void doTargetHud() {
        if(mc.world == null || !targetHud.getValue() || getClosestTarget() == null) {
            return;
        }
        int setX = convertString(targetHudX.getValue());
        int setY = convertString(targetHudY.getValue());
        int fontHeight = font.getHeight();
        int c = Colors.INSTANCE.getColor(1, false);
        Entity targetEntity = getClosestTarget();
        String entName = targetEntity.getName();
        font.drawStringWithShadow(entName, setX, setY, 0xffffffff);
        setY+=font.getHeight()+2+5;
        int armorY = 0;
        if(targetArmor.getValue()) {
            armorY+=renderArmor(setX, setY, (EntityPlayer) targetEntity);
            if(((EntityPlayer) targetEntity).inventory.armorInventory != null && !((EntityPlayer) targetEntity).inventory.armorInventory.isEmpty()) {
                setX += 20;
            }
        }
        //if(targetModel.getValue()) {
            //drawEntityOnScreen(setX+35, setY+60, 30, 0, 0, (EntityLivingBase) targetEntity);
        //    setX+=35;
        //}
        if(targetHealth.getValue()) {
            float health = ((EntityPlayer)targetEntity).getHealth()+((EntityPlayer)targetEntity).getAbsorptionAmount();
            TextFormatting textColor;
            if(health <= 0) {
                health = 1;
            }
            if (health > 18.0F) {
                textColor = TextFormatting.GREEN;
            } else if (health > 16.0F) {
                textColor = TextFormatting.DARK_GREEN;
            } else if (health > 12.0F) {
                textColor = TextFormatting.YELLOW;
            } else if (health > 8.0F) {
                textColor = TextFormatting.GOLD;
            } else if (health > 5.0F) {
                textColor = TextFormatting.RED;
            } else {
                textColor = TextFormatting.DARK_RED;
            }
            int pHealth = (int)Math.ceil(health);
            if(pHealth <= 0) {
                pHealth = 1;
            }
            font.drawStringWithShadow("HP: "+ textColor + String.valueOf(pHealth), setX+20, setY, 0xffffffff);
            setY+=10;
        }

        if(targetPopped.getValue()) {
            if(Nametags.getTotemPops(targetEntity.getName()) > 0) {
                font.drawStringWithShadow("TP: "+ TextFormatting.RED + String.valueOf(Nametags.getTotemPops(targetEntity.getName())), setX+20, setY, 0xffffffff);
                setY+=10;
            }
        }
        if(targetPing.getValue()) {
            font.drawStringWithShadow("Ping: "+ TextFormatting.GREEN + EntityUtil.getPing((EntityPlayer) targetEntity), setX+20, setY, 0xffffffff);
            setY+=10;
        }
        if(targetBackground.getValue()) {
            double maxLength = setX + 63.5 + (EntityUtil.getPing((EntityPlayer) targetEntity) > 100 ? Dactyl.fontUtil.getStringWidth("9") : 0);
            RenderUtil.drawBetterColoredRect(convertString(targetHudX.getValue()), convertString(targetHudY.getValue()), maxLength, setY + 52.5, 1.0f, c);
        }
    }

    private int renderArmor(int x, int setY, EntityPlayer entity) {
        ScaledResolution renderer = new ScaledResolution(mc);
        final int width = renderer.getScaledWidth();
        final int height = renderer.getScaledHeight();
        GlStateManager.enableTexture2D();
        int iteration = 0;
        int y = setY;
        ArrayList<ItemStack> stacks = new ArrayList<>();
        stacks.addAll(entity.inventory.armorInventory);
        Collections.reverse(stacks);
        for (final ItemStack is : stacks) {
            ++iteration;
            if (is.isEmpty()) {
                continue;
            }
            GlStateManager.enableDepth();
            mc.getRenderItem().zLevel = 200.0f;
            mc.getRenderItem().renderItemAndEffectIntoGUI(is, x, y);
            mc.getRenderItem().renderItemOverlayIntoGUI(HUD.mc.fontRenderer, is, x, y, "");
            mc.getRenderItem().zLevel = 0.0f;
            GlStateManager.enableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            final String s = (is.getCount() > 1) ? (is.getCount() + "") : "";
            int dmg = 0;
            final int itemDurability = is.getMaxDamage() - is.getItemDamage();
            final float green = (is.getMaxDamage() - (float)is.getItemDamage()) / is.getMaxDamage();
            final float red = 1.0f - green;
            dmg = 100 - (int)(red * 100.0f);
            Dactyl.fontUtil.drawStringWithShadow(dmg + "", (int)((x + 8 - Dactyl.fontUtil.getStringWidth(dmg + "") / 2)+16), y+3, RenderUtil.toRGBA((int)(red * 255.0f), (int)(green * 255.0f), 0));
            y+=16;
        }
        GlStateManager.enableDepth();
        GlStateManager.disableLighting();
        return y;
    }

    private Entity getClosestTarget() {
        if(mc.world == null || mc.player == null) {
            return null;
        }
        Entity t = null;
        switch(targetHudPrio.getValue()) {
            case CAURA:
                t = mc.world.getPlayerEntityByName(AutoCrystal.INSTANCE.getModuleInfo());
                break;
            case KAURA:
                t = mc.world.getPlayerEntityByName(Killaura.INSTANCE.getModuleInfo());
                break;
            case DIST:
                t = getClosest();
                break;
        }
        if(t == null) {
            if(targetHudPrio.getValue() == THudPrio.CAURA) {
                t = mc.world.getPlayerEntityByName(Killaura.INSTANCE.getModuleInfo());
            } else if(targetHudPrio.getValue() == THudPrio.KAURA) {
                t = getClosest();
            } else {
                t = mc.world.getPlayerEntityByName(AutoCrystal.INSTANCE.getModuleInfo());
            }
        }
        if(t == null) {
            if(targetHudPrio.getValue() == THudPrio.CAURA) {
                t = getClosest();
            } else if(targetHudPrio.getValue() == THudPrio.KAURA) {
                t = mc.world.getPlayerEntityByName(AutoCrystal.INSTANCE.getModuleInfo());
            } else {
                t = mc.world.getPlayerEntityByName(Killaura.INSTANCE.getModuleInfo());
            }
        }
        return t;
    }

    private Entity getClosest() {
        Entity ent = null;
        double maxDist = 8;
        for (Entity e : mc.world.loadedEntityList) {
            if (e != null) {
                if(e != mc.player) {
                    if (e instanceof EntityPlayer) {
                        if(!Dactyl.friendManager.isFriend(e.getName())) {
                            float currentDist = mc.player.getDistance(e);
                            if (currentDist <= maxDist) {
                                maxDist = currentDist;
                                ent = e;
                            }
                        }
                    }
                }
            }
        }
        return ent;
    }

    public void drawEntityOnScreen(int posX, int posY, int scale, float mouseX, float mouseY, EntityLivingBase ent) {
        if(Minecraft.getMinecraft().getRenderManager() == null) {
            return;
        }
        GlStateManager.pushMatrix();
        GlStateManager.translate((float)posX, (float)posY, 50.0F);
        GlStateManager.scale((float)(-scale), (float)scale, (float)scale);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        float f = ent.renderYawOffset;
        float f1 = ent.rotationYaw;
        float f2 = ent.rotationPitch;
        float f3 = ent.prevRotationYawHead;
        float f4 = ent.rotationYawHead;
        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-((float)Math.atan((double)(mouseY / 40.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
        ent.renderYawOffset = (float)Math.atan((double)(mouseX / 40.0F)) * 20.0F;
        ent.rotationYaw = (float)Math.atan((double)(mouseX / 40.0F)) * 40.0F;
        ent.rotationPitch = -((float)Math.atan((double)(mouseY / 40.0F))) * 20.0F;
        ent.rotationYawHead = ent.rotationYaw;
        ent.prevRotationYawHead = ent.rotationYaw;
        GlStateManager.translate(0.0F, 0.0F, 0.0F);
        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        rendermanager.setPlayerViewY(180.0F);
        rendermanager.setRenderShadow(false);
        rendermanager.renderEntity(ent, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
        rendermanager.setRenderShadow(true);
        ent.renderYawOffset = f;
        ent.rotationYaw = f1;
        ent.rotationPitch = f2;
        ent.prevRotationYawHead = f3;
        ent.rotationYawHead = f4;
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    private int convertString(String s) {
        int i = 0;
        try{
            i = Integer.parseInt(s);
        } catch(Exception e) {
            i = 0;
        }
        return i;
    }

    public enum AddonPage {
        TARGETHUD("THud"),
        PVPINFO("PvP");

        private final String name;
        private AddonPage(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }


    }


    private enum THudPrio {
        KAURA,
        CAURA,
        DIST
    }
}
