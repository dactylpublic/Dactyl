package me.chloe.dactyl.module.impl.movement;

import me.chloe.dactyl.event.impl.network.PacketEvent;
import me.chloe.dactyl.setting.Setting;
import me.chloe.dactyl.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiScreenHorseInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.AbstractChestHorse;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EntityControl extends Module {
    public Setting<Boolean> mountBypass = new Setting<Boolean>("MountBypass", true);
    public Setting<Boolean> dupeButton = new Setting<Boolean>("DupeButton", true);
    public static EntityControl INSTANCE;
    public EntityControl() {
        super("EntityControl", Category.MOVEMENT);
        INSTANCE = this;
    }

    public boolean ignoring = false;

    @SubscribeEvent
    public void onClick(GuiScreenEvent.ActionPerformedEvent.Pre paramPre) {
        if (paramPre.getGui() instanceof GuiScreenHorseInventory && (paramPre.getButton()).id == 133769420)
            for (Entity entity : (Minecraft.getMinecraft()).world.loadedEntityList) {
                if (entity instanceof EntityLivingBase && entity != (Minecraft.getMinecraft()).player && ((EntityLivingBase)entity).getHealth() > 0.0F && (Minecraft.getMinecraft()).player.getDistance(entity) <= 2.5D && entity instanceof net.minecraft.entity.passive.AbstractChestHorse) {
                    ignoring = true;
                    Vec3d vec3d = new Vec3d(entity.posX, entity.posY, entity.posZ);
                    (Minecraft.getMinecraft()).player.connection.sendPacket(new CPacketUseEntity(entity, EnumHand.MAIN_HAND, vec3d));
                    ignoring = false;
                }
            }
    }

    @SubscribeEvent
    public void onOpenGui(GuiScreenEvent.InitGuiEvent paramInitGuiEvent) {
        if(dupeButton.getValue()) {
            if (paramInitGuiEvent.getGui() instanceof GuiScreenHorseInventory) {
                GuiScreenHorseInventory guiScreenHorseInventory = (GuiScreenHorseInventory) paramInitGuiEvent.getGui();
                paramInitGuiEvent.getButtonList().add(new DupeBut(133769420, guiScreenHorseInventory.getGuiLeft(), guiScreenHorseInventory.getGuiTop() - 20, 50, 20, "Dupe"));
            }
        }
    }


    @Override
    public void onClientUpdate() {
        this.setModuleInfo(mountBypass.getValue() ? "Bypass" : "Riding");
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if(mc.player == null || mc.world == null) {
            return;
        }
        if(event.getType() == PacketEvent.PacketType.OUTGOING) {
            if (mountBypass.getValue()) {
                if (!ignoring) {
                    if (event.getPacket() instanceof CPacketUseEntity) {
                        if (((CPacketUseEntity) event.getPacket()).getEntityFromWorld(mc.world) instanceof AbstractChestHorse && ((CPacketUseEntity) event.getPacket()).getAction() == CPacketUseEntity.Action.INTERACT_AT) {
                            event.setCanceled(true);
                        }
                    }
                }
            }
        }
    }

    public class DupeBut extends GuiButton {
        public void drawButton(Minecraft paramMinecraft, int paramInt1, int paramInt2, float paramFloat) {
            if (this.visible) {
                FontRenderer fontRenderer = paramMinecraft.fontRenderer;
                paramMinecraft.getTextureManager().bindTexture(BUTTON_TEXTURES);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                this.hovered = (paramInt1 >= this.x && paramInt2 >= this.y && paramInt1 < this.x + this.width && paramInt2 < this.y + this.height);
                int i = getHoverState(this.hovered);
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                drawTexturedModalRect(this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
                drawTexturedModalRect(this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
                mouseDragged(paramMinecraft, paramInt1, paramInt2);
                int j = 14737632;
                if (this.packedFGColour != 0) {
                    j = this.packedFGColour;
                } else if (!this.enabled) {
                    j = 10526880;
                } else if (this.hovered) {
                    j = 16777120;
                }
                drawCenteredString(fontRenderer, this.displayString, this.x + this.width / 2, this.y + (this.height - 8) / 2, j);
            }
        }

        public DupeBut(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, String paramString) {
            super(paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramString);
        }

        protected int getHoverState(boolean paramBoolean) {
            byte b = 1;
            if (!this.enabled) {
                b = 0;
            } else if (paramBoolean) {
                b = 2;
            }
            return b;
        }
    }
}
