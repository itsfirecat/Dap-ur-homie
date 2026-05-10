package com.cooptest.mixin.client;
import com.cooptest.PoseNetworking;
import com.cooptest.PoseState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.HashMap;
import java.util.UUID;
@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {
    @Unique
    private static final HashMap<UUID, Boolean> matrixPushed = new HashMap<>();
    @Unique
    private static final HashMap<UUID, Float> lockedYaw = new HashMap<>();
    @Inject(method = "setupTransforms(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;FF)V", at = @At("RETURN"))
    private void rotateGrabbedPlayer(@Coerce Object stateObj, MatrixStack matrices, float bodyYaw, float animationProgress, CallbackInfo ci) {
        PlayerEntityRenderState state = (PlayerEntityRenderState) stateObj;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;
        Entity entity = client.world.getEntityById(state.id);
        if (!(entity instanceof PlayerEntity player)) return;
        UUID uuid = player.getUuid();
        PoseState pose = PoseNetworking.poseStates.getOrDefault(uuid, PoseState.NONE);
        if (pose == PoseState.GRABBED) {
            float facingYaw;
            Entity vehicle = player.getVehicle();
            if (vehicle instanceof PlayerEntity holder) {
                facingYaw = holder.getYaw();
                lockedYaw.put(player.getUuid(), facingYaw);
            } else {
                if (lockedYaw.containsKey(player.getUuid())) {
                    facingYaw = lockedYaw.get(player.getUuid());
                } else {
                    facingYaw = player.getYaw();
                    lockedYaw.put(player.getUuid(), facingYaw);
                }
            }
            float counterRotation = -bodyYaw + facingYaw;
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(counterRotation));
            matrices.translate(0, 0.9, 0);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
            matrices.translate(0, -0.9, 0);
            matrixPushed.put(player.getUuid(), true);
        } else {
            lockedYaw.remove(player.getUuid());
            matrixPushed.put(player.getUuid(), false);
        }
    }
}