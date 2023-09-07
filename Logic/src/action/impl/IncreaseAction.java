package action.impl;

import action.api.AbstractAction;
import action.api.ActionType;
import action.second.entity.SecondaryEntity;
import definition.entity.api.EntityDefinition;
import action.expression.api.Expression;
import action.expression.impl.ExpressionFactory;
import action.context.api.Context;
import definition.property.api.Range;
import instance.entity.api.EntityInstance;
import instance.property.api.PropertyInstance;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class IncreaseAction extends AbstractAction implements Serializable {
    private final String propertyName;
    private final String byExpression;

    public IncreaseAction(EntityDefinition entity, String propertyName, String byExpression) {
        super(entity, ActionType.INCREASE);
        this.propertyName = propertyName;
        this.byExpression = byExpression;
    }

    public IncreaseAction(EntityDefinition mainEntity, SecondaryEntity secondaryEntity, String propertyName, String byExpression) {
        super(mainEntity, secondaryEntity, ActionType.INCREASE);
        this.propertyName = propertyName;
        this.byExpression = byExpression;
    }

    @Override
    public void applyAction(Context context) {
        EntityInstance invokeOn = context.getEntityInstance();
        PropertyInstance propertyToUpdate = invokeOn.getPropertyByName(propertyName);
        Expression expression = new ExpressionFactory(byExpression, invokeOn);
        Object increaseBy = expression.getValue(context);

        if (propertyToUpdate.getPropertyDefinition().isNumeric()) {
            if (propertyToUpdate.getPropertyDefinition().isInteger()) {
                increaseInteger(propertyToUpdate, increaseBy);
            } else {
                increaseDouble(propertyToUpdate, increaseBy);
            }
        } else {
            throw new IllegalArgumentException("Increase action only available  on numeric type!");
        }
    }

    private void increaseInteger(PropertyInstance propertyToUpdate, Object increaseBy) {
        if (!(increaseBy instanceof Integer)) {
            throw new IllegalArgumentException("Increase on integer number can get only another integer.");
        }

        Integer propertyValue = (Integer) propertyToUpdate.getValue();
        Integer result = propertyValue + (Integer) increaseBy;

        checkRangeAndUpdateNumericValue(propertyToUpdate , result );
    }

    private void increaseDouble(PropertyInstance propertyToUpdate, Object increaseBy) {
        Double propertyValue = (Double) propertyToUpdate.getValue();
        if (increaseBy instanceof Number){
            double res  = ((Number)increaseBy).doubleValue();
            Double result = propertyValue + res;
            checkRangeAndUpdateNumericValue(propertyToUpdate , result );
        }else {
            throw new IllegalArgumentException("Increase can get only numeric values.");
        }
    }

    private void checkRangeAndUpdateNumericValue(PropertyInstance propertyToUpdate, Number result){
        Range range = propertyToUpdate.getPropertyDefinition().getRange();

        if (range != null) {
            double min = range.getMin();
            double max = range.getMax();

            double resultValue = result.doubleValue(); // Convert Number to double

            if (resultValue > min && resultValue < max) {
                propertyToUpdate.setValue(result);
            }
        } else {
            propertyToUpdate.setValue(result);
        }
    }

    @Override
    public Map<String, String> getArguments() {
        Map<String, String> arguments = new LinkedHashMap<>();

        arguments.put("property", propertyName);
        arguments.put("by", byExpression);

        return arguments;
    }
}

