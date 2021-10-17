package jp.kaiz.metascrollchanger;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Mod(modid = MetaScrollChanger.MODID, version = MetaScrollChanger.VERSION)
public class MetaScrollChanger {
    public static final String MODID = "metascrollchanger";
    public static final String VERSION = "1.7.10_1.0.1";

    private static final String CATEGORY_KEY = "msc.key";

    private MSCGuiInGame guiInGame;
    private Minecraft mc;
    public static final KeyBinding KEY_TRIGGER = new KeyBinding("msc.trigger", Keyboard.KEY_TAB, CATEGORY_KEY);

    @EventHandler
    public void init(FMLInitializationEvent event) {
        ClientRegistry.registerKeyBinding(KEY_TRIGGER);
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        this.mc = Minecraft.getMinecraft();
        this.guiInGame = new MSCGuiInGame(Minecraft.getMinecraft());
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);
    }

    @SubscribeEvent
    public void onRenderGui(RenderGameOverlayEvent.Pre event) {
        this.guiInGame.onRenderGui(event);
    }

    private int currentItemSlot;

    @SubscribeEvent
    public void onMouseInput(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            this.currentItemSlot = this.mc.thePlayer.inventory.currentItem;
        }
    }

    @SubscribeEvent
    public void onMouseInput(InputEvent.MouseInputEvent event) {
        if (this.mc.playerController.isInCreativeMode()) {
            if (MetaScrollChanger.KEY_TRIGGER.getIsKeyPressed()) {
                EntityClientPlayerMP player = this.mc.thePlayer;
                InventoryPlayer inventory = player.inventory;
                inventory.currentItem = this.currentItemSlot;
                int i = MathHelper.clamp_int(Mouse.getDWheel(), -1, 1);
                if (i == 0) {
                    return;
                }
                ItemStack itemStack = inventory.getCurrentItem();
                if (itemStack == null) {
                    return;
                }
                Item item = itemStack.getItem();
                List<ItemStack> list = new ArrayList<>();
                item.getSubItems(item, CreativeTabs.tabAllSearch, list);
                int size = list.size();
                if (size == 1) {
                    return;
                }

                int index = IntStream.range(0, size).filter(i1 -> list.get(i1).isItemEqual(itemStack)).findFirst().orElse(-1);
                if (index == -1) {
                    return;
                }
                int meta = (index + i + size) % size;
                int slot = player.inventoryContainer.inventorySlots.size() - 9 + inventory.currentItem;
                this.mc.playerController.sendSlotPacket(list.get(meta), slot);
            }
        }
    }
}
