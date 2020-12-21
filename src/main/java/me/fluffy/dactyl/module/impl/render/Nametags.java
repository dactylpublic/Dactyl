package me.fluffy.dactyl.module.impl.render;

import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.event.impl.world.Render3DEvent;
import me.fluffy.dactyl.injection.inj.access.IRenderManager;
import me.fluffy.dactyl.managers.TotemManager;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.module.impl.client.Colors;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.CombatUtil;
import me.fluffy.dactyl.util.EntityUtil;
import me.fluffy.dactyl.util.MathUtil;
import me.fluffy.dactyl.util.render.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;

/*
* @author Comu101
*
* Edited, better looking nametags by fluffy.
 */

public class Nametags extends Module {
    Setting<Boolean> armor = new Setting<Boolean>("Armor", true);
    Setting<Boolean> health = new Setting<Boolean>("Health", true);
    Setting<Boolean> ping = new Setting<Boolean>("Ping", true);
    Setting<Boolean> gamemode = new Setting<Boolean>("Gamemode", true);
    Setting<Boolean> invisibles = new Setting<Boolean>("Invisibles", true);
    Setting<Boolean> durability = new Setting<Boolean>("Durability", true);
    Setting<Boolean> itemName = new Setting<Boolean>("ItemName", true);
    Setting<Boolean> totemPops = new Setting<Boolean>("TotemPops", true);
    Setting<Integer> scaling = new Setting<Integer>("Scaling", 3, 1, 5);
    Setting<Boolean> border = new Setting<Boolean>("Border", true);
    Setting<Double> borderWidth = new Setting<Double>("BorderWidth", 1.0d, 0.1d, 3.0d, v->border.getValue());

    public static Nametags INSTANCE;
    public Nametags() {
        super("NameTags", Category.RENDER);
        INSTANCE = this;
    }

    private ICamera camera = new Frustum();

    @Override
    public void onRender3D(Render3DEvent event) {
        for (EntityPlayer entity : mc.world.playerEntities) {
            double x = interpolate(entity.lastTickPosX, entity.posX, event.getPartialTicks()) - ((IRenderManager)(mc.getRenderManager())).getRenderPosX();
            double y = interpolate(entity.lastTickPosY, entity.posY, event.getPartialTicks()) - ((IRenderManager)(mc.getRenderManager())).getRenderPosY();
            double z = interpolate(entity.lastTickPosZ, entity.posZ, event.getPartialTicks()) -((IRenderManager)(mc.getRenderManager())).getRenderPosZ();
            if(entity != mc.getRenderViewEntity()) {
                if(!invisibles.getValue() && entity.isInvisible()) {
                    return;
                }
                renderNameTag(entity, x, y, z, event.getPartialTicks());
            }
        }
    }

    private void renderNameTag(EntityPlayer player, double x, double y, double z, float delta) {
        camera.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);

        AxisAlignedBB bb = player.getEntityBoundingBox();

        bb = bb.expand(0.15f, 0.1f, 0.15f);

        if (!camera.isBoundingBoxInFrustum(bb)) {
            return;
        }
        double tempY = y;
        tempY += player.isSneaking() ? 0.5D : 0.7D;
        Entity camera = mc.getRenderViewEntity();
        double originalPositionX = camera.posX;
        double originalPositionY = camera.posY;
        double originalPositionZ = camera.posZ;
        camera.posX = interpolate(camera.prevPosX, camera.posX, delta);
        camera.posY = interpolate(camera.prevPosY, camera.posY, delta);
        camera.posZ = interpolate(camera.prevPosZ, camera.posZ, delta);
        double distance = camera.getDistance(x + (mc.getRenderManager()).viewerPosX, y + (mc.getRenderManager()).viewerPosY, z +
                (mc.getRenderManager()).viewerPosZ);
        int width = (int) (Dactyl.fontUtil.getStringWidth(getDisplayName(player)) / 2);
        double scale = 0.0018D + ((scaling.getValue()*0.001)) * distance;
        if (distance <= 8.0D)
            scale = 0.0245D;
        GlStateManager.pushMatrix();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enablePolygonOffset();
        GlStateManager.doPolygonOffset(1.0F, -1500000.0F);
        GlStateManager.disableLighting();
        GlStateManager.translate((float)x, (float)tempY + 1.4F, (float)z);
        GlStateManager.rotate(-(mc.getRenderManager()).playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((mc.getRenderManager()).playerViewX, (mc.gameSettings.thirdPersonView == 2) ? -1.0F : 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-scale, -scale, scale);
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GL11.glDisable(2929);
        GlStateManager.enableBlend();
        if(!Dactyl.fontUtil.isCustomFont()) {
            drawBorderedRect((-width - 2), -(Dactyl.fontUtil.getFontHeight() + 1), width + 2.0F, 1.5F, borderWidth.getValue().floatValue(), borderWidth.getValue().floatValue(), 1996488704, Colors.INSTANCE.getColor(1, false));
        } else {
            drawBorderedRect((-width - 2), -(Dactyl.fontUtil.getFontHeight() + 2), width + 2.0F, 1.5F, borderWidth.getValue().floatValue(), borderWidth.getValue().floatValue(), 1996488704, Colors.INSTANCE.getColor(1, false));
        }
        GlStateManager.disableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glColor4f(1.0f, 10.0f, 1.0f, 1.0f);
        if(!Dactyl.fontUtil.isCustomFont()) {
            Dactyl.fontUtil.drawStringWithShadow(getDisplayName(player), -width, (int) -(Dactyl.fontUtil.getFontHeight() - 1), getDisplayColour(player));
        } else {
            Dactyl.fontUtil.drawStringWithShadow(getDisplayName(player), -width, (int) -(Dactyl.fontUtil.getFontHeight() + 2), getDisplayColour(player));
        }
        GlStateManager.glNormal3f(0.0F, 0.0F, 0.0F);
        if (armor.getValue()) {
            GlStateManager.pushMatrix();
            int xOffset = 0;
            if((player.getHeldItemMainhand().getItem() != null && player.getHeldItemOffhand().getItem() == null) || (player.getHeldItemMainhand().getItem() == null && player.getHeldItemOffhand().getItem() != null)) {
                xOffset = -4;
            } else if(player.getHeldItemMainhand().getItem() != null && player.getHeldItemOffhand().getItem() != null) {
                xOffset = -8;
            }
            int index;
            for (index = 3; index >= 0; index--) {
                ItemStack stack = player.inventory.armorInventory.get(index);
                if (stack != null && stack.getItem() != Items.AIR)
                    xOffset -= 8;
            }
            ArrayList<ItemStack> armorStacks = new ArrayList<>();
            if(player.inventory.armorInventory != null) {
                for(ItemStack itemStack : player.inventory.armorInventory) {
                    if(itemStack != null && !itemStack.getItem().equals(Items.AIR)) {
                        armorStacks.add(itemStack);
                    }
                }
            }
            ArrayList<ItemStack> stacks = new ArrayList<>();
            stacks.addAll(player.inventory.armorInventory);
            if(player.getHeldItemMainhand() != null) stacks.add(player.getHeldItemMainhand().copy());
            if(player.getHeldItemOffhand() != null) stacks.add(player.getHeldItemOffhand().copy());

            if (player.getHeldItemOffhand() != null) {
                xOffset -= 8;
                ItemStack renderStack = player.getHeldItemOffhand().copy();
                if(!renderStack.getItem().equals(Items.AIR)) {
                    renderItemStack(stacks, renderStack, xOffset, -(getEnchantSpace(stacks)+26)+26+10);
                    if(armorStacks.isEmpty()) {
                        xOffset += 22;
                    } else {
                        xOffset += 16;
                    }
                }
            }
            for (index = armorStacks.size()-1; index >= 0; index--) {
                ItemStack stack = armorStacks.get(index);
                if (stack != null) {
                    ItemStack armourStack = stack.copy();
                    if(!armourStack.getItem().equals(Items.AIR)) {
                        if (armourStack.getItem() instanceof net.minecraft.item.ItemTool || armourStack.getItem() instanceof net.minecraft.item.ItemArmor || armourStack.getItem().equals(Items.ELYTRA)) {
                            renderItemStack(stacks, armourStack, xOffset, -(getEnchantSpace(stacks)+26)+26+10);
                        }
                        if(armorStacks.get(0) == stack) {
                            xOffset += 24;
                        } else {
                            xOffset += 16;
                        }
                    }
                }
            }
            if (player.getHeldItemMainhand() != null) {
                xOffset -= 8;
                ItemStack renderStack = player.getHeldItemMainhand().copy();
                if(!renderStack.getItem().equals(Items.AIR)) {
                    renderItemStack(stacks, renderStack, xOffset, -(getEnchantSpace(stacks)+26)+26+10);
                    if (itemName.getValue() && !renderStack.isEmpty() && renderStack.getItem() != Items.AIR) {
                        String stackName = renderStack.getDisplayName();
                        int stackNameWidth = (int) (Dactyl.fontUtil.getStringWidth(stackName) / 2);
                        GL11.glPushMatrix();
                        GL11.glScalef((float)0.5f, (float)0.5f, (float)0.0f);
                        int enchantY = 2-getEnchantSpace(stacks)-24;
                        if(!(enchantY < -50)) {
                            enchantY = -50;
                        }
                        Dactyl.fontUtil.drawStringWithShadow(stackName, -stackNameWidth, (int) (enchantY - 20), -1);
                        GL11.glScalef((float)1.5f, (float)1.5f, (float)1.0f);
                        GL11.glPopMatrix();
                    }
                    xOffset += 16;
                }
            }
            GlStateManager.popMatrix();
        }
        camera.posX = originalPositionX;
        camera.posY = originalPositionY;
        camera.posZ = originalPositionZ;
        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.disablePolygonOffset();
        GlStateManager.doPolygonOffset(1.0F, 1500000.0F);
        GlStateManager.popMatrix();
    }


    public static int getTotemPops(String user) {
        if(Dactyl.totemManager.getPoppedUsers() != null && Dactyl.totemManager.getPoppedUsers().get(user) != null) {
            return (int)Dactyl.totemManager.getPoppedUsers().get(user);
        }
        return 0;
    }

    public void drawBorderedRect(double x, double y, double x1, double y1, double width, float borderWidth, int internalColor, int borderColor) {
        GL11.glPushMatrix();
        enableGL2D();
        RenderUtil.drawRect(x + width, y + width, x1 - width, y1 - width, internalColor);
        if(border.getValue()) {
            RenderUtil.drawBetterColoredRect(x + borderWidth, y + borderWidth, x1 - borderWidth, y1 - borderWidth, borderWidth, borderColor);
        }
        disableGL2D();
        GL11.glPopMatrix();
    }

    public static void enableGL2D() {
        GL11.glDisable(2929);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glDepthMask(true);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glHint(3155, 4354);
    }

    public static void disableGL2D() {
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
        GL11.glHint(3154, 4352);
        GL11.glHint(3155, 4352);
    }

    public int getEnchantSpace(ArrayList<ItemStack> items) {
        int biggestEncCount = 0;
        for(ItemStack i : items) {
            NBTTagList enchants = i.getEnchantmentTagList();
            if (enchants != null) {
                if(enchants.tagCount() > biggestEncCount) {
                    biggestEncCount = enchants.tagCount();
                }
            }
        }
        return biggestEncCount*8;
    }

    public int getHighestEncY(ArrayList<ItemStack> items) {
        return getEnchantSpace(items);
    }

    public String getGMText(EntityPlayer p) {
        if(p.isCreative()) return "C";
        if(p.isSpectator()) return "I";
        if(!p.isAllowEdit() && !p.isSpectator()) return "A";
        if(!p.isCreative() && !p.isSpectator() && p.isAllowEdit()) return "S";
        return "";
    }

    private void renderItemStack(ArrayList<ItemStack> stacks, ItemStack stack, int x, int y) {
        int enchantY = 2-getEnchantSpace(stacks)-24;
        int armorY = 2-(getEnchantSpace(stacks)/2)-14;

        if(!(armorY < -26)) {
            armorY = -26;
        }
        if(!(enchantY < -50)) {
            enchantY = -50;
        }

        GlStateManager.pushMatrix();
        GlStateManager.depthMask(true);
        GlStateManager.clear(256);
        RenderHelper.enableStandardItemLighting();
        (mc.getRenderItem()).zLevel = -150.0F;
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableAlpha();
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableAlpha();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, armorY);
        mc.getRenderItem().renderItemOverlays(mc.fontRenderer, stack, x, armorY);
        (mc.getRenderItem()).zLevel = 0.0F;
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableCull();
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.disableLighting();
        GlStateManager.scale(0.5F, 0.5F, 0.5F);
        GlStateManager.disableDepth();
        renderEnchantmentText(stack, x, enchantY);
        GlStateManager.enableDepth();
        GlStateManager.scale(2.0F, 2.0F, 2.0F);
        GlStateManager.popMatrix();
    }

    private void renderEnchantmentText(ItemStack stack, int x, int y) {
        int enchantmentY = y;

        if (stack.getItem() instanceof ItemArmor || stack.getItem() instanceof ItemSword
                || stack.getItem() instanceof ItemTool || stack.getItem() instanceof ItemElytra) {
            if (durability.getValue()) {
                float armorPercent = ((float)(stack.getMaxDamage()-stack.getItemDamage()) /  (float)stack.getMaxDamage())*100.0f;
                int armorBarPercent = (int)Math.min(armorPercent, 999.0f);
                Dactyl.fontUtil.drawStringWithShadow(String.valueOf(armorBarPercent)+"%", x * 2, y - 10, stack.getItem().getRGBDurabilityForDisplay(stack));
            }
        }

        if(stack.getItem() != null && !(stack.getItem() instanceof ItemAir)) {
            NBTTagList enchants = stack.getEnchantmentTagList();
            if (enchants != null) {
                for (int index = 0; index < enchants.tagCount(); ++index) {
                    short id = enchants.getCompoundTagAt(index).getShort("id");
                    short level = enchants.getCompoundTagAt(index).getShort("lvl");
                    Enchantment enc = Enchantment.getEnchantmentByID(id);
                    if (enc != null) {
                        String encName = enc.isCurse() ? enc.getTranslatedName(level).substring(11).substring(0, 1).toLowerCase() : enc.getTranslatedName(level).substring(0, 3).toLowerCase();
                        if(!String.valueOf(level).equalsIgnoreCase("1") && !enc.isCurse()) {
                            encName = enc.getTranslatedName(level).substring(0, 2).toLowerCase()+String.valueOf(level);
                        } else if(String.valueOf(level).equalsIgnoreCase("1") && !enc.isCurse()) {
                            encName = enc.getTranslatedName(level).substring(0, 3).toLowerCase();
                        }
                        if(enc.isCurse()) {
                            encName = "Van";
                        }
                        encName = encName.substring(0, 1).toUpperCase() + encName.substring(1);
                        Dactyl.fontUtil.drawStringWithShadow(encName, x * 2, enchantmentY, 0xffffffff);
                        enchantmentY += 8;
                    }
                }
            }
        }
        if (stack.getItem() == Items.GOLDEN_APPLE && stack.hasEffect()) {
            Dactyl.fontUtil.drawStringWithShadow("God", (x * 2), enchantmentY, 0xff9e1800);
        }
    }

    private String getDisplayName(EntityPlayer player) {
        TextFormatting color;
        String name = player.getDisplayName().getFormattedText();
        if (!this.health.getValue())
            return name;
        float health = player.getHealth()+player.getAbsorptionAmount();
        if(health <= 0) {
            health = 1;
        }
        if (health > 18.0F) {
            color = TextFormatting.GREEN;
        } else if (health > 16.0F) {
            color = TextFormatting.DARK_GREEN;
        } else if (health > 12.0F) {
            color = TextFormatting.YELLOW;
        } else if (health > 8.0F) {
            color = TextFormatting.GOLD;
        } else if (health > 5.0F) {
            color = TextFormatting.RED;
        } else {
            color = TextFormatting.DARK_RED;
        }
        String gm = "";
        String p = "";
        if(gamemode.getValue()) gm+= (" ["+getGMText(player)+"]");
        if(ping.getValue()) p+= (" "+String.valueOf(EntityUtil.getPing(player))+"ms");
        float totalHealth = player.getHealth() + player.getAbsorptionAmount();
        int pHealth = (int)Math.ceil(totalHealth);
        if(pHealth <= 0) {
            pHealth = 1;
        }
        name = name + gm + p + color + " " + String.valueOf(pHealth);
        if(totemPops.getValue() && getTotemPops(player.getName()) > 0) {
            name+= TextFormatting.AQUA + " -" + String.valueOf(getTotemPops(player.getName()));
        }
        return name;
    }

    private int getDisplayColour(EntityPlayer player) {
        int colour = 0xffffffff;
        if (Dactyl.friendManager.isFriend(player.getName()))
            return 0xff55FFFF;
        if (player.isInvisible()) {
            colour = 0xff910022;
        } else if (player.isSneaking()) {
            colour = -6481515;
        }
        return colour;
    }


    private double interpolate(double previous, double current, float delta) {
        return previous + (current - previous) * delta;
    }
}
