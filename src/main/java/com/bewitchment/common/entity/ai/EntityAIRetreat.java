package com.bewitchment.common.entity.ai;

import com.bewitchment.common.entity.interfaces.IEntityCanRetreat;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class EntityAIRetreat<T extends EntityLivingBase> extends EntityAIBase {
    private final Predicate<Entity> canBeSeenSelector;
    /**
     * The entity we are attached to
     */
    protected EntityCreature theEntity;
    protected T closestLivingEntity;
    private double farSpeed;
    private double nearSpeed;
    private float avoidDistance;
    /**
     * The PathEntity of our entity
     */
    private Path entityPathEntity;
    /**
     * The PathNavigate of our entity
     */
    private PathNavigate entityPathNavigate;
    private Class<T> classToAvoid;
    private Predicate<? super T> avoidTargetSelector;

    public EntityAIRetreat(EntityCreature theEntityIn, Class<T> classToAvoidIn, float avoidDistanceIn, double farSpeedIn, double nearSpeedIn) {
        this(theEntityIn, classToAvoidIn, t -> true, avoidDistanceIn, farSpeedIn, nearSpeedIn);
    }

    public EntityAIRetreat(EntityCreature theEntityIn, Class<T> classToAvoidIn, Predicate<? super T> avoidTargetSelectorIn, float avoidDistanceIn, double farSpeedIn, double nearSpeedIn) {
        this.canBeSeenSelector = p_apply_1_ -> p_apply_1_.isEntityAlive() && EntityAIRetreat.this.theEntity.getEntitySenses().canSee(p_apply_1_);
        this.theEntity = theEntityIn;
        this.classToAvoid = classToAvoidIn;
        this.avoidTargetSelector = avoidTargetSelectorIn;
        this.avoidDistance = avoidDistanceIn;
        this.farSpeed = farSpeedIn;
        this.nearSpeed = nearSpeedIn;
        this.entityPathNavigate = theEntityIn.getNavigator();
        this.setMutexBits(3);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean shouldExecute() {
        if (!((IEntityCanRetreat) this.theEntity).shouldRetreat()) {
            return false;
        }

        // Enemies in the vincinity
        List<T> list = this.theEntity.getEntityWorld().getEntitiesWithinAABB(this.classToAvoid, this.theEntity.getEntityBoundingBox().expand((double) this.avoidDistance, 5.0D, (double) this.avoidDistance));

        if (list.isEmpty()) {
            return true;
        } else {
            this.closestLivingEntity = list.get(0);
            int attempts = 0;
            Vec3d escapePoint;
            Vec3d enemyPos = new Vec3d(this.closestLivingEntity.posX, this.closestLivingEntity.posY, this.closestLivingEntity.posZ);
            do {
                if(attempts > 20)
                    return false;
                attempts++;
                escapePoint = RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.theEntity, 45, 3, enemyPos);
            }while(escapePoint == null || escapePoint.distanceTo(enemyPos) < 10 || (this.theEntity.getEntityWorld().getBlockState(new BlockPos(escapePoint.x, escapePoint.y, escapePoint.z)).getBlock() != Blocks.AIR));
            this.entityPathEntity = this.entityPathNavigate.getPathToXYZ(escapePoint.x, escapePoint.y, escapePoint.z);
            return this.entityPathEntity != null;

        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    @Override
    public boolean shouldContinueExecuting() {
        return (!((IEntityCanRetreat) this.theEntity).shouldRetreatHigh());
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    @Override
    public void startExecuting() {
        if (this.entityPathEntity != null) {
            this.entityPathNavigate.setPath(this.entityPathEntity, this.farSpeed);
        }
    }

    /**
     * Resets the task
     */
    @Override
    public void resetTask() {
        this.closestLivingEntity = null;
    }

    /**
     * Updates the task
     */
    @Override
    public void updateTask() {
        if (this.closestLivingEntity != null) {
            if (this.theEntity.getDistanceSq(this.closestLivingEntity) < 15.0D) {
                this.theEntity.getNavigator().setSpeed(this.nearSpeed);
            } else {
                this.theEntity.getNavigator().setSpeed(this.farSpeed);
            }
        }
    }
}