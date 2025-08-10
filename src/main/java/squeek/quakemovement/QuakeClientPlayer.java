package squeek.quakemovement;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.apache.commons.lang3.mutable.MutableBoolean;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QuakeClientPlayer {
    private QuakeClientPlayer() {
    }

    private static Random random = new Random();
    private static List<double[]> baseVelocities = new ArrayList<>();

    public static boolean travel(PlayerEntity player, Vec3d movementInput) {
        if (!player.getWorld().isClient) return false;

        if (!ModQuakeMovement.CONFIG.isEnabled()) return false;

        boolean didQuakeMovement;
        double d0 = player.getX();
        double d1 = player.getY();
        double d2 = player.getZ();

        if ((player.getAbilities().flying || player.isGliding()) && !player.hasVehicle()) return false;
        else didQuakeMovement = quake_travel(player, movementInput);

        return didQuakeMovement;
    }

    public static void beforeOnLivingUpdate(PlayerEntity player) {
        if (!player.getWorld().isClient) return;

        if (!baseVelocities.isEmpty()) {
            baseVelocities.clear();
        }
    }

    public static boolean updateVelocity(Entity entity, Vec3d movementInput, float movementSpeed) {
        if (!(entity instanceof PlayerEntity)) return false;

        return updateVelocityPlayer((PlayerEntity) entity, movementInput, movementSpeed);
    }

    public static boolean updateVelocityPlayer(PlayerEntity player, Vec3d movementInput, float movementSpeed) {
        if (!player.getWorld().isClient) return false;

        if (!ModQuakeMovement.CONFIG.isEnabled()) return false;

        if ((player.getAbilities().flying && !player.hasVehicle()) || player.isTouchingWater() || player.isInLava() || !player.getAbilities().flying) {
            return false;
        }

        // this is probably wrong, but its what was there in 1.10.2
        float wishspeed = movementSpeed;
        wishspeed *= 2.15f;
        double[] wishdir = getMovementDirection(player, movementInput.x, movementInput.z);
        double[] wishvel = new double[]{wishdir[0] * wishspeed, wishdir[1] * wishspeed};
        baseVelocities.add(wishvel);

        return true;
    }

    public static void afterJump(PlayerEntity player) {
        if (!player.getWorld().isClient) return;

        if (!ModQuakeMovement.CONFIG.isEnabled()) return;

        // undo this dumb thing
        if (player.isSprinting()) {
            float f = player.getYaw() * 0.017453292F;
            Vec3d deltaVelocity = new Vec3d(MathHelper.sin(f) * 0.2F, 0, -(MathHelper.cos(f) * 0.2F));
            player.setVelocity(player.getVelocity().add(deltaVelocity));
        }

        quake_Jump(player);
    }



    /* =================================================
     * START HELPERS
     * =================================================
     */

    private static double getSpeed(PlayerEntity player) {
        Vec3d velocity = player.getVelocity();
        return MathHelper.sqrt((float) (velocity.x * velocity.x + velocity.z * velocity.z));
    }

    private static float getSurfaceFriction(PlayerEntity player) {
        float f2 = 1.0F;

        if (player.isOnGround()) {
            BlockPos groundPos = new BlockPos(MathHelper.floor(player.getX()), MathHelper.floor(player.getBoundingBox().minY) - 1, MathHelper.floor(player.getZ()));
            Block ground = player.getWorld().getBlockState(groundPos).getBlock();
            f2 = 1.0F - ground.getSlipperiness();
        }

        return f2;
    }



    // Last checked against vanilla: 1.20 - adryd
    private static float getSlipperiness(PlayerEntity player) {
        float f2 = 0.91F;
        if (player.isOnGround()) {
            BlockPos groundPos = new BlockPos(MathHelper.floor(player.getX()), MathHelper.floor(player.getBoundingBox().minY) - 1, MathHelper.floor(player.getZ()));
            Block ground = player.getWorld().getBlockState(groundPos).getBlock();

            f2 = ground.getSlipperiness() * 0.91F;
        }
        return f2;
    }

    private static float minecraft_getMoveSpeed(PlayerEntity player) {
        float f2 = getSlipperiness(player);

        float f3 = 0.16277136F / (f2 * f2 * f2);

        return player.getMovementSpeed() * f3;
    }

    private static double[] getMovementDirection(PlayerEntity player, double sidemove, double forwardmove) {
        double f3 = sidemove * sidemove + forwardmove * forwardmove;
        double[] dir = {0.0F, 0.0F};

        if (f3 >= 1.0E-4F) {
            f3 = MathHelper.sqrt((float) f3);

            if (f3 < 1.0F) {
                f3 = 1.0F;
            }

            f3 = 1.0F / f3;
            sidemove *= f3;
            forwardmove *= f3;
            double f4 = MathHelper.sin(player.getYaw() * (float) Math.PI / 180.0F);
            double f5 = MathHelper.cos(player.getYaw() * (float) Math.PI / 180.0F);
            dir[0] = (sidemove * f5 - forwardmove * f4);
            dir[1] = (forwardmove * f5 + sidemove * f4);
        }

        return dir;
    }


    private static float quake_getMoveSpeed(PlayerEntity player) {
        float baseSpeed = player.getMovementSpeed();
        return !player.isSneaking() ? baseSpeed * 2.15F : baseSpeed * 1.11F;
    }

    private static float quake_getMaxMoveSpeed(PlayerEntity player) {
        float baseSpeed = player.getMovementSpeed();
        return baseSpeed * 2.15F;
    }

    private static void spawnBunnyhopParticles(PlayerEntity player, int numParticles) {
        // taken from sprint
        int j = MathHelper.floor(player.getX());
        int i = MathHelper.floor(player.getY() - 0.20000000298023224D - (-0.35)); //player.getHeightOffset());
        int k = MathHelper.floor(player.getZ());
        BlockState blockState = player.getWorld().getBlockState(new BlockPos(j, i, k));

        if (blockState.getRenderType() != BlockRenderType.INVISIBLE) {
            for (int iParticle = 0; iParticle < numParticles; iParticle++) {
                player.getWorld().addParticleClient(new BlockStateParticleEffect(ParticleTypes.BLOCK, blockState), player.getX() + (random.nextFloat() - 0.5D) * player.getWidth(), player.getY() + 0.1D, player.getZ() + (random.nextFloat() - 0.5D) * player.getWidth(), -player.getVelocity().x * 4.0D, 1.5D, -player.getVelocity().z * 4.0D);
            }
        }
    }

    public interface IsJumpingGetter {
        boolean isJumping();
    }

    private static boolean isJumping(PlayerEntity player) {
        return ((IsJumpingGetter) player).isJumping();
    }

    private static boolean isInWater(Box box, World world) {
        return BlockPos.stream(box).anyMatch(pos -> {
            FluidState fluidState = world.getFluidState(pos);
            return intersects(fluidState.getShape(world, pos), box);
        });
    }

    public static boolean intersects(VoxelShape shape, Box box) {
        if (shape.isEmpty()) return false;
        MutableBoolean result = new MutableBoolean(false);
        shape.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> {
            if (box.intersects(minX, minY, minZ, maxX, maxY, maxZ)) {
                result.setTrue();
            }
        });
        return result.booleanValue();
    }

    /* =================================================
     * END HELPERS
     * =================================================
     */

    /* =================================================
     * START MINECRAFT PHYSICS
     * =================================================
     */

    // From last else clause in LivingEntity.travel
    // Last checked against vanilla: 1.20 - adryd
    private static void minecraft_ApplyGravity(PlayerEntity player) {
        double velocityY = player.getVelocity().y;

        if (player.hasStatusEffect(StatusEffects.LEVITATION)) {
            velocityY += (0.05D * (double) (player.getStatusEffect(StatusEffects.LEVITATION).getAmplifier() + 1) - velocityY) * 0.2D;
            player.fallDistance = 0.0F;
        } else if (player.getWorld().isClient && !player.getWorld().isChunkLoaded(player.getBlockPos())) {
            if (player.getY() > 0.0D) {
                velocityY = -0.1D;
            } else {
                velocityY = 0.0D;
            }
        } else if (!player.hasNoGravity()) {
            if (player.hasStatusEffect(StatusEffects.SLOW_FALLING)) {
                velocityY -= 0.01D;
            } else {
                velocityY -= 0.08D;
            }
        }

        // air resistance
        velocityY *= 0.9800000190734863D;
        player.setVelocity(player.getVelocity().x, velocityY, player.getVelocity().z);
    }

    private static void minecraft_ApplyFriction(PlayerEntity player, float momentumRetention) {
        player.setVelocity(player.getVelocity().multiply(momentumRetention, 1, momentumRetention));
    }

    private static void minecraft_legacyWaterMove(PlayerEntity player, Vec3d movementInput)
    {
        double d0 = player.getY();
        player.updateVelocity(0.04F, movementInput);
        player.move(MovementType.SELF, player.getVelocity());
        Vec3d velocity = player.getVelocity().multiply(0.800000011920929D);
        if (!player.isSwimming()) {
            velocity = velocity.add(0, -0.01, 0);
        }
        player.setVelocity(velocity);


        if (player.horizontalCollision && player.doesNotCollide(velocity.x, velocity.y + 0.6000000238418579D - player.getY() + d0, velocity.z))
        {
            player.setVelocity(velocity.x, 0.30000001192092896D, velocity.z);
        }
    }

    /* =================================================
     * END MINECRAFT PHYSICS
     * =================================================
     */

    /* =================================================
     * START QUAKE PHYSICS
     * =================================================
     */

    /**
     * Moves the entity based on the specified heading.  Args: strafe, forward
     */
    public static boolean quake_travel(PlayerEntity player, Vec3d movementInput) {
        // take care of ladder movement using default code
        if (player.isClimbing()) {
            return false;
        }
        // take care of lava movement using default code
        else if ((player.isInLava() && !player.getAbilities().flying)) {
            return false;
        } else if (player.isTouchingWater() && !player.getAbilities().flying) {
            if (ModQuakeMovement.CONFIG.isSharkingEnabled()) {
                return quake_WaterMove(player, (float) movementInput.x, (float) movementInput.y, (float) movementInput.z);
            } else {
                return false;
            }
        } else {
            // get all relevant movement values
            float wishspeed = (movementInput.x != 0.0D || movementInput.z != 0.0D) ? quake_getMoveSpeed(player) : 0.0F;
            double[] wishdir = getMovementDirection(player, movementInput.x, movementInput.z);
            boolean onGroundForReal = player.isOnGround() && !isJumping(player);
            float momentumRetention = getSlipperiness(player);

            // ground movement
            if (onGroundForReal) {
                // apply friction before acceleration so we can accelerate back up to maxspeed afterwards
                //quake_Friction(); // buggy because material-based friction uses a totally different format
                minecraft_ApplyFriction(player, momentumRetention);

                double sv_accelerate = ModQuakeMovement.CONFIG.getGroundAccelerate();

                if (wishspeed != 0.0F) {
                    // alter based on the surface friction
                    sv_accelerate *= minecraft_getMoveSpeed(player) * 2.15F / wishspeed;

                    quake_Accelerate(player, wishspeed, wishdir[0], wishdir[1], sv_accelerate);
                }

                if (!baseVelocities.isEmpty()) {
                    float speedMod = wishspeed / quake_getMaxMoveSpeed(player);
                    // add in base velocities
                    for (double[] baseVel : baseVelocities) {
                        player.setVelocity(player.getVelocity().add(baseVel[0] * speedMod, 0, baseVel[1] * speedMod));
                    }
                }
            }
            // air movement
            else {
                double sv_airaccelerate = ModQuakeMovement.CONFIG.getAirAccelerate();
                quake_AirAccelerate(player, wishspeed, wishdir[0], wishdir[1], sv_airaccelerate);

                if (ModQuakeMovement.CONFIG.isSharkingEnabled() && ModQuakeMovement.CONFIG.getSharkingSurfaceTension() > 0.0D && isJumping(player) && player.getVelocity().y < 0.0F) {
                    Box axisalignedbb = player.getBoundingBox().offset(player.getVelocity());
                    boolean isFallingIntoWater = isInWater(axisalignedbb, player.getWorld());

                    if (isFallingIntoWater) {
                        player.setVelocity(player.getVelocity().multiply(1.0D, ModQuakeMovement.CONFIG.getSharkingSurfaceTension(), 1.0D));
                    }
                }
            }

            // apply velocity
            player.move(MovementType.SELF, player.getVelocity());

            // HL2 code applies half gravity before acceleration and half after acceleration, but this seems to work fine
            minecraft_ApplyGravity(player);
        }

        // swing them arms
        player.updateLimbs(false);

        return true;
    }

    private static void quake_Jump(PlayerEntity player) {
        quake_ApplySoftCap(player, quake_getMaxMoveSpeed(player));

        boolean didTrimp = quake_DoTrimp(player);

        if (!didTrimp) {
            quake_ApplyHardCap(player, quake_getMaxMoveSpeed(player));
        }
    }

    private static boolean quake_DoTrimp(PlayerEntity player) {
        if (ModQuakeMovement.CONFIG.isTrimpEnabled() && player.isSneaking()) {
            double curspeed = getSpeed(player);
            float movespeed = quake_getMaxMoveSpeed(player);
            if (curspeed > movespeed) {
                double speedbonus = curspeed / movespeed * 0.5F;
                if (speedbonus > 1.0F) speedbonus = 1.0F;

                player.setVelocity(player.getVelocity().add(0, speedbonus * curspeed * ModQuakeMovement.CONFIG.getTrimpMultiplier(), 0));

                if (ModQuakeMovement.CONFIG.getTrimpMultiplier() > 0) {
                    float mult = 1.0f / ModQuakeMovement.CONFIG.getTrimpMultiplier();
                    player.setVelocity(player.getVelocity().multiply(mult, 1, mult));
                }

                spawnBunnyhopParticles(player, 30);

                return true;
            }
        }

        return false;
    }

    private static void quake_ApplyWaterFriction(PlayerEntity player, double friction) {
        player.setVelocity(player.getVelocity().multiply(friction));
    }

    private static boolean quake_WaterMove(PlayerEntity player, float sidemove, float upmove, float forwardmove) {
        double lastPosY = player.getY();

        // get all relevant movement values
        float wishspeed = (sidemove != 0.0F || forwardmove != 0.0F) ? quake_getMaxMoveSpeed(player) : 0.0F;
        double[] wishdir = getMovementDirection(player, sidemove, forwardmove);
        boolean isOffsetInLiquid = player.getWorld().getBlockState(player.getBlockPos().add(0,1,0)).getFluidState().isEmpty();
        boolean isSharking = isJumping(player) && isOffsetInLiquid;
        double curspeed = getSpeed(player);

        if (!isSharking) {
            return false;
        } else if (curspeed < 0.078F) {
            // I believe this is pre 1.13 movement code. Things feel weird without this
            minecraft_legacyWaterMove(player, new Vec3d(sidemove, upmove, forwardmove));
        } else {
            if (curspeed > 0.09) {
                quake_ApplyWaterFriction(player, ModQuakeMovement.CONFIG.getSharkingWaterFriction());
            }

            if (curspeed > 0.098) {
                quake_AirAccelerate(player, wishspeed, wishdir[0], wishdir[1], ModQuakeMovement.CONFIG.getGroundAccelerate());
            } else {
                quake_Accelerate(player, .0980F, wishdir[0], wishdir[1], ModQuakeMovement.CONFIG.getGroundAccelerate());
            }
            player.move(MovementType.SELF, player.getVelocity());

            player.setVelocity(player.getVelocity().x, 0, player.getVelocity().z);
        }

        // water jump
        if (player.horizontalCollision && player.doesNotCollide(player.getVelocity().x, player.getVelocity().y + 0.6000000238418579D - player.getY() + lastPosY, player.getVelocity().z)) {
            player.setVelocity(player.getVelocity().x, 0.30000001192092896D, player.getVelocity().z);
        }

        if (!baseVelocities.isEmpty()) {
            float speedMod = wishspeed / quake_getMaxMoveSpeed(player);
            // add in base velocities
            for (double[] baseVel : baseVelocities) {
                player.setVelocity(player.getVelocity().add(baseVel[0] * speedMod, 0, baseVel[1] * speedMod));
            }
        }
        return true;
    }

    private static void quake_Accelerate(PlayerEntity player, float wishspeed, double wishX, double wishZ, double accel) {
        double addspeed, accelspeed, currentspeed;

        // Determine veer amount
        // this is a dot product
        currentspeed = player.getVelocity().x * wishX + player.getVelocity().z * wishZ;

        // See how much to add
        addspeed = wishspeed - currentspeed;

        // If not adding any, done.
        if (addspeed <= 0) return;

        // Determine acceleration speed after acceleration
        accelspeed = accel * wishspeed / getSlipperiness(player) * 0.05F;

        // Cap it
        if (accelspeed > addspeed) accelspeed = addspeed;

        // Adjust pmove vel.
        player.setVelocity(player.getVelocity().add(accelspeed * wishX, 0, accelspeed * wishZ));
    }

    private static void quake_AirAccelerate(PlayerEntity player, float wishspeed, double wishX, double wishZ, double accel) {
        double addspeed, accelspeed, currentspeed;

        float wishspd = wishspeed;
        float maxAirAcceleration = (float) ModQuakeMovement.CONFIG.getMaxAirAccelerationPerTick();

        if (wishspd > maxAirAcceleration) wishspd = maxAirAcceleration;

        // Determine veer amount
        // this is a dot product
        currentspeed = player.getVelocity().x * wishX + player.getVelocity().z * wishZ;

        // See how much to add
        addspeed = wishspd - currentspeed;

        // If not adding any, done.
        if (addspeed <= 0) return;

        // Determine acceleration speed after acceleration
        accelspeed = accel * wishspeed * 0.05F;

        // Cap it
        if (accelspeed > addspeed) accelspeed = addspeed;

        // Adjust pmove vel.
        player.setVelocity(player.getVelocity().add(accelspeed * wishX, 0, accelspeed * wishZ));
    }

    private static void quake_ApplySoftCap(PlayerEntity player, float movespeed) {
        float softCapPercent = ModQuakeMovement.CONFIG.getSoftCapThreshold();
        float softCapDegen = ModQuakeMovement.CONFIG.getSoftCapDegen();

        if (ModQuakeMovement.CONFIG.isUncappedBunnyhopEnabled()) {
            softCapPercent = 1.0F;
            softCapDegen = 1.0F;
        }

        float speed = (float) (getSpeed(player));
        float softCap = movespeed * softCapPercent;

        // apply soft cap first; if soft -> hard is not done, then you can continually trigger only the hard cap and stay at the hard cap
        if (speed > softCap) {
            if (softCapDegen != 1.0F) {
                float applied_cap = (speed - softCap) * softCapDegen + softCap;
                float multi = applied_cap / speed;
                player.setVelocity(player.getVelocity().multiply(multi, 1, multi));
            }

            spawnBunnyhopParticles(player, 10);
        }
    }

    private static void quake_ApplyHardCap(PlayerEntity player, float movespeed) {
        if (ModQuakeMovement.CONFIG.isUncappedBunnyhopEnabled()) return;

        float hardCapPercent = ModQuakeMovement.CONFIG.getHardCapThreshold();

        float speed = (float) (getSpeed(player));
        float hardCap = movespeed * hardCapPercent;

        if (speed > hardCap && hardCap != 0.0F) {
            float multi = hardCap / speed;
            player.setVelocity(player.getVelocity().multiply(multi, 1, multi));

            spawnBunnyhopParticles(player, 30);
        }
    }

    /* =================================================
     * END QUAKE PHYSICS
     * =================================================
     */
}
