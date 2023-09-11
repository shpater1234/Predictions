package execution.simulation.api;

import api.DTOConvertible;
import definition.world.api.World;
import execution.simulation.data.api.SimulationData;
import execution.simulation.termination.api.TerminateCondition;
import impl.SimulationDTO;
import impl.SimulationDataDTO;
import instance.enviornment.api.ActiveEnvironment;

public interface Simulation extends DTOConvertible<SimulationDTO> {
    int getSerialNumber();
    long getRunStartTime();
    ActiveEnvironment setEnvironmentVariables();

    void run();

    TerminateCondition getEndReason();
    SimulationDataDTO getResultAsDTO(String entityName, String propertyName);
    World getWorld();
}
