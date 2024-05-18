
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.CrossSine;
import net.ccbluex.liquidbounce.event.KeyEvent;
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura;
import net.ccbluex.liquidbounce.features.module.modules.player.InvManager;
import net.ccbluex.liquidbounce.features.module.modules.visual.Interface;
import net.ccbluex.liquidbounce.features.module.modules.world.Stealer;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.extensions.RendererExtensionKt;
import net.ccbluex.liquidbounce.utils.render.EaseUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(GuiContainer.class)
public abstract class MixinGuiContainer extends MixinGuiScreen {
    @Shadow
    protected int xSize;
    @Shadow
    protected int ySize;
    @Shadow
    protected int guiLeft;
    @Shadow
    protected int guiTop;

    private long guiOpenTime = -1;

    private boolean translated = false;

    @Shadow
    protected abstract boolean checkHotbarKeys(int keyCode);

    private GuiButton stealButton, chestStealerButton, InventorymanagerButton, killAuraButton;
    @Shadow private int dragSplittingButton;
    @Shadow private int dragSplittingRemnant;
    private float progress = 0F;
    private long lastMS = 0L;

    @Inject(method = "initGui", at = @At("HEAD"), cancellable = true)
    public void injectInitGui(CallbackInfo callbackInfo){
        if (!CrossSine.INSTANCE.getDestruced()) {
            GuiScreen guiScreen = Minecraft.getMinecraft().currentScreen;
            if (guiScreen instanceof GuiChest) {
                if (Interface.INSTANCE.getButtonValue().get()) {
                    buttonList.add(killAuraButton = new GuiButton(1024576, 5, 5, 150, 20, "Disable KillAura"));
                    buttonList.add(InventorymanagerButton = new GuiButton(321123, 5, 27, 150, 20, "Disable InventoryManager"));
                    buttonList.add(chestStealerButton = new GuiButton(727, 5, 49, 150, 20, "Disable Stealer"));
                }
            }
            lastMS = System.currentTimeMillis();
            progress = 0F;
        }
     }
    @Inject(method = "initGui", at = @At("RETURN"))
    private void initGuiReturn(CallbackInfo callbackInfo) {
        guiOpenTime = System.currentTimeMillis();
    }
    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 1024576)
            CrossSine.moduleManager.getModule(KillAura.class).setState(false);
        if (button.id == 727)
            CrossSine.moduleManager.getModule(Stealer.class).setState(false);
        if (button.id == 321123)
            CrossSine.moduleManager.getModule(InvManager.class).setState(false);
    }

    @Inject(method = "drawScreen", at = @At("HEAD"), cancellable = true)
    private void drawScreenHead(CallbackInfo callbackInfo) {
        if (!CrossSine.INSTANCE.getDestruced()) {

            Stealer stealer = CrossSine.moduleManager.getModule(Stealer.class);
            Minecraft mc = Minecraft.getMinecraft();
            GuiScreen guiScreen = mc.currentScreen;
            if (stealer.getState() && stealer.getFreelookValue().get() && guiScreen instanceof GuiChest) {
                if (!stealer.getSilentValue().get()) {
                    mc.inGameHasFocus = true;
                    mc.mouseHelper.grabMouseCursor();
                }
            }
            if (stealer.getState() && stealer.getSilentValue().get() && guiScreen instanceof GuiChest) {
                GuiChest chest = (GuiChest) guiScreen;
                if (!(stealer.getChestTitleValue().get() && (chest.lowerChestInventory == null || !chest.lowerChestInventory.getName().contains(new ItemStack(Item.itemRegistry.getObject(new ResourceLocation("minecraft:chest"))).getDisplayName())))) {
                    // mouse focus
                    mc.setIngameFocus();
                    mc.currentScreen = guiScreen;

                    // hide GUI
                    if (stealer.getSilentTitleValue().get() && stealer.getSilentValue().get()) {
                        RendererExtensionKt.drawCenteredString(Fonts.fontSFUI35, "ChestStealer Silent", width / 2, (height / 2) + 30, 0xffffffff, true);
                    }
                    callbackInfo.cancel();
                }
            } else {
                mc.currentScreen.drawWorldBackground(0);
                if (Interface.INSTANCE.getInventoryAnimation().get() && Interface.INSTANCE.getState() && guiScreen instanceof GuiInventory) {
                    double pct = Math.max(300 - (System.currentTimeMillis() - guiOpenTime), 0) / ((double) 300);
                    if (pct != 0) {
                        GL11.glPushMatrix();
                        pct = EaseUtils.INSTANCE.easeInCirc(pct);

                        double scale = 1 - pct;
                        GL11.glScaled(scale, scale, scale);
                        GL11.glTranslated(((guiLeft + (xSize * 0.5 * pct)) / scale) - guiLeft,
                                ((guiTop + (ySize * 0.5d * pct)) / scale) - guiTop,
                                0);
                        translated = true;
                    }
                }
            }
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void checkCloseClick(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
        if (!CrossSine.INSTANCE.getDestruced()) {
            if (mouseButton - 100 == mc.gameSettings.keyBindInventory.getKeyCode()) {
                mc.thePlayer.closeScreen();
                ci.cancel();
            }
        }
    }

    @Inject(method = "mouseClicked", at = @At("TAIL"))
    private void checkHotbarClicks(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
        checkHotbarKeys(mouseButton - 100);
    }

    @Inject(method = "updateDragSplitting", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;copy()Lnet/minecraft/item/ItemStack;"), cancellable = true)
    private void fixRemnants(CallbackInfo ci) {
        if (this.dragSplittingButton == 2) {
            this.dragSplittingRemnant = mc.thePlayer.inventory.getItemStack().getMaxStackSize();
            ci.cancel();
        }
    }

    @Inject(method = "drawScreen", at = @At("RETURN"))
    private void drawScreenReturn(CallbackInfo callbackInfo) {
        if (translated) {
            GL11.glPopMatrix();
            translated = false;
        }
    }

    @Inject(method = "keyTyped", at = @At("HEAD"))
    private void keyTyped(char typedChar, int keyCode, CallbackInfo ci) {
        Stealer stealer = CrossSine.moduleManager.getModule(Stealer.class);
        try {
            if (stealer.getState() && mc.currentScreen instanceof GuiChest)
                CrossSine.eventManager.callEvent(new KeyEvent(keyCode == 0 ? typedChar + 256 : keyCode));
        }catch (Exception e){

        }
    }
}