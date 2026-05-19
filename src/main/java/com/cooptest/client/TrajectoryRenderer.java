package com.cooptest.client;

import com.cooptest.GrabInputHandler;
import com.cooptest.PoseNetworking;
import com.cooptest.PoseState;
import com.mojang.blaze3d.buffers.*;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.*;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import net.minecraft.client.gl.*;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.render.RenderLayer;

public class TrajectoryRenderer {

    private static final int TRAJECTORY_POINTS = 30;
    private static final float TIME_STEP = 0.1f;
    private static final float GRAVITY = 0.08f;
    private static final float DRAG = 0.02f;

    private static final float DOT_SIZE = 0.08f;
    private static final float MIN_POWER_MULT = 1.5f;
    private static final float MAX_POWER_MULT = 3.5f;
    // BufferAllocator is the correct allocator type — takes a byte size
    private static final BufferAllocator ALLOCATOR = new BufferAllocator(786432);

    // DEBUG_FILLED_BOX already has blending and no cull — use it directly.
// POSITION_COLOR_SNIPPET is private so you can't build on top of it.
    private static final RenderPipeline TRAJECTORY_PIPELINE = RenderPipelines.DEBUG_FILLED_BOX;
    public static void register() {
        WorldRenderEvents.END_MAIN.register(TrajectoryRenderer::render);
    }

    private static void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        PoseState pose = PoseNetworking.poseStates.getOrDefault(
            client.player.getUuid(), PoseState.NONE
        );

        if (pose != PoseState.GRAB_HOLDING) return;

        float chargeProgress = GrabInputHandler.getThrowChargeProgress();
        if (chargeProgress <= 0) return;

        float power = MIN_POWER_MULT + (MAX_POWER_MULT - MIN_POWER_MULT) * chargeProgress;

        Vec3d lookVec = client.player.getRotationVec(client.getRenderTickCounter().getDynamicDeltaTicks());
        Vec3d startPos = client.player.getEyePos().add(0, 0.5, 0); // Above head

        Vec3d velocity = lookVec.multiply(power);

        Vec3d[] points = new Vec3d[TRAJECTORY_POINTS];
        Vec3d pos = startPos;
        Vec3d vel = velocity;

        for (int i = 0; i < TRAJECTORY_POINTS; i++) {
            points[i] = pos;

            vel = vel.add(0, -GRAVITY, 0);
            vel = vel.multiply(1.0 - DRAG);
            pos = pos.add(vel);

            if (pos.y < client.player.getY() - 10) break;
        }

        renderTrajectoryDots(context, points, chargeProgress);
    }

    private static void renderTrajectoryDots(WorldRenderContext context, Vec3d[] points, float charge) {
        Camera camera = context.gameRenderer().getCamera();
        Vec3d camPos = camera.getCameraPos();

        MatrixStack matrices = context.matrices();
        matrices.push();
        matrices.translate(-camPos.x, -camPos.y, -camPos.z);
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        VertexConsumer consumer = context.consumers().getBuffer(
                RenderLayer.of("trajectory", RenderSetup.builder(
                        RenderPipelines.DEBUG_FILLED_BOX
                ).build())
        );

        int r = (int)(charge * 255);
        int g = (int)((1 - charge) * 255);
        int b = 50;

        for (int i = 0; i < points.length && points[i] != null; i++) {
            Vec3d point = points[i];
            int fadeAlpha = (int)(200 * (1.0f - (float)i / points.length));
            float size = DOT_SIZE * (1.0f - (float)i / points.length * 0.5f);
            float x = (float)point.x;
            float y = (float)point.y;
            float z = (float)point.z;

            consumer.vertex(matrix, x-size, y-size, z+size).color(r, g, b, fadeAlpha);
            consumer.vertex(matrix, x+size, y-size, z+size).color(r, g, b, fadeAlpha);
            consumer.vertex(matrix, x+size, y+size, z+size).color(r, g, b, fadeAlpha);
            consumer.vertex(matrix, x-size, y+size, z+size).color(r, g, b, fadeAlpha);
            consumer.vertex(matrix, x+size, y-size, z-size).color(r, g, b, fadeAlpha);
            consumer.vertex(matrix, x-size, y-size, z-size).color(r, g, b, fadeAlpha);
            consumer.vertex(matrix, x-size, y+size, z-size).color(r, g, b, fadeAlpha);
            consumer.vertex(matrix, x+size, y+size, z-size).color(r, g, b, fadeAlpha);
            consumer.vertex(matrix, x-size, y+size, z-size).color(r, g, b, fadeAlpha);
            consumer.vertex(matrix, x-size, y+size, z+size).color(r, g, b, fadeAlpha);
            consumer.vertex(matrix, x+size, y+size, z+size).color(r, g, b, fadeAlpha);
            consumer.vertex(matrix, x+size, y+size, z-size).color(r, g, b, fadeAlpha);
        }

        matrices.pop();
        // context.consumers() flushes automatically at end of frame — no manual draw call needed
    }
}