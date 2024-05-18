package net.ccbluex.liquidbounce.injection.forge.mixins.render;

import net.ccbluex.liquidbounce.CrossSine;
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura;
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura2;
import net.ccbluex.liquidbounce.features.module.modules.visual.Animations;
import net.ccbluex.liquidbounce.features.module.modules.visual.Interface;
import net.ccbluex.liquidbounce.features.module.modules.visual.OldAnimations;
import net.ccbluex.liquidbounce.features.module.modules.visual.NoRender;
import net.ccbluex.liquidbounce.utils.SpoofItemUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.*;
import net.minecraft.util.MathHelper;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer {
    @Shadow
    private float prevEquippedProgress;

    @Shadow
    private float equippedProgress;
    @Shadow
    private int equippedItemSlot = -1;
    @Shadow
    @Final
    private Minecraft mc;

    @Shadow
    protected abstract void rotateArroundXAndY(float angle, float angleY);

    @Shadow
    protected abstract void setLightMapFromPlayer(AbstractClientPlayer clientPlayer);

    @Shadow
    protected abstract void rotateWithPlayerRotations(EntityPlayerSP entityPlayerSP, float partialTicks);
    @Shadow
    public abstract void renderItem(EntityLivingBase entityIn, ItemStack heldStack, ItemCameraTransforms.TransformType transform);
    @Shadow
    private ItemStack itemToRender;

    @Shadow
    private RenderItem itemRenderer;

    @Shadow
    protected abstract void renderItemMap(AbstractClientPlayer clientPlayer, float pitch, float equipmentProgress, float swingProgress);

    @Shadow
    protected abstract boolean isBlockTranslucent(Block p_isBlockTranslucent_1_);

    @Shadow
    protected abstract void performDrinking(AbstractClientPlayer clientPlayer, float partialTicks);

    @Shadow
    protected abstract void doBlockTransformations();

    @Shadow
    protected abstract void doBowTransformations(float partialTicks, AbstractClientPlayer clientPlayer);

    @Shadow
    protected abstract void doItemUsedTransformations(float swingProgress);

    @Shadow
    protected abstract void renderPlayerArm(AbstractClientPlayer clientPlayer, float equipProgress, float swingProgress);

    private Animations animations;

    @Inject(method = "updateEquippedItem", at = @At("HEAD"), cancellable = true)
    public void updateEquippedItemHead(CallbackInfo i) {
        this.prevEquippedProgress = this.equippedProgress;
        ItemStack itemstack = SpoofItemUtils.INSTANCE.getStack();
        boolean flag = false;
        if (this.itemToRender != null && itemstack != null) {
            if (!this.itemToRender.getIsItemStackEqual(itemstack)) {
                if (!this.itemToRender.getItem().shouldCauseReequipAnimation(this.itemToRender, itemstack, this.equippedItemSlot != SpoofItemUtils.INSTANCE.getSlot())) {
                    this.itemToRender = itemstack;
                    this.equippedItemSlot = SpoofItemUtils.INSTANCE.getSlot();
                    return;
                }

                flag = true;
            }
        } else flag = this.itemToRender != null || itemstack != null;

        float f = 0.4F;
        float f1 = flag ? 0.0F : 1.0F;
        float f2 = MathHelper.clamp_float(f1 - this.equippedProgress, -f, f);
        this.equippedProgress += f2;
        if (this.equippedProgress < 0.1F) {
            this.itemToRender = itemstack;
            this.equippedItemSlot = SpoofItemUtils.INSTANCE.getSlot();
        }
        i.cancel();
    }
    /**
     * @author Liuli
     */
    @Overwrite
    private void transformFirstPersonItem(float equipProgress, float swingProgress) {
        doItemRenderGLTranslate();
        GlStateManager.translate(0.0F, equipProgress * -0.6F, 0.0F);
        GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
        float f = MathHelper.sin(swingProgress * swingProgress * 3.1415927F);
        float f1 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * 3.1415927F);
        GlStateManager.rotate(f * -20.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(f1 * -20.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(f1 * -80.0F, 1.0F, 0.0F, 0.0F);
        doItemRenderGLScale();
    }

    private void oldBlockAnimation() {
        GlStateManager.translate(-0.5F, 0.4F, -0.1F);
        GlStateManager.rotate(30.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-80.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(60.0F, 0.0F, 1.0F, 0.0F);
    }

    /**
     * @author Liuli
     */
    @Overwrite
    public void renderItemInFirstPerson(float partialTicks) {
        if (animations == null) {
            animations = CrossSine.moduleManager.getModule(Animations.class);
        }
        float f = 1.0F - (this.prevEquippedProgress + (this.equippedProgress - this.prevEquippedProgress) * partialTicks);
        EntityPlayerSP abstractclientplayer = mc.thePlayer;
        float f1 = abstractclientplayer.getSwingProgress(partialTicks);
        float f2 = abstractclientplayer.prevRotationPitch + (abstractclientplayer.rotationPitch - abstractclientplayer.prevRotationPitch) * partialTicks;
        float f3 = abstractclientplayer.prevRotationYaw + (abstractclientplayer.rotationYaw - abstractclientplayer.prevRotationYaw) * partialTicks;
        this.rotateArroundXAndY(f2, f3);
        this.setLightMapFromPlayer(abstractclientplayer);
        this.rotateWithPlayerRotations(abstractclientplayer, partialTicks);
        GlStateManager.enableRescaleNormal();
        GlStateManager.pushMatrix();

        if (this.itemToRender != null) {
            if (OldAnimations.INSTANCE.getBlockAnimation().get() && (itemToRender.getItem() instanceof ItemCarrotOnAStick || itemToRender.getItem() instanceof ItemFishingRod)) {
                GlStateManager.translate(0.08F, -0.027F, -0.33F);
                GlStateManager.scale(0.93F, 1.0F, 1.0F);
            }
            final KillAura killAura = CrossSine.moduleManager.getModule(KillAura.class);


            if (this.itemToRender.getItem() instanceof ItemMap) {
                this.renderItemMap(abstractclientplayer, f2, f, f1);
            } else if ((abstractclientplayer.isUsingItem() || ((itemToRender.getItem() instanceof ItemSword) && (killAura.getDisplayBlocking() && killAura.getCurrentTarget() != null) || (KillAura.INSTANCE.getState() && KillAura.INSTANCE.getDisplayBlocking() && KillAura.INSTANCE.getCurrentTarget() != null) || (KillAura2.INSTANCE.getState() && KillAura2.INSTANCE.getCanBlock() && KillAura2.INSTANCE.getTarget() != null)))) {
                switch (this.itemToRender.getItemUseAction()) {
                    case NONE:
                        this.transformFirstPersonItem(f, 0.0F);
                        break;
                    case EAT:
                    case DRINK:
                        this.performDrinking(abstractclientplayer, partialTicks);
                        if (OldAnimations.INSTANCE.getBlockAnimation().get()) {
                            this.transformFirstPersonItem(f, f1);
                        } else {
                            this.transformFirstPersonItem(f, 0.0F);
                        }
                        break;
                    case BLOCK:
                        if (animations.getState()) {
                            switch (animations.getBlockingModeValue().get()) {
                                case "1.7": {
                                    transformFirstPersonItem(f, f1);
                                    oldBlockAnimation();
                                    break;
                                }
                                case "1.8": {
                                    this.transformFirstPersonItem(f, f1);
                                    this.doBlockTransformations();
                                    GlStateManager.translate(-0.35F, 0.2F, 0.0F);
                                    break;
                                }
                                case "Spin": {
                                    transformFirstPersonItem(f / 3.0f, 0.0f);
                                    GlStateManager.translate(0.0, 0.3, 0.0);
                                    final float rot = MathHelper.sin(MathHelper.sqrt_float(f2) * 3.1415927f);
                                    GlStateManager.rotate(-rot, 0.0f, 0.0f, rot);
                                    GlStateManager.rotate(-rot * 2.0f, rot * 7.0f, 0.0f, 0.0f);
                                    GlStateManager.rotate(-rot * 2.0f, 16.0f, 0.0f, 15.0f);
                                    this.doBlockTransformations();
                                }
                                case "Slash": {
                                    final float var = MathHelper.sin((float) (MathHelper.sqrt_float(f1) * Math.PI));
                                    transformFirstPersonItem(f / 1.8f, 0.0f);
                                    this.doBlockTransformations();
                                    final float var16 = MathHelper.sin((float) (f1 * f1 * Math.PI));
                                    GlStateManager.rotate(-var16 * 0f, 0.0f, 1.0f, 0.0f);
                                    GlStateManager.rotate(-var * 62f, 0.0f, 0.0f, 1.0f);
                                    GlStateManager.rotate(-var * 0f, 1.5f, 0.0f, 0.0f);
                                    break;
                                }
                                case "Sigma4": {
                                    final float var = MathHelper.sin((float) (MathHelper.sqrt_float(f1) * Math.PI));
                                    transformFirstPersonItem(f / 2.0F, 0.0F);
                                    GlStateManager.rotate(-var * 55 / 2.0F, -8.0F, -0.0F, 9.0F);
                                    GlStateManager.rotate(-var * 45, 1.0F, var / 2, 0.0F);
                                    this.doBlockTransformations();
                                    break;
                                }
                                case "Jello": {
                                    this.transformFirstPersonItem(0.0f, 0.0f);
                                    this.doBlockTransformations();
                                    final int alpha = (int)Math.min(255L, ((System.currentTimeMillis() % 255L > 127L) ? Math.abs(Math.abs(System.currentTimeMillis()) % 255L - 255L) : (System.currentTimeMillis() % 255L)) * 2L);
                                    GlStateManager.translate(0.3f, -0.0f, 0.4f);
                                    GlStateManager.rotate(0.0f, 0.0f, 0.0f, 1.0f);
                                    GlStateManager.translate(0.0f, 0.5f, 0.0f);
                                    GlStateManager.rotate(90.0f, 1.0f, 0.0f, -1.0f);
                                    GlStateManager.translate(0.6f, 0.5f, 0.0f);
                                    GlStateManager.rotate(-90.0f, 1.0f, 0.0f, -1.0f);
                                    GlStateManager.rotate(-10.0f, 1.0f, 0.0f, -1.0f);
                                    GlStateManager.rotate(abstractclientplayer.isSwingInProgress ? (-alpha / 5.0f) : 1.0f, 1.0f, -0.0f, 1.0f);
                                    break;
                                }
                                case "Exhibition": {
                                    transformFirstPersonItem(f, 0.83F);
                                    float f4 = MathHelper.sin(MathHelper.sqrt_float(f1) * 3.83F);
                                    GlStateManager.translate(-0.5F, 0.2F, 0.2F);
                                    GlStateManager.rotate(-f4 * 0.0F, 0.0F, 0.0F, 0.0F);
                                    GlStateManager.rotate(-f4 * 43.0F, 58.0F, 23.0F, 45.0F);
                                    doBlockTransformations();
                                    break;
                                }
                            }
                        } else {
                            this.transformFirstPersonItem(f + 0.1F, f1);
                            this.doBlockTransformations();
                            GlStateManager.translate(-0.5F, 0.2F, 0.0F);
                        }
                        break;
                    case BOW:
                        if (OldAnimations.INSTANCE.getBlockAnimation().get()) {
                            this.transformFirstPersonItem(f, f1);
                        } else {
                            this.transformFirstPersonItem(f, 0.0F);
                        }
                        this.doBowTransformations(partialTicks, abstractclientplayer);
                }
            } else {
                if (!animations.getState() || !animations.getFluxAnimation().get())
                    this.doItemUsedTransformations(f1);
                this.transformFirstPersonItem(f, f1);
            }
            this.renderItem(abstractclientplayer, this.itemToRender, ItemCameraTransforms.TransformType.FIRST_PERSON);
        } else if (!abstractclientplayer.isInvisible()) {
            this.renderPlayerArm(abstractclientplayer, f, f1);
        }

        GlStateManager.popMatrix();
        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
    }

    private void doItemRenderGLTranslate() {
        if (animations.getState()) {
            GlStateManager.translate(0.56F + animations.getItemPosXValue().get(), -0.52F + (animations.getBlockingModeValue().equals("1.7") && (KillAura.INSTANCE.getCurrentTarget() != null && KillAura.INSTANCE.getDisplayBlocking() || KillAura2.INSTANCE.getTarget() != null && KillAura2.INSTANCE.getCanBlock() || mc.thePlayer.isBlocking()) ? 0.08 + animations.getItemPosYValue().get() : animations.getItemPosYValue().get()), -0.71999997F + animations.getItemPosZValue().get());
        } else {
            GlStateManager.translate(0.56F, -0.52F, -0.71999997F);
        }
    }

    private void doItemRenderGLScale() {
        if (animations.getState()) {
            GlStateManager.scale(((float) Animations.INSTANCE.getItemScaleValue().get() / 100) * 0.4, ((float) Animations.INSTANCE.getItemScaleValue().get() / 100) * 0.4, ((float) Animations.INSTANCE.getItemScaleValue().get() / 100) * 0.4);
        } else {
            GlStateManager.scale(0.4, 0.4, 0.4);
        }
    }

    /**
     * @author Liuli
     */
    @Inject(method = "renderFireInFirstPerson", at = @At("HEAD"), cancellable = true)
    private void renderFireInFirstPerson(final CallbackInfo callbackInfo) {
        final NoRender NoRender = CrossSine.moduleManager.getModule(NoRender.class);

        if (NoRender.getState() && NoRender.getFireEffect().get()) {
            //vanilla's method
            GlStateManager.color(1.0F, 1.0F, 1.0F, 0.9F);
            GlStateManager.depthFunc(519);
            GlStateManager.depthMask(false);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableBlend();
            GlStateManager.depthMask(true);
            GlStateManager.depthFunc(515);
            callbackInfo.cancel();
        }
    }
}