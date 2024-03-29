package de.hsrm.mi.swt.presentation;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import business.components.Crossing;
import business.components.Curve;
import business.components.Direction;
import business.components.Junction;
import business.components.Straight;
import business.components.Street;
import business.components.Trafficlight;
import business.components.TrafficlightStatus;
import business.components.TriggerPoints;
import business.components.Vehicle;
import business.simulation.Grid;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class VerkehrssimulationController implements Initializable {

	private static final int GRIDSIZE = 5;
	private int playbackspeed = 5;
	private int streetCounter;

	private Grid grid = new Grid(GRIDSIZE);
	private HashMap<Vehicle, ImageView> vehicles = new HashMap<>();
	private Timeline timelineTrafficLights, timelineVehicle;
	private ImageView vehicleImgV;

	@FXML
	private Button loadiSimulationButton;

	@FXML
	private Button loadSimulationBackToMenu;

	@FXML
	private AnchorPane menuPane;

	@FXML
	private Button menuNewSimulationButton;

	@FXML
	private Button menuLoadSimulationButton;

	@FXML
	private Button quitSimulationButton;

	@FXML
	private AnchorPane simulationPane;

	@FXML
	private AnchorPane simulationGrid;

	@FXML
	private GridPane simulationGridPane;

	@FXML
	private Text showInfoLabelOne;

	@FXML
	private Text showInfoLabelTwo;

	@FXML
	private Text showInfoLabelThree;

	@FXML
	private Button controllButtonStart;

	@FXML
	private Button controllButtonStop;

	@FXML
	private Button controllButtonIncrease;

	@FXML
	private Button controllButtonDrecease;

	@FXML
	private ImageView StreetElementStraight;

	@FXML
	private ImageView StreetElementCurve;

	@FXML
	private ImageView StreetElementCrossing;

	@FXML
	private ImageView StreetElementJunction;

	@FXML
	private ImageView StreetElementCar;

	@FXML
	private ImageView StreetElementTrafficLight;

	@FXML
	private Button functionButtonSafe;

	@FXML
	private Button functioButtonLoad;

	@FXML
	private Button functionButtonMenuBack;

	@FXML
	private Button functionButtonQuit;

	@FXML
	private Button functionButtonHelp;

	@FXML
	void handleDragDetection(MouseEvent event) {

		String id = null;

		try {

			ImageView picked = (ImageView) event.getPickResult().getIntersectedNode();

			if (picked.getId().equals("StreetElementStraight")) {
				id = "Straight";
			} else if (picked.getId().equals("StreetElementCrossing")) {
				id = "Crossing";
			} else if (picked.getId().equals("StreetElementCurve")) {
				id = "Curve";
			} else if (picked.getId().equals("StreetElementJunction")) {
				id = "Junction";
			} else if (picked.getId().equals("StreetElementCar")) {
				id = "StreetElementCar";
			} else if (picked.getId().equals("StreetElementTrafficLight")) {
				id = "TrafficLight";
			}

			Dragboard db = picked.startDragAndDrop(TransferMode.ANY);
			ClipboardContent cb = new ClipboardContent();

			cb.putImage(picked.getImage());
			cb.putString(id);
			db.setContent(cb);

			event.consume();

		} catch (Exception e) {
			System.out.println("Wohl abgerutscht beim platzieren - halb so wild. Einfach weitermachen.");
		}
	}

	@FXML
	void handleImageDragOver(DragEvent event) {
		if (event.getDragboard().hasImage()) {
			event.acceptTransferModes(TransferMode.ANY);
		}
	}

	@FXML
	void handleImageDrop(DragEvent event) {

		Node node = event.getPickResult().getIntersectedNode();

		double doubleX = event.getSceneX();
		double doubleY = event.getSceneY();

		Integer cIndex = GridPane.getColumnIndex(node);
		Integer rIndex = GridPane.getRowIndex(node);

		int x = cIndex == null ? 0 : cIndex;
		int y = rIndex == null ? 0 : rIndex;

		Image img = event.getDragboard().getImage();
		String id = event.getDragboard().getString();

		switch (id) {
		case "Straight":

			Straight straight = new Straight();
			grid.placeStreet(straight, x, y);

			break;

		case "Curve":

			Curve curve = new Curve();
			grid.placeStreet(curve, x, y);
			break;

		case "Junction":
			Junction junction = new Junction();
			grid.placeStreet(junction, x, y);
			break;

		case "Crossing":
			Crossing crossing = new Crossing();
			grid.placeStreet(crossing, x, y);
			break;

		case "TrafficLight":

			List<Trafficlight> trafficlights = new ArrayList<>();

			for (Direction d : grid.getStreet(x, y).getDirections()) {

				Trafficlight trafficlight = new Trafficlight(d);

				if (d.equals(Direction.UP) || d.equals(Direction.DOWN)) {
					trafficlight.switchLight();
					trafficlight.switchLight();
				}
				trafficlights.add(trafficlight);
			}

			grid.getStreet(x, y).addTrafficlights(trafficlights);

			Street street = grid.getStreet(x, y);

			if (street.toString().contains("Straight")) {

				ImageView image = new ImageView("/gerade_rot.png");
				image.setFitHeight(100);
				image.setFitWidth(100);
				image.setRotate(90 * grid.getStreet(x, y).getRotationCount().doubleValue());
				simulationGridPane.add(image, x, y);

			} else if (street.toString().contains("Crossing")) {

				ImageView image = new ImageView("/kreuzung_situation1.png");
				image.setFitHeight(100);
				image.setFitWidth(100);
				image.setRotate(90 * grid.getStreet(x, y).getRotationCount().doubleValue());
				simulationGridPane.add(image, x, y);

			} else if (street.toString().contains("Junction")) {

				ImageView image = new ImageView("/abzweigung_ampel_2_gruen.png");
				image.setFitHeight(100);
				image.setFitWidth(100);
				image.setRotate(90 * grid.getStreet(x, y).getRotationCount().doubleValue());
				simulationGridPane.add(image, x, y);

			} else if (street.toString().contains("Curve")) {

				ImageView image = new ImageView("/kurve_rot.png");
				image.setFitHeight(100);
				image.setFitWidth(100);
				image.setRotate(90 * grid.getStreet(x, y).getRotationCount().doubleValue());
				simulationGridPane.add(image, x, y);

			}

			break;

		case "StreetElementCar":

			vehicleImgV = new ImageView("/smallcar.png");
			simulationGrid.getChildren().add(vehicleImgV);
			Vehicle v = new Vehicle(x * 100, y * 100, grid);

			vehicleImgV.setOnMouseClicked(e -> {

				if (e.getButton() == MouseButton.PRIMARY) {

					v.rotate();
					vehicleImgV.setRotate(90 * v.getRotationCount().get());

				}
			});

			vehicleImgV.setFitHeight(24);
			vehicleImgV.setFitWidth(24);

			vehicleImgV.setX(v.getXCarProperty().get() - vehicleImgV.getFitHeight() / 2);
			vehicleImgV.setY(v.getYCarProperty().get() - vehicleImgV.getFitWidth() / 2);
			vehicles.put(v, vehicleImgV);
			initVehicleListener(v);

			break;

		default:
			break;
		}

		if (!id.contains(("TrafficLight"))) {
			if (!id.equals("StreetElementCar")) {
				((ImageView) event.getPickResult().getIntersectedNode()).setImage(img);
				streetCounter++;
			}
		}
	}

	private void initVehicleListener(Vehicle v) {

		v.getXCarProperty().addListener((observable, oldValue, newV) -> {
			ImageView imageV = vehicles.get(v);
			imageV.setX(v.getXPosition() - imageV.getFitWidth() / 2);

		});

		v.getYCarProperty().addListener((observable, oldValue, newV) -> {
			ImageView imageV = vehicles.get(v);
			imageV.setY(v.getYPosition() - imageV.getFitHeight() / 2);

		});
	}

	@FXML
	void onMouseClickedGrid(MouseEvent event) {

		try {

			ImageView img = (ImageView) event.getPickResult().getIntersectedNode();

			Integer cIndex = GridPane.getColumnIndex(img);
			Integer rIndex = GridPane.getRowIndex(img);
			int x = cIndex == null ? 0 : cIndex;
			int y = rIndex == null ? 0 : rIndex;

			if (event.getButton() == MouseButton.PRIMARY) {

				Street street = grid.getStreet(x, y);

				if (street.getClass().toString().contains("Straight")) {
					((Straight) street).rotate();
				} else if (street.getClass().toString().contains("Curve")) {
					((Curve) street).rotate();
				} else if (street.getClass().toString().contains("Junction")) {
					((Junction) street).rotate();
				} else if (street.getClass().toString().contains("Crossing")) {
					((Crossing) street).rotate();
				}

				img.setRotate(90 * street.getRotationCount().get());
			}

			if (event.getButton() == MouseButton.SECONDARY) {

				grid.removeItem(x, y);
				img.setImage(null);
			}

		} catch (Exception e) {
			System.out.println("Nur abgerutscht beim Klicken. Kann mal vorkommen.");
		}

	}

	@FXML
	void backToMenu(ActionEvent event) {
		scrollToMenu();
		timelineTrafficLights.stop();
		timelineVehicle.stop();
	}

	@FXML
	void endSimulation(ActionEvent event) {
		timelineVehicle.stop();
		timelineTrafficLights.stop();

		StreetElementCar.setDisable(false);
		StreetElementCrossing.setDisable(false);
		StreetElementCurve.setDisable(false);
		StreetElementJunction.setDisable(false);
		StreetElementStraight.setDisable(false);
		StreetElementTrafficLight.setDisable(false);

		controllButtonStart.setDisable(false);
		controllButtonDrecease.setDisable(true);
		controllButtonIncrease.setDisable(true);
		controllButtonStop.setDisable(true);
	}

	@FXML
	void increaseSpeed(ActionEvent event) {

		if (playbackspeed < 10) {
			playbackspeed++;
			timelineVehicle.stop();
			timelineVehicle.setRate(playbackspeed);
			timelineVehicle.play();
			timelineTrafficLights.stop();
			timelineTrafficLights.setRate(playbackspeed / 2);
			timelineTrafficLights.play();
			showInfoLabelThree.setText("Abspielgeschwindkeit: " + playbackspeed);
		}

	}

	@FXML
	void decreaseSpeed(ActionEvent event) {

		if (playbackspeed > 1) {
			playbackspeed--;
			timelineVehicle.stop();
			timelineVehicle.setRate(playbackspeed);
			timelineVehicle.play();
			timelineTrafficLights.stop();
			timelineTrafficLights.setRate(playbackspeed / 2);
			timelineTrafficLights.play();
			showInfoLabelThree.setText("Abspielgeschwindkeit: " + playbackspeed);
		}

	}

	@FXML
	void newSimulation(ActionEvent event) {
		scrollToSimulation();
	}

	@FXML
	void quitSimulation(ActionEvent event) {
		System.exit(0);
	}

	@FXML
	void quitSiumulation(ActionEvent event) {
		System.exit(0);
	}

	@FXML
	void sendHelp(ActionEvent event) {

		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Bedienungsanleitung");
		alert.setHeaderText("Das Programm ist wie folgt zu Benutzen:");
		alert.setContentText(
				"Elemente per Drag and Drop platzieren und per linkem Mausklick drehen. Elemente mit Rechsklick entfernen. Wenn alle Elemente platziert, dann Start druecken. Sollten sich die Autos mal festfahren, den Stau einfach durch anklicken eines Autos lösen");
		alert.showAndWait();
	}

	@FXML
	void startSimulation(ActionEvent event) {

		TriggerPoints trigger = new TriggerPoints();

		Duration durationVehilce = Duration.millis(50);
		KeyFrame vehicleFrame = new KeyFrame(durationVehilce, ev -> {
			for (Vehicle car : vehicles.keySet()) {
				boolean drive = true;

				int x = car.getXPosition();
				int y = car.getYPosition();

				if (collisionCheck(vehicles.get(car), car.getDirection())) {
					drive = false;
				} else {
					drive = true;
				}

				try {

					if (trigger.isTriggered(car, x, y)) {
						car.setNextDirection(
								trigger.chooseRandomDirection(grid.getStreet(x / 100, y / 100), car.getDirection()));

						for (Trafficlight t : grid.getStreet(x / 100, y / 100).getTrafficlights()) {

							if (t.getDirection().equals(car.tellOposite(car.getDirection()))) {
								if (!(t.getStatus().get() == TrafficlightStatus.GREEN)) {
									drive = false;
								}
							}
						}
					}
				}

				catch (Exception e) {

					car.setDirection(trigger.tellOposite(car.getDirection()));
					// Abfangen

				}

				if (trigger.canTurnTo(car) == car.getNextDirection()) {

					car.setDirection(car.getNextDirection());

					switch (car.getDirection()) {
					case RIGHT:
						vehicles.get(car).setRotate(0);
						break;
					case DOWN:
						vehicles.get(car).setRotate(90);
						break;
					case LEFT:
						vehicles.get(car).setRotate(180);
						break;
					case UP:
						vehicles.get(car).setRotate(270);
						break;

					default:
						break;
					}

				}

				if (drive) {
					car.drive();
				}
			}
		});

		timelineVehicle = new Timeline();
		timelineVehicle.getKeyFrames().add(vehicleFrame);
		timelineVehicle.setCycleCount(Timeline.INDEFINITE);
		timelineVehicle.setRate(playbackspeed);

		timelineVehicle.play();

		Duration durationTrafficLight = Duration.millis(5000);
		KeyFrame trafficLightFrame = new KeyFrame(durationTrafficLight, e -> {

			ImageView newStreetImg = null;

			for (int x = 0; x < GRIDSIZE; x++) {
				for (int y = 0; y < GRIDSIZE; y++) {
					if (grid.getStreet(x, y) != null) {
						for (Trafficlight t : grid.getStreet(x, y).getTrafficlights()) {

							t.switchLight();

							Street street = grid.getStreet(x, y);

							if (t.getTrafficlightStatus().equals(TrafficlightStatus.GREEN)) {
								newStreetImg = t.loadImage(street, t.getTrafficlightStatus());
								newStreetImg.setRotate(90 * grid.getStreet(x, y).getRotationCount().doubleValue());
								simulationGridPane.add(newStreetImg, x, y);
							}

							if (t.getTrafficlightStatus().equals(TrafficlightStatus.ORANGE)) {
								newStreetImg = t.loadImage(street, t.getTrafficlightStatus());
								newStreetImg.setRotate(90 * grid.getStreet(x, y).getRotationCount().doubleValue());
								simulationGridPane.add(newStreetImg, x, y);
							}

							if (t.getTrafficlightStatus().equals(TrafficlightStatus.ORANGEGREEN)) {
								newStreetImg = t.loadImage(street, t.getTrafficlightStatus());
								newStreetImg.setRotate(90 * grid.getStreet(x, y).getRotationCount().doubleValue());
								simulationGridPane.add(newStreetImg, x, y);
							}

							if (t.getTrafficlightStatus().equals(TrafficlightStatus.RED)) {
								newStreetImg = t.loadImage(street, t.getTrafficlightStatus());
								newStreetImg.setRotate(90 * grid.getStreet(x, y).getRotationCount().doubleValue());
								simulationGridPane.add(newStreetImg, x, y);
							}
						}
					}
				}
			}
		});

		timelineTrafficLights = new Timeline();
		timelineTrafficLights.getKeyFrames().add(trafficLightFrame);
		timelineTrafficLights.setCycleCount(Timeline.INDEFINITE);

		timelineTrafficLights.play();

		showInfoLabelOne.setText("Anzahl Autos: " + vehicles.size());
		showInfoLabelTwo.setText("Anzahl Strassen: " + streetCounter);
		showInfoLabelThree.setText("Abspielgeschwindkeit: " + playbackspeed);

		controllButtonIncrease.setDisable(false);
		controllButtonDrecease.setDisable(false);
		controllButtonStop.setDisable(false);
		controllButtonStart.setDisable(true);

		StreetElementCar.setDisable(true);
		StreetElementCrossing.setDisable(true);
		StreetElementCurve.setDisable(true);
		StreetElementJunction.setDisable(true);
		StreetElementStraight.setDisable(true);
		StreetElementTrafficLight.setDisable(true);

	}

	private boolean collisionCheck(ImageView carImg, Direction carDirection) {

		boolean collisionDetected = false;

		for (Vehicle v : vehicles.keySet()) {

			if (carDirection.equals(Direction.DOWN)) {
				if (!carImg.equals(vehicles.get(v))) {
					if (carImg.getBoundsInParent().intersects(v.getXCarProperty().get(), v.getYCarProperty().get() - 18,
							carImg.getFitHeight(), carImg.getFitWidth() / 2)) {

						collisionDetected = true;
					}
				}
			}

			if (carDirection.equals(Direction.UP)) {
				if (!carImg.equals(vehicles.get(v))) {
					if (carImg.getBoundsInParent().intersects(v.getXCarProperty().get(), v.getYCarProperty().get() + 18,
							carImg.getFitHeight() / 2, 0)) {

						collisionDetected = true;
					}
				}
			}

			if (carDirection.equals(Direction.LEFT)) {
				if (!carImg.equals(vehicles.get(v))) {
					if (carImg.getBoundsInParent().intersects(v.getXCarProperty().get() + 25, v.getYCarProperty().get(),
							carImg.getFitHeight() / 2, 0)) {

						collisionDetected = true;
					}
				}
			}
			if (carDirection.equals(Direction.RIGHT)) {
				if (!carImg.equals(vehicles.get(v))) {
					if (carImg.getBoundsInParent().intersects(v.getXCarProperty().get() - 25, v.getYCarProperty().get(),
							carImg.getFitHeight() / 2, carImg.getFitWidth())) {

						collisionDetected = true;
					}
				}
			}
		}
		return collisionDetected;
	}

	public void scrollToMenu() {
		TranslateTransition tr3 = new TranslateTransition();
		tr3.setDuration(Duration.millis(300));
		tr3.setToX(0);
		tr3.setToY(-700);
		tr3.setNode(simulationPane);
		TranslateTransition tr2 = new TranslateTransition();
		tr2.setDuration(Duration.millis(300));
		tr2.setFromX(0);
		tr2.setFromY(700);
		tr2.setToX(0);
		tr2.setToY(0);
		tr2.setNode(menuPane);
		ParallelTransition pt = new ParallelTransition(tr2, tr3);
		pt.play();
	}

	public void scrollToSimulation() {
		TranslateTransition tr1 = new TranslateTransition();
		tr1.setDuration(Duration.millis(300));
		tr1.setToX(0);
		tr1.setToY(-700);
		tr1.setNode(menuPane);
		TranslateTransition tr2 = new TranslateTransition();
		tr2.setDuration(Duration.millis(300));
		tr2.setFromX(0);
		tr2.setFromY(700);
		tr2.setToX(0);
		tr2.setToY(0);
		tr2.setNode(simulationPane);
		ParallelTransition pt = new ParallelTransition(tr1, tr2);
		pt.play();
	}

	public void initialize(URL arg0, ResourceBundle arg1) {

		controllButtonIncrease.setDisable(true);
		controllButtonDrecease.setDisable(true);
		controllButtonStop.setDisable(true);

	}

}