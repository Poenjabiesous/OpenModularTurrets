package omtteam.openmodularturrets.entity.projectiles;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import omtteam.openmodularturrets.blocks.turretheads.BlockAbstractTurretHead;
import omtteam.openmodularturrets.entity.projectiles.damagesources.NormalDamageSource;
import omtteam.openmodularturrets.handler.ConfigHandler;
import omtteam.openmodularturrets.init.ModSounds;
import omtteam.openmodularturrets.tileentity.TurretBase;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;

public class BulletProjectile extends TurretProjectile {
    @SuppressWarnings("unused")
    public BulletProjectile(World p_i1776_1_) {
        super(p_i1776_1_);
        this.gravity = 0.00F;
    }

    public BulletProjectile(World par1World, ItemStack ammo, TurretBase turretBase) {
        super(par1World, ammo, turretBase);
        this.gravity = 0.00F;
    }

    @Override
    public void onEntityUpdate() {
        if (ticksExisted >= 50) {
            this.setDead();
        }
    }

    @Override
    @ParametersAreNonnullByDefault
    protected void onImpact(RayTraceResult movingobjectposition) {
        if (this.ticksExisted <= 1) {
            return;
        }
        if (movingobjectposition.typeOfHit == RayTraceResult.Type.BLOCK) {
            IBlockState hitBlock = getEntityWorld().getBlockState(movingobjectposition.getBlockPos());

            if (hitBlock.getBlock() instanceof BlockAbstractTurretHead) {
                return;
            }

            if (!hitBlock.getMaterial().isSolid()) {
                // Go through non solid block
                return;
            }
        }

        if (movingobjectposition.entityHit != null && !getEntityWorld().isRemote) {
            if (movingobjectposition.typeOfHit.equals(RayTraceResult.Type.MISS)) {
                if (getEntityWorld().isAirBlock(movingobjectposition.getBlockPos())) {
                    return;
                }
            }

            int damage = ConfigHandler.getGunTurretSettings().getDamage();

            if (isAmped) {
                if (movingobjectposition.entityHit instanceof EntityLivingBase) {
                    EntityLivingBase elb = (EntityLivingBase) movingobjectposition.entityHit;
                    damage += ((int) elb.getHealth() * (0.06F * amp_level));
                }
            }

            if (movingobjectposition.entityHit instanceof EntityPlayer) {
                if (canDamagePlayer((EntityPlayer) movingobjectposition.entityHit)) {
                    movingobjectposition.entityHit.attackEntityFrom(new NormalDamageSource("bullet"), damage);
                    movingobjectposition.entityHit.hurtResistantTime = 0;
                }
            } else {
                movingobjectposition.entityHit.attackEntityFrom(new NormalDamageSource("bullet"), damage);
                movingobjectposition.entityHit.hurtResistantTime = 0;
            }
            setMobDropLoot(movingobjectposition.entityHit);
        }

        if (movingobjectposition.entityHit == null && !getEntityWorld().isRemote) {
            Random random = new Random();
            getEntityWorld().playSound(null, new BlockPos(posX, posY, posZ), ModSounds.bulletHitSound, SoundCategory.AMBIENT,
                    ConfigHandler.getTurretSoundVolume(), random.nextFloat() + 0.5F);
        }
        this.setDead();
    }

    @Override
    @ParametersAreNonnullByDefault
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
        this.posY = posY + 12F;
    }

    @Override
    protected float getGravityVelocity() {
        return this.gravity;
    }
}