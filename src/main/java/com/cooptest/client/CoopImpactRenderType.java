package com.cooptest.client;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.util.Identifier;

public class CoopImpactRenderType {

    public static boolean isReady() { return true; } // no shader loading needed

    public static RenderLayer getWhiteLayer(Identifier texture) {
        return RenderLayer.of("cooptest_entity_white",
                RenderSetup.builder(RenderPipelines.DEBUG_FILLED_BOX).build());
    }

    public static RenderLayer getBlackLayer(Identifier texture) {
        return RenderLayer.of("cooptest_entity_black",
                RenderSetup.builder(RenderPipelines.DEBUG_FILLED_BOX).build());
    }

    public static net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener createReloadListener() {
        return new net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return Identifier.of("cooptest", "impact_shaders");
            }
            @Override
            public void reload(net.minecraft.resource.ResourceManager manager) {}
        };
    }
}

// note! this will most likely look wrong because of DEBUG_FILLED_BOX used as a stand-in