package jp.kaiz.metascrollchanger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.stream.IntStream;

@Mod(modid = MetaScrollChanger.MODID, version = MetaScrollChanger.VERSION)
public class MetaScrollChanger {
    public static final String MODID = "metascrollchanger";
    public static final String VERSION = "1.12.2_1.0";

    private static final String CATEGORY_KEY = "msc.key";

    private MSCGuiInGame guiInGame;
    private Minecraft mc;
    public static final KeyBinding KEY_TRIGGER = new KeyBinding("msc.trigger", Keyboard.KEY_TAB, CATEGORY_KEY);

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ClientRegistry.registerKeyBinding(KEY_TRIGGER);
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if (event.getSide() == Side.CLIENT) {
            this.mc = Minecraft.getMinecraft();
            this.guiInGame = new MSCGuiInGame(Minecraft.getMinecraft());
            MinecraftForge.EVENT_BUS.register(this);
            FMLCommonHandler.instance().bus().register(this);
        }
    }

    @SubscribeEvent
    public void onRenderGui(RenderGameOverlayEvent.Pre event) {
        this.guiInGame.onRenderGui(event);
    }

    private int currentItemSlot;

    @SubscribeEvent
    public void onMouseInput(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            this.currentItemSlot = event.player.inventory.currentItem;
        }
    }

    @SubscribeEvent
    public void onMouseInput(InputEvent.MouseInputEvent event) {
        if (this.mc.playerController.isInCreativeMode()) {
            if (Keyboard.isKeyDown(MetaScrollChanger.KEY_TRIGGER.getKeyCode())) {
                EntityPlayer player = this.mc.player;
                InventoryPlayer inventory = player.inventory;
                inventory.currentItem = this.currentItemSlot;
                int i = MathHelper.clamp(Mouse.getDWheel(), -1, 1);
                if (i == 0) {
                    return;
                }
                ItemStack itemStack = inventory.getCurrentItem();
                Item item = itemStack.getItem();
                NonNullList<ItemStack> list = NonNullList.create();
                item.getSubItems(CreativeTabs.SEARCH, list);
                int size = list.size();
                if (size == 1) {
                    return;
                }

                int index = IntStream.range(0, size).filter(i1 -> list.get(i1).isItemEqual(itemStack)).findFirst().orElse(-1);
                if (index == -1) {
                    return;
                }
                int meta = (index + i + size) % size;
                int slot = 36 + inventory.currentItem;
                this.mc.playerController.sendSlotPacket(list.get(meta), slot);
            }
        }
    }
}
