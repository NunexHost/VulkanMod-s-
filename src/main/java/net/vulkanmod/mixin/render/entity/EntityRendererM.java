package net.vulkanmod.mixin.render.entity;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.vulkanmod.Initializer;
import net.vulkanmod.render.chunk.RenderSection;
import net.vulkanmod.render.chunk.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityRenderer.class)
public class EntityRendererM<T extends Entity> {

    private static final WorldRenderer worldRenderer = WorldRenderer.getInstance(); // Cache for efficiency

    @Redirect(method = "shouldRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/culling/Frustum;isVisible(Lnet/minecraft/world/phys/AABB;)Z"))
    private boolean isVisible(Frustum frustum, AABB aABB) {
        if (!Initializer.CONFIG.entityCulling) {
            return frustum.isVisible(aABB);
        }

        RenderSection section = worldRenderer.getSectionGrid().getSectionAtBlockPos((int) aABB.getCenter().x(), (int) aABB.getCenter().y(), (int) aABB.getCenter().z());

        return section != null && worldRenderer.getLastFrame() == section.getLastFrame();
    }

    // Additional potential optimizations (consider profiling for impact):

    // - If section access is frequent, create a local cache within this method.
    // - Optimize AABB calculations if they're found to be a bottleneck.
    // - Explore alternative culling techniques for specific scenarios.
}
