package net.esweld.witheredflame.particle;

import net.esweld.witheredflame.WitheredFlame;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, WitheredFlame.MOD_ID);

    public static final RegistryObject<SimpleParticleType> WITHERED_EMBER =
            PARTICLES.register("withered_ember", () -> new SimpleParticleType(false));
}