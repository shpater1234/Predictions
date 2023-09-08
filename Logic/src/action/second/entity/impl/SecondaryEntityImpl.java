package action.second.entity.impl;

import action.context.api.Context;
import action.impl.condition.Condition;
import action.second.entity.SecondaryEntity;
import definition.entity.api.EntityDefinition;

public class SecondaryEntityImpl implements SecondaryEntity {
    EntityDefinition secondEntity;

    Condition condition;

    String count;

    @Override
    public EntityDefinition getSecondEntity() {
        return secondEntity;
    }

    @Override
    public String getInstancesCount() {
        return count;
    }

    @Override
    public Boolean evaluateCondition(Context context) {
        return condition.evaluate(context, instance);
    }

    @Override
    public boolean isConditionExist() {
        return condition != null;
    }
}
