package omtteam.openmodularturrets.tileentity.turrets;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import omtteam.omlib.util.RandomUtil;
import omtteam.omlib.util.WorldUtil;
import omtteam.openmodularturrets.entity.projectiles.TurretProjectile;
import omtteam.openmodularturrets.entity.projectiles.damagesources.ArmorBypassDamageSource;
import omtteam.openmodularturrets.entity.projectiles.damagesources.NormalDamageSource;
import omtteam.openmodularturrets.handler.config.OMTConfig;
import omtteam.openmodularturrets.util.OMTUtil;
import omtteam.openmodularturrets.util.TurretHeadUtil;

import java.util.List;
import java.util.Random;

public abstract class RayTracingTurret extends TurretHead {

    public RayTracingTurret(int tier) {
        super();
        this.turretTier = tier;
    }

    @Override
    protected float getProjectileGravity() {
        return 0.00F;
    }

    @Override
    public TurretProjectile createProjectile(World world, Entity target, ItemStack ammo) {
        return null;
    }

    @Override
    protected void doTargetedShot(Entity target, ItemStack ammo) {
        shootRay(target.posX, target.posY + target.getEyeHeight(), target.posZ, this.getTurretAccuracy(), target);
    }

    protected abstract void renderRay(Vec3d start, Vec3d end);

    protected abstract SoundEvent getHitSound();

    protected abstract float getDamageModifier(Entity entity);

    protected abstract float getNormalDamageFactor();

    protected abstract float getBypassDamageFactor();

    protected abstract void applyHitEffects(Entity entity);

    protected abstract void applyLaunchEffects();

    protected abstract void handleBlockHit(IBlockState hitBlock, BlockPos pos);

    protected void shootRay(double adjustedX, double adjustedY, double adjustedZ, double accuracy, Entity target) {
        // Consume energy
        base.setEnergyStored(base.getEnergyLevel(EnumFacing.DOWN) - getPowerRequiredForNextShot());
        this.applyLaunchEffects();

        // Create one projectile per scatter-shot upgrade
        for (int i = 0; i <= TurretHeadUtil.getScattershotUpgrades(base); i++) {
            double xDev, yDev, zDev;
            boolean hit = false;
            Vec3d vector = new Vec3d(adjustedX, adjustedY, adjustedZ);
            Vec3d baseVector = new Vec3d(this.getPos().getX() + 0.5D,
                                         this.getPos().getY() + 0.6D,
                                         this.getPos().getZ() + 0.5D);
            double deviationModifier = 1D * (target.height < 0.5 ? 1.5D : 1D)
                    * ((vector.distanceTo(baseVector) * 0.5D / (this.getTurretRange() + TurretHeadUtil.getRangeUpgrades(base, this))) + 0.3D);

            xDev = RandomUtil.random.nextGaussian() * 0.035D * accuracy * deviationModifier;
            yDev = RandomUtil.random.nextGaussian() * 0.035D * accuracy * deviationModifier;
            zDev = RandomUtil.random.nextGaussian() * 0.035D * accuracy * deviationModifier;

            vector = vector.addVector(xDev, yDev, zDev);
            baseVector = baseVector.add(vector.subtract(baseVector).normalize().scale(0.75D));

            // Play Sound
            this.getWorld().playSound(null, this.pos, this.getLaunchSoundEffect(), SoundCategory.BLOCKS,
                                      OMTConfig.TURRETS.turretSoundVolume, new Random().nextFloat() + 0.5F);

            RayTraceResult blockTraceResult = world.rayTraceBlocks(baseVector, vector, false, true, false);
            List<RayTraceResult> entityHits = WorldUtil.traceEntities(null, baseVector, vector, world);
            double blockRange = blockTraceResult != null ? blockTraceResult.hitVec.distanceTo(baseVector) : 500;
            for (RayTraceResult result : entityHits) {
                Entity entity = result.entityHit;
                if (baseVector.distanceTo(result.hitVec) <= blockRange) {
                    if (onHitEntity(entity)) {
                        this.renderRay(baseVector, vector);
                        hit = true;
                        break;
                    }
                }
            }
            if (!hit) {
                if (blockTraceResult != null && blockTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
                    handleBlockHit(world.getBlockState(blockTraceResult.getBlockPos()), blockTraceResult.getBlockPos());
                }
                this.renderRay(baseVector, vector.add(vector.subtract(baseVector).scale(2D)));
            }
        }
    }

    protected boolean onHitEntity(Entity entity) {
        if (entity != null && !entity.getEntityWorld().isRemote && !(entity instanceof TurretProjectile)) {
            if (entity instanceof EntityPlayer) {
                if (OMTUtil.canDamagePlayer((EntityPlayer) entity, base)) {
                    damageEntity(entity);
                    applyHitEffects(entity);
                    entity.hurtResistantTime = -1;
                    Random random = RandomUtil.random;
                    this.getWorld().playSound(null, entity.getPosition(), this.getHitSound(), SoundCategory.AMBIENT,
                                              OMTConfig.TURRETS.turretSoundVolume, random.nextFloat() + 0.5F);
                    return true;
                } else {
                    return false;
                }
            } else if (OMTUtil.canDamageEntity(entity, base)) {
                OMTUtil.setTagsForTurretHit(entity, base);
                damageEntity(entity);
                applyHitEffects(entity);
                entity.hurtResistantTime = -1;
                Random random = RandomUtil.random;
                this.getWorld().playSound(null, entity.getPosition(), this.getHitSound(), SoundCategory.AMBIENT,
                                          OMTConfig.TURRETS.turretSoundVolume, random.nextFloat() + 0.5F);
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    protected void damageEntity(Entity entity) {
        float damageModifier = this.getDamageModifier(entity); //0.8x to 1.8x damage multiplicator
        float damage = this.getTurretType().getSettings().getBaseDamage() * damageModifier;
        int fakeDrops = TurretHeadUtil.getFakeDropsLevel(base);

        if (this.getTurretDamageAmpBonus() * TurretHeadUtil.getAmpLevel(base) > 0) {
            if (entity instanceof EntityLivingBase) {
                EntityLivingBase elb = (EntityLivingBase) entity;
                damage += ((int) elb.getHealth() * this.getTurretDamageAmpBonus() * TurretHeadUtil.getAmpLevel(base));
            }
        }

        if (entity instanceof EntityLivingBase) {
            EntityLivingBase elb = (EntityLivingBase) entity;
            elb.attackEntityFrom(new NormalDamageSource(this.getTurretType().getInternalName(), fakeDrops, base,
                                                        (WorldServer) this.getWorld(), false), damage * this.getNormalDamageFactor());
            elb.attackEntityFrom(new ArmorBypassDamageSource(this.getTurretType().getInternalName(), fakeDrops, base,
                                                             (WorldServer) this.getWorld(), false), damage * this.getBypassDamageFactor());
        }
    }
}