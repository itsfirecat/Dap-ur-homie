package com.cooptest.mixin.client.impactframemixin;

import com.cooptest.client.CoopImpactHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Environment(EnvType.CLIENT)
    @Mixin(LivingEntityRenderer.class)
    public class PlayerRenderFlagMixin {
        @Inject(
                method = "render",
                at = @At("HEAD")
        )
        private void onRenderStart(EntityRenderState state, MatrixStack matrices,
                                   VertexConsumerProvider provider, int light, CallbackInfo ci) {
            if (state instanceof PlayerEntityRenderState) {
                CoopImpactHandler.renderingPlayer = true;
            }
        }

        @Inject(
                method = "render",
                at = @At("RETURN")
        )
        private void onRenderEnd(EntityRenderState state, MatrixStack matrices,
                                 VertexConsumerProvider provider, int light, CallbackInfo ci) {
            if (state instanceof PlayerEntityRenderState) {
                CoopImpactHandler.renderingPlayer = false;
            }
        }
    }
