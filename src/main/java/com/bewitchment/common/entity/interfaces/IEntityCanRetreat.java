package com.bewitchment.common.entity.interfaces;

public interface IEntityCanRetreat {
    /**
     * @return under which circumstances should the entity disengage
     */
    boolean shouldRetreat();
    boolean shouldRetreatHigh();
}
