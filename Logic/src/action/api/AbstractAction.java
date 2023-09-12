package action.api;

import action.context.api.Context;
import action.second.entity.SecondaryEntity;
import definition.entity.api.EntityDefinition;

import impl.ActionDTO;
import impl.EntityDefinitionDTO;
import instance.entity.api.EntityInstance;

import java.io.Serializable;
import java.util.List;

public abstract class AbstractAction implements Action, Serializable {
    private final EntityDefinition primaryEntity;
    private final SecondaryEntity secondaryEntity;
    private final ActionType type;
    private EntityInstance primaryEntityInstance;
    protected List<EntityInstance> secondaryEntitiesInstances;

    public AbstractAction(EntityDefinition primaryEntity, ActionType type) {
        this(primaryEntity, null, type);
    }

    public AbstractAction(EntityDefinition primaryEntity, SecondaryEntity secondaryEntity, ActionType type) {
        this.primaryEntity = primaryEntity;
        this.secondaryEntity = secondaryEntity;
        this.type = type;
    }

   /* @Override
    public EntityDefinition applyOn() {
        return primaryEntity;
    }*/

    @Override
    public ActionType getType() {
        return type;
    }

    @Override
    public String getName() {
        return type.name();
    }

    @Override
    public ActionDTO convertToDTO() {
        EntityDefinitionDTO secondaryDTO = secondaryEntity != null ?
                secondaryEntity.getSecondaryEntity().convertToDTO() :
                null;

        return new ActionDTO(
                type.name(),
                primaryEntity.convertToDTO(),
                secondaryDTO,
                getArguments(),
                getAdditionalInformation()
        );
    }

    @Override
    public void invoke(Context context) {
        if (context.getEntityInstance().getName().equals(primaryEntity.getName())) {
            setEntitiesInstances(context);
            apply(context);
        }
    }

    private void setEntitiesInstances(Context context) {
        if (isSecondaryEntityExist()) {
            primaryEntityInstance = context.getEntityInstance();
            secondaryEntitiesInstances = context.getSecondEntityFilteredList(secondaryEntity);
        }
    }

    protected abstract void apply(Context context);

    protected Boolean isSecondaryEntityExist() {
        return secondaryEntity != null;
    }
}
