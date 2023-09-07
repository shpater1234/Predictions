package instance.entity.manager.api;

import definition.entity.api.EntityDefinition;
import grid.api.SphereSpace;
import instance.entity.api.EntityInstance;

import java.util.Collection;
import java.util.List;

public interface EntityInstanceManager {
    void create(EntityDefinition entityDefinition, SphereSpace space);

    void killEntity(int idToKill);

    List<EntityInstance> getInstances();

    void moveAllEntitiesInSpace(SphereSpace space);
}
