package net.vulkanmod.mixin.render.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.vulkanmod.interfaces.ExtendedVertexBuilder;
import net.vulkanmod.render.chunk.RenderSection;
import net.vulkanmod.render.chunk.WorldRenderer;
import net.vulkanmod.vulkan.util.ColorUtil;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SingleQuadParticle.class)
public abstract class SingleQuadParticleM extends Particle {

    @Shadow protected float quadSize;

    @Shadow protected abstract float getU0();
    @Shadow protected abstract float getU1();
    @Shadow protected abstract float getV0();
    @Shadow protected abstract float getV1();

    @Shadow public abstract float getQuadSize(float f);

    @Shadow @Final private ClientLevel level; // Cache for efficiency

    protected SingleQuadParticleM(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
        super(clientLevel, d, e, f, g, h, i);
        this.quadSize = 0.1F * (this.random.nextFloat() * 0.5F + 0.5F) * 2.0F;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void render(VertexConsumer vertexConsumer, Camera camera, float f) {
        float xOffset = (float)(Mth.lerp(f, this.xo, this.x));
        float yOffset = (float)(Mth.lerp(f, this.yo, this.y));
        float zOffset = (float)(Mth.lerp(f, this.zo, this.z));

        if (cull(xOffset, yOffset, zOffset)) {
            return;

        Vec3 vec3 = camera.getPosition();
        xOffset -= (float) vec3.x();
        yOffset -= (float) vec3.y();
        zOffset -= (float) vec3.z();

        Quaternionf quaternionf;
        if (this.roll != 0.0F) {
            quaternionf = new Quaternionf(camera.rotation());
            quaternionf.rotateZ(Mth.lerp(f, this.oRoll, this.roll));
        } else {
            quaternionf = camera.rotation();
        }

        Vector3f[] vector3fs = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
        float j = this.getQuadSize(f);

        for(int k = 0; k < 4; ++k) {
            Vector3f vector3f = vector3fs[k];
            vector3f.rotate(quaternionf);
            vector3f.mul(j);
            vector3f.add(xOffset, yOffset, zOffset);
        }

        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();
        int light = this.getLightColor(f);

        ExtendedVertexBuilder vertexBuilder = (ExtendedVertexBuilder)vertexConsumer;
        int packedColor = ColorUtil.packColorIntRGBA(this.rCol, this.gCol, this.bCol, this.alpha);

        vertexBuilder.vertex(vector3fs[0].x(), vector3fs[0].y(), vector3fs[0].z(), u1, v1, packedColor, light);
        vertexBuilder.vertex(vector3fs[1].x(), vector3fs[1].y(), vector3fs[1].z(), u1, v0, packedColor, light);
        vertexBuilder.vertex(vector3fs[2].x(), vector3fs[2].y(), vector3fs[2].z(), u0, v0, packedColor, light);
        vertexBuilder.vertex(vector3fs[3].x(), vector3fs[3].y(), vector3fs[3].z(), u0, v1, packedColor, light);
    }

    protected int getLightColor(float f) {
        BlockPos blockPos = BlockPos.containing(this.x, this.y, this.z);
        return this.level.hasChunkAt(blockPos) ? LevelRenderer.getLightColor(this.level, blockPos) : 0;
    }

    private boolean cull(float x, float y, float z) {
        return WorldRenderer.getInstance().getSectionGrid().getSectionAtBlockPos((int) x, (int) y, (int) z) != null &&
               WorldRenderer.getInstance().getSectionGrid().getSectionAtBlockPos((int) x, (int) y, (int) z).getLastFrame() != WorldRenderer.getInstance().getLastFrame();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return null;
    }
}
