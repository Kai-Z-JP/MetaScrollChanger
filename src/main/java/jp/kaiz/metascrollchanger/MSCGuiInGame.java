package jp.kaiz.metascrollchanger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class MSCGuiInGame extends GuiScreen {
    protected static final ResourceLocation widgetsTexPath = new ResourceLocation("textures/gui/widgets.png");
    protected static final RenderItem itemRenderer = new RenderItem();

    public MSCGuiInGame(Minecraft mc) {
        this.mc = mc;
    }

    public void onRenderGui(RenderGameOverlayEvent.Pre event) {
        if (this.mc.playerController.isInCreativeMode()) {
            if (event.type == RenderGameOverlayEvent.ElementType.HOTBAR) {
                if (Keyboard.isKeyDown(MetaScrollChanger.KEY_TRIGGER.getKeyCode())) {
                    this.setScale(event.resolution);

                    this.mc.entityRenderer.setupOverlayRendering();
                    GL11.glEnable(GL11.GL_BLEND);

                    ItemStack itemStack = this.mc.thePlayer.inventory.getCurrentItem();
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

                    int index = IntStream.range(0, size).filter(i -> list.get(i).isItemEqual(itemStack)).findFirst().orElse(-1);
                    if (index == -1) {
                        return;
                    }

                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    this.mc.getTextureManager().bindTexture(widgetsTexPath);
                    this.zLevel = -90.0F;

                    int inventorySize = Math.min(size, 9);

                    this.drawTexturedModalRect(this.width / 2 - 90 - 1, this.height - 44, 0, 0, 190 + 2, 22);
                    this.drawTexturedModalRect(this.width / 2 - 10 - 2, this.height - 44 - 1, 0, 22, 24, 22);

                    GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                    RenderHelper.enableGUIStandardItemLighting();

                    int first = (inventorySize) / 2;
                    int j1 = this.width / 2 - 10 - first * 20 + 2;
                    int k1 = this.height - 38 - 3;
                    for (int i = 0; i < inventorySize; i++) {
                        int damage = (index - first + i + size) % size;
                        this.renderInventorySlot(list.get(damage), j1, k1, event.partialTicks);
                        j1 += 20;
                    }

                    RenderHelper.disableStandardItemLighting();
                    GL11.glDisable(GL12.GL_RESCALE_NORMAL);
                    GL11.glDisable(GL11.GL_BLEND);
                }
            }
        }
    }

    private void setScale(ScaledResolution par1) {
        this.width = par1.getScaledWidth();
        this.height = par1.getScaledHeight();
    }

    protected void renderInventorySlot(ItemStack itemstack, int p_73832_2_, int p_73832_3_, float p_73832_4_) {
        if (itemstack != null) {
            float f1 = (float) itemstack.animationsToGo - p_73832_4_;

            if (f1 > 0.0F) {
                GL11.glPushMatrix();
                float f2 = 1.0F + f1 / 5.0F;
                GL11.glTranslatef((float) (p_73832_2_ + 8), (float) (p_73832_3_ + 12), 0.0F);
                GL11.glScalef(1.0F / f2, (f2 + 1.0F) / 2.0F, 1.0F);
                GL11.glTranslatef((float) (-(p_73832_2_ + 8)), (float) (-(p_73832_3_ + 12)), 0.0F);
            }

            itemRenderer.renderItemAndEffectIntoGUI(this.mc.fontRenderer, this.mc.getTextureManager(), itemstack, p_73832_2_, p_73832_3_);

            if (f1 > 0.0F) {
                GL11.glPopMatrix();
            }

            itemRenderer.renderItemOverlayIntoGUI(this.mc.fontRenderer, this.mc.getTextureManager(), itemstack, p_73832_2_, p_73832_3_);
        }
    }
}
