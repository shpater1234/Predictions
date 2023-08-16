package definition.environment.impl;

import api.DTO;
import definition.environment.api.EnvironmentVariableManager;
import definition.property.api.PropertyDefinition;
import instance.enviornment.api.ActiveEnvironment;
import instance.enviornment.impl.ActiveEnvironmentImpl;
import instance.property.impl.PropertyInstanceImpl;

import java.util.*;

public class EnvironmentVariableManagerImpl implements EnvironmentVariableManager {
    private Map<String, PropertyDefinition> propNameToPropDefinition = new HashMap<>();

    @Override
    public void addEnvironmentVariable(PropertyDefinition property) {
        if (property == null) {
            throw new NullPointerException("Property can not be null!");
        }

        propNameToPropDefinition.put(property.getName(), property);
    }

    @Override
    public Collection<PropertyDefinition> getEnvironmentVariables() {
        return propNameToPropDefinition.values();
    }

    @Override
    public ActiveEnvironment createActiveEnvironment() {
        ActiveEnvironment environment = new ActiveEnvironmentImpl();

        for (PropertyDefinition property : propNameToPropDefinition.values()) {
            environment.addPropertyInstance(new PropertyInstanceImpl(property));
        }

        return environment;
    }

    //TODO: implement
    @Override
    public void mapEnvironmentVariableDTOtoEnvironmentVariableAndCreateIt(DTO environmentVariables) {

    }
}
