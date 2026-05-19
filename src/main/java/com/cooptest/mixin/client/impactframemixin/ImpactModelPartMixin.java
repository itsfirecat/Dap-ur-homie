package com.cooptest.mixin.client.impactframemixin;

import com.cooptest.client.CoopImpactHandler;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ModelPart.class)
public abstract class ImpactModelPartMixin {
// WASTED 7 HR ON THIS DOSNT WORK AS INTNEDE
    private static boolean shouldFlash() {
        return CoopImpactHandler.playing && CoopImpactHandler.renderingPlayer;
    }

    @ModifyVariable(
            method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0  // light
    )
    private int forceLight(int light) {
        return shouldFlash() ? 15728880 : light;
    }

    @ModifyVariable(
            method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 1  // overlay
    )
    private int forceOverlay(int overlay) {
        if (!shouldFlash()) return overlay;
        return CoopImpactHandler.whiteFrame ? OverlayTexture.DEFAULT_UV : 0;
    }

    @ModifyVariable(
            method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private VertexConsumer wrapConsumer(VertexConsumer original) {
        if (!shouldFlash()) return original;
        final boolean white = CoopImpactHandler.whiteFrame;

        return new VertexConsumer() {
            @Override
            public VertexConsumer vertex(float x, float y, float z) {
                return original.vertex(x, y, z);
            }
            @Override
            public VertexConsumer color(int argb) {
                return white ? original.color(0xFF000000) : original.color(0xFFFFFFFF);
            }

            @Override
            public VertexConsumer texture(float u, float v) {
                return null;
            }

            @Override
            public VertexConsumer overlay(int u, int v) {
                return null;
            }

            @Override
            public VertexConsumer light(int u, int v) {
                return null;
            }

            @Override
            public VertexConsumer normal(float x, float y, float z) {
                return null;
            }

            @Override
            public VertexConsumer color(int r, int g, int b, int a) {
                return white ? original.color(0, 0, 0, 255) : original.color(255, 255, 255, 255);
            }
            @Override
            public VertexConsumer lineWidth(float width) {
                return original.lineWidth(width);
            }
        };
    }
}