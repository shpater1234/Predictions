package javafx.tab.results;

import execution.simulation.api.PredictionsLogic;
import impl.SimulationDTO;
import impl.SimulationDataDTO;
import impl.WorldDTO;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.mainScene.main.PredictionsMainAppController;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.tab.results.analyze.AnalyzePaginationController;
import javafx.tab.results.helper.Category;
import javafx.tab.results.progress.ProgressController;
import task.helper.EntityPopulationData;

import java.util.LinkedList;
import java.util.Queue;

public class ResultsController {

    @FXML
    private StackPane progress;

    @FXML
    private ListView<Category> simulationsListView;

    @FXML
    private TableView<EntityPopulationData> entitiesPopulationTableView;

    @FXML
    private TableColumn<EntityPopulationData, String> entitiesCol;

    @FXML
    private TableColumn<EntityPopulationData, Integer> populationCol;

    @FXML
    private ChoiceBox<String> entityChoiceBox;

    @FXML
    private ChoiceBox<String> propertyChoiceBox;

    @FXML
    private TitledPane entityAmountTitlePane;

    @FXML
    private TitledPane dataAnalyzeTitlePane;

    @FXML
    private Pagination analyzePagination;

    @FXML
    private AnalyzePaginationController analyzePaginationController;

    @FXML
    private ProgressController progressController;

    private TabPane tabPane; //todo: why do we need it?

    private PredictionsLogic engine;
    private PredictionsMainAppController predictionsMainAppController;

    private ObjectProperty<Category> selectedSimulation;
    private StringProperty propertyToView;
    private StringProperty entityToView;
    private BooleanProperty isSelectedSimulationEnded;
    private BooleanProperty isNewFileLoaded;

    private ObservableList<EntityPopulationData> entityPopulationList = FXCollections.observableArrayList();

    private Queue<Category> waitingQueue = new LinkedList<>();

    public ResultsController() {
        selectedSimulation = new SimpleObjectProperty<>();
        propertyToView = new SimpleStringProperty();
        entityToView = new SimpleStringProperty();
        isSelectedSimulationEnded = new SimpleBooleanProperty();
        isNewFileLoaded = new SimpleBooleanProperty();
    }

    @FXML
    private void initialize() {
        propertyToView.bind(propertyChoiceBox.valueProperty());
        entityToView.bind(entityChoiceBox.valueProperty());
        selectedSimulation.bind(simulationsListView.getSelectionModel().selectedItemProperty());
        isSelectedSimulationEnded.bind(progressController.isStopProperty());
        progress.disableProperty().bind(Bindings.isNull(selectedSimulation));
        dataAnalyzeTitlePane.disableProperty().bind(isSelectedSimulationEnded.not());

        selectedSimulation.addListener((observable, oldValue, newValue) -> progressController.onSelectedSimulationChange(newValue));
        isSelectedSimulationEnded.addListener((observable, oldValue, newValue) -> onSelectedSimulationStop());

        entityToView.addListener((observable, oldValue, newValue) -> onSelectedEntity());

        entitiesCol.setCellValueFactory(new PropertyValueFactory<>("entityName"));
        populationCol.setCellValueFactory(new PropertyValueFactory<>("population"));

        analyzePaginationController.setResultsController(this);
    }

    public SimulationDataDTO getSimulationData() {
        return selectedSimulation.isNotNull().get() ?
                engine.getSimulationData(
                        selectedSimulation.get().getId(),
                        entityToView.get(),
                        propertyChoiceBox.getValue()
                ) :
                null;
    }

    private void onSelectedEntity() {
        setPropertyChoiceBox();
        analyzePaginationController.setConsistency(engine.getConsistencyByEntityName(selectedSimulation.get().getId(), entityToView.get()));
    }

    public void onStartButtonClicked(int newSimulationSerialNumber) {
        progressController.setTableView(this.entitiesPopulationTableView);
        System.out.println(progressController);
        SimulationDTO lastSimulation = engine.getSimulationDTOBySerialNumber(newSimulationSerialNumber);

        Category simulationInfo = new Category(lastSimulation.getStartDate(), lastSimulation.getSerialNumber());
        simulationsListView.getItems().add(simulationInfo);
        waitingQueue.add(simulationInfo);
    }

    public void onNewFileLoaded() {
//        histogramBarChart.getData().clear();
//        simulationChoiceBox.getItems().clear();
        simulationsListView.getItems().clear();
        setEntitiesChoiceBox();
        setPropertyChoiceBox();

        //propertyChoiceBox.getItems().clear();
    }

    private void onSelectedSimulationStop() {
        analyzePaginationController.setPopulationData(engine.getPopulationCountSortedByName(selectedSimulation.get().getId()));
    }

    public Category getSelectedSimulation() {
        return selectedSimulation.get();
    }

    public ObjectProperty<Category> getSelectedSimulationProperty() {
        return selectedSimulation;
    }

    public Queue<Category> getWaitingQueue() {
        return waitingQueue;
    }

    public String getPropertyToView() {
        return propertyToView.get();
    }

    public StringProperty propertyToViewProperty() {
        return propertyToView;
    }

    public String getEntityToView() {
        return entityToView.get();
    }

    public StringProperty entityToViewProperty() {
        return entityToView;
    }

    public boolean isNewFileLoaded() {
        return isNewFileLoaded.get();
    }

    public BooleanProperty isNewFileLoadedProperty() {
        return isNewFileLoaded;
    }

    public boolean isIsSelectedSimulationEnded() {
        return isSelectedSimulationEnded.get();
    }

    public BooleanProperty isSelectedSimulationEndedProperty() {
        return isSelectedSimulationEnded;
    }

    public ObjectProperty<Category> selectedSimulationProperty() {
        return selectedSimulation;
    }

    public void setEngine(PredictionsLogic engine) {
        this.engine = engine;
        progressController.setEngine(engine);
    }

    public void setPredictionsMainAppController(PredictionsMainAppController predictionsMainAppController) {
        this.predictionsMainAppController = predictionsMainAppController;
        progressController.setPredictionsMainAppController(predictionsMainAppController);
        isNewFileLoaded.bind(predictionsMainAppController.isFileSelectedProperty());
    }

    private void setPropertyChoiceBox() {
        propertyChoiceBox.getItems().clear();

        if (entityToView.isNotNull().get()) {
            WorldDTO simulation = engine.getLoadedSimulationDetails();
            simulation
                    .getEntities()
                    .stream()
                    .filter(entity -> entity.getName().equals(entityToView.get()))
                    .findAny()
                    .get()
                    .getProperties()
                    .forEach(property -> propertyChoiceBox.getItems().add(property.getName()));
        }
    }

    private void setEntitiesChoiceBox() {
        entityChoiceBox.getItems().clear();

        WorldDTO simulationDTO = engine.getLoadedSimulationDetails();
        simulationDTO
                .getEntities()
                .forEach(entity -> entityChoiceBox.getItems().add(entity.getName()));
    }

    public void setTabPane(TabPane tabPane) {
        this.tabPane = tabPane;
        progressController.setTabPane(tabPane);
    }

    public void setDisableEntityChoiceBoxValue(boolean disable) {
        String value = entityChoiceBox.getValue();

        entityChoiceBox.setDisable(disable);
        entityChoiceBox.valueProperty().set(disable ? null : value);

    }

    public void setDisablePropertyChoiceBoxValue(boolean disable) {
        String value = propertyChoiceBox.getValue();

        propertyChoiceBox.setDisable(disable);
        propertyChoiceBox.valueProperty().set(disable ? null : value);
    }
}