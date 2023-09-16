package execution.simulation.impl;

import action.api.Action;
import action.context.impl.ContextImpl;
import definition.entity.api.EntityDefinition;
import definition.entity.impl.EntityDefinitionImpl;
import definition.world.impl.WorldImpl;
import execution.simulation.api.Simulation;
import definition.world.api.World;
import execution.simulation.data.api.SimulationData;
import execution.simulation.data.impl.SimulationDataImpl;
import execution.simulation.termination.api.TerminateCondition;
import execution.simulation.termination.impl.TerminationImpl;
import grid.SphereSpaceImpl;
import grid.api.SphereSpace;
import impl.SimulationDTO;
import impl.SimulationDataDTO;
import impl.SimulationInitDataFromUserDTO;
import instance.entity.api.EntityInstance;
import instance.entity.manager.api.EntityInstanceManager;
import instance.entity.manager.impl.EntityInstanceManagerImpl;
import instance.enviornment.api.ActiveEnvironment;
import instance.enviornment.impl.ActiveEnvironmentImpl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SimulationImpl implements Simulation , Serializable {
    private final int serialNumber;
    private final Random random;

    private World world;
    private EntityInstanceManager entities;
    private ActiveEnvironment environmentVariables;
    private SimulationData data;
    private TerminateCondition endReason;
    private SphereSpace space;
    private SimulationInitDataFromUserDTO initData;

    private long startTime;
    private long pauseDuration;
    private int tick;
    private boolean isStop;
    private boolean isPause;

    public SimulationImpl(World world, SimulationInitDataFromUserDTO initData, int serialNumber) {
        this.world = world;
        this.initData = initData;
        this.serialNumber = serialNumber;
        this.random = new Random();
        this.entities = null;
        this.environmentVariables = null;
        this.data = null;
        this.startTime = 0;
        this.pauseDuration = 0;
        this.space = new SphereSpaceImpl(world.getGridRows(), world.getGridCols());
        this.tick = 0;
    }

    @Override
    public int getSerialNumber() {
        return serialNumber;
    }

    @Override
    public void run() {
        startTime = System.currentTimeMillis();

        initEntities();
        initEnvironmentVariables();
        tick = 1;

        while ((endReason = world.isActive(tick, startTime, pauseDuration, isStop)) == null) {
            entities.moveAllEntitiesInSpace(space);
            executeRules(tick);
            tick++;

            if (isPause) {
                this.pauseDuration += pauseDuringRunning();
            }
        }

        data = new SimulationDataImpl(serialNumber, startTime, world.getEntities(), entities);
        resetEnvironmentVariables();
    }

    @Override
    public TerminateCondition getEndReason() {
        return endReason;
    }

    @Override
    public long getRunStartTime() {
        return startTime;
    }

    @Override
    public ActiveEnvironment setEnvironmentVariables() {
        this.environmentVariables = world.createActiveEnvironment();

        return this.environmentVariables;
    }

    @Override
    public SimulationDataDTO getResultAsDTO(String entityName, String propertyName) {
        return new SimulationDataDTO(
                world.getEntities().size(),
                data.getStarterPopulationQuantity(entityName),
                data.getFinalPopulationQuantity(entityName),
                propertyName != null ?
                        data.getPropertyOfEntityPopulationSortedByValues(entityName, propertyName) :
                        null
        );
    }

    private void initEntities() {
        EntityInstanceManager instances = new EntityInstanceManagerImpl();

        for (EntityDefinition entityDefinition : world.getEntities()) {
            instances.createInstancesFromDefinition(entityDefinition, space);
        }

        entities = instances;
    }

    private synchronized void initEnvironmentVariables() {
        environmentVariables = new ActiveEnvironmentImpl(initData.getEnvironmentVariables());
    }

    private void resetEnvironmentVariables() {
        world.getEnvironmentVariables()
                .forEach(propertyDefinition -> propertyDefinition.setRandom(true));
    }

    private void executeRules(int tick) {
        List<Action> activeActions = createActionToInvokeList(tick);

        entities.getInstances().stream()
                .filter(EntityInstance::isAlive)
                .forEach(entity -> invokeActionsOnEntity(entity, activeActions));
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public SimulationDTO convertToDTO() {
        return new SimulationDTO(startTime, serialNumber, world.convertToDTO());
    }

    @Override
    public void pause() {
        isPause = true;
    }

    @Override
    public void stop() {
        isStop = true;
        resume();
    }

    @Override
    public void resume() {
        isPause = false;

        synchronized (this) {
            this.notifyAll();
        }
    }

    private long pauseDuringRunning() {
        long startTime = System.currentTimeMillis();

        synchronized (this) {
            while (isPause) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    stop();
                }
            }
        }

        return System.currentTimeMillis() - startTime;
    }

    private List<Action> createActionToInvokeList(int tick) {
        return world.getRules().stream()
                .filter(rule -> rule.isActive(tick, random.nextDouble()))
                .flatMap(rule -> rule.getActions().stream())
                .collect(Collectors.toList());
    }

    private void invokeActionsOnEntity(EntityInstance entity, List<Action> activeActions) {
        activeActions.stream()
                .filter(action -> action
                        .applyOn()
                        .getName()
                        .equals(entity.getName())
                )
                .forEach(action -> action.invoke(new ContextImpl(entity, entities, environmentVariables, tick)));
    }
}
