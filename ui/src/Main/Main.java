package Main;

import definition.entity.api.EntityDefinition;
import definition.entity.impl.EntityDefinitionImpl;
import definition.property.api.PropertyDefinition;
import definition.property.api.PropertyType;
import definition.property.api.Range;
import definition.property.impl.PropertyDefinitionImpl;
import instance.entity.api.EntityInstance;
import instance.entity.manager.api.EntityInstanceManager;
import instance.entity.manager.impl.EntityInstanceManagerImpl;
import instance.property.api.PropertyInstance;
import instance.property.impl.PropertyInstanceImpl;

public class Main {
    public static void main(String[] args) {
        PropertyDefinition p1 = new PropertyDefinitionImpl("name", PropertyType.STRING, false, "Avi");
        PropertyDefinition p2 = new PropertyDefinitionImpl("age", PropertyType.INT, true, new Range(10, 50));

        EntityDefinition student = new EntityDefinitionImpl("Student", 140);
        student.addProperty(p1);
        student.addProperty(p2);

        System.out.println(student);

        System.out.println("===============");

        PropertyInstance inst1 = new PropertyInstanceImpl(p1);
        PropertyInstance inst2 = new PropertyInstanceImpl(p2);

        EntityInstanceManager manager = new EntityInstanceManagerImpl();

        for (int i = 1; i <= 100; i++) {
            manager.create(student);
        }

        for (int i = 0; i < 100; i++) {
            System.out.println(manager.getInstances().get(i).getPropertyByName("name").getValue());
            System.out.println(manager.getInstances().get(i).getPropertyByName("age").getValue());
        }
    }
}
