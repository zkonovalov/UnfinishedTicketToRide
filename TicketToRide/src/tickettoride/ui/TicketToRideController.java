package tickettoride.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import tickettoride.model.GameState;
import tickettoride.model.MapData;
import tickettoride.model.MapData.CardColor;
import tickettoride.model.MapData.Connection;
import tickettoride.model.MapData.Destination;
import tickettoride.utilities.ImageLoader;

public class TicketToRideController {

	private final double destinationCircleDiameter = 20;
	
	private Property<GameState> game = new SimpleObjectProperty<>();
	private ObjectBinding<MapData> mapData = 
			Bindings.createObjectBinding(
					() -> game.getValue() == null ? null : game.getValue().getMap(),
					game);
	
	@FXML
	protected ScrollPane mapPane;
	
	@FXML
	protected MapPane mapAnchorPane;
	@FXML
	protected Canvas mapCanvas;
	
	@FXML
	protected ImageView backgroundImage;
	
	@FXML
	protected Rectangle cardToDraw1;
	@FXML
	protected Rectangle cardToDraw2;
	@FXML
	protected Rectangle cardToDraw3;
	@FXML
	protected Rectangle cardToDraw4;
	@FXML
	protected Rectangle cardToDraw5;
	@FXML
	protected Rectangle deck;
	
	private final double MIN_MAP_WIDTH = 400;
	private final double MIN_MAP_HEIGHT = 400;
	public void initialize() {
		
		mapPane.setVbarPolicy(ScrollBarPolicy.ALWAYS);
		mapPane.setHbarPolicy(ScrollBarPolicy.ALWAYS);

		mapCanvas.widthProperty().bind(Bindings.max(MIN_MAP_WIDTH, mapPane.widthProperty().subtract(15.0)));
		mapCanvas.heightProperty().bind(Bindings.max(MIN_MAP_HEIGHT, mapPane.heightProperty().subtract(15.0)));
		
		backgroundImage.fitWidthProperty().bind(mapCanvas.widthProperty());
		backgroundImage.fitHeightProperty().bind(mapCanvas.heightProperty());
		
		mapCanvas.setOpacity(0.5);
		
		mapAnchorPane.getMapProperty().bind(mapData);
		mapAnchorPane.setBackgroundCanvas(mapCanvas);
		
		game.addListener((mapData) -> paintMap());
		mapCanvas.widthProperty().addListener((mapData) -> paintMap());
		mapCanvas.heightProperty().addListener((mapData) -> paintMap());
		
		setUpSidePanelCardBindings();
	}
	
	private void setUpSidePanelCardBindings() {
		
		try {
			Image transportCardBack = ImageLoader.load("images/cardBack.jpg");
			deck.setFill(new ImagePattern(transportCardBack));
			
			Map<CardColor, Image> transportationCardImages = new HashMap<>();
			transportationCardImages.put(CardColor.ANY, ImageLoader.load("images/wild.jpg"));
			transportationCardImages.put(CardColor.BLACK, ImageLoader.load("images/black.jpg"));
			transportationCardImages.put(CardColor.BLUE, ImageLoader.load("images/blue.jpg"));
			transportationCardImages.put(CardColor.GREEN, ImageLoader.load("images/green.jpg"));
			transportationCardImages.put(CardColor.ORANGE, ImageLoader.load("images/orange.jpg"));
			transportationCardImages.put(CardColor.PURPLE, ImageLoader.load("images/purple.jpg"));
			transportationCardImages.put(CardColor.RED, ImageLoader.load("images/red.jpg"));
			transportationCardImages.put(CardColor.WHITE, ImageLoader.load("images/white.jpg"));
			transportationCardImages.put(CardColor.YELLOW, ImageLoader.load("images/yellow.jpg"));
			
			List<Rectangle> drawCardRects = Arrays.asList(
					cardToDraw1,
					cardToDraw2,
					cardToDraw3,
					cardToDraw4,
					cardToDraw5);
			
			game.addListener((x) -> {
				drawCardRects.forEach(r -> r.fillProperty().unbind());
				if(game.getValue() != null) {
					List<ObjectProperty<CardColor>> cardProperties = game.getValue().getFaceUpTransportationCardProperties();
					for(int i =0; i < drawCardRects.size(); i++) {
						final ObjectProperty<CardColor> colorProperty = cardProperties.get(i);
						drawCardRects.get(i)
							.fillProperty()
							.bind(Bindings.createObjectBinding(
									() -> {
										CardColor color = colorProperty.getValue();
										if(color == null) {
											return null;
										}
										else {
											return new ImagePattern(transportationCardImages.get(color));
										}
									},
									colorProperty
									)
									);
					}
				}
			});
			
			//TODO, it turns out that this isn't actually going to work when the cards change
			//since the binding only hinges on when the game changes. This was just a flawed idea
			//but at least it tests out how it displays for now.
//			for(int i = 0; i < 5; i++) {
//				final int finalI = i;
//				drawCardRects[i]
//						.fillProperty()
//						.bind(
//							Bindings.createObjectBinding(
//									() -> {
//										if(game.getValue() == null) {
//											return null;
//										}
//										if(game.getValue().getFaceUpTransportationCardProperties().get(finalI).isNull().get()) {
//											return null;
//										}
//										Image img = null;
//										switch(game.getValue().getFaceUpTransportationCardProperties().get(finalI).getValue()) {
//										case ANY:
//											img = wild;
//											break;
//										case BLACK:
//											img = black;
//											break;
//										case BLUE:
//											img = blue;
//											break;
//										case GREEN:
//											img = green;
//											break;
//										case ORANGE:
//											img = orange;
//											break;
//										case PURPLE:
//											img = purple;
//											break;
//										case RED:
//											img = red;
//											break;
//										case WHITE:
//											img = white;
//											break;
//										case YELLOW:
//											img = yellow;
//											break;
//										}
//										return new ImagePattern(img);
//									},
//									game
//									)
//								);
//				
//			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
	@FXML
	public void createNewGame() throws FileNotFoundException {
		//TODO probably use a file chooser to load a file
		//GameController gameController = null; //TODO new GameController();
		//For now, we'll just manually build some stuff to test the map drawing
//		File backgroundImageFile = new File("C:/Users/nate/git/TicketToRide/TicketToRide/resources/maps/usaMap.jpg");
//
//		Image background = new Image(backgroundImageFile.toURI().toString());
		Image background = ImageLoader.load("maps/usaMap.jpg");
		
		Destination d1 = new Destination("Awesomeville", 0.5, 0.5);
		Destination d2 = new Destination("Paradise City", 0.6, 0.7);
		Destination d3 = new Destination("SuperdyDuperBurgh", 0.2, 0.8);
		Destination d4 = new Destination("Justokayville", 0.3, 0.4);
				
		MapData hardCodedMapData = new MapData() {

			@Override
			public Collection<Destination> getDestinations() {
				return Arrays.asList(d1, d2, d3, d4);
			}

			@Override
			public Image getBackgroundImage() {
				return background;
			}

			@Override
			public Collection<Connection> getConnectionsToOrFromDest(Destination dest) {
				return getConnections().stream().filter(c -> c.getStart().equals(dest) || c.getEnd().equals(dest))
									.collect(Collectors.toList());
			}

			@Override
			public Collection<Connection> getConnections() {
				return Arrays.asList(new Connection(d1, d3, CardColor.BLACK, 3),
						new Connection(d1, d2, CardColor.YELLOW, 1),
						new Connection(d2, d3, CardColor.ORANGE, 2),
						new Connection(d3, d4, CardColor.ANY, 3),
						new Connection(d4, d3, CardColor.BLUE, 3),
						new Connection(d4, d1, CardColor.GREEN, 3));
			}
			
		};
		
		game.setValue(new GameState(hardCodedMapData));
		
//		Circle circle = new Circle();
//		circle.centerXProperty().bind(mapCanvas.widthProperty().divide(2.0));
//		circle.centerYProperty().bind(mapCanvas.heightProperty().divide(2.0));
//		circle.setRadius(destinationCircleDiameter);
//		
//		mapAnchorPane.getChildren().add(circle);
	}
	
	private void paintMap() {

		GraphicsContext gc = mapCanvas.getGraphicsContext2D();
		
		gc.clearRect(0, 0, mapCanvas.getWidth(), mapCanvas.getHeight());
		
		if(mapData.getValue() != null) {
			MapData map = mapData.getValue();

			gc.drawImage(map.getBackgroundImage(), 0, 0, mapCanvas.getWidth(), mapCanvas.getHeight());
		
			

//			Map<Destination, Point> destinationPoints = new HashMap<>();
//			
//			//Draw cities
//			for(Destination dest : map.getDestinations()) {
//				gc.setFill(Color.BLACK);
//				
//				double x = dest.getXFraction() * mapCanvas.getWidth();
//				double y = dest.getYFraction() * mapCanvas.getHeight();
//				
//				destinationPoints.put(dest, new Point(x,y));
//
//				x -= destinationCircleDiameter/2;
//				y -= destinationCircleDiameter/2;
//				
//				gc.strokeOval(x, y, destinationCircleDiameter, destinationCircleDiameter);
//				
//				gc.strokeText(dest.getName(), x + destinationCircleDiameter, y);
//			}
//			
//			//Draw connections
//			for(Connection conn : map.getConnections()) {
//				Point start = destinationPoints.get(conn.getStart());
//				Point end = destinationPoints.get(conn.getEnd());
//				
//				gc.strokeLine(start.getX(), start.getY(), end.getX(), end.getY());
//			}
		}
	}
	
	private class Point {
		private final double x;
		private final double y;
		
		public Point(double x, double y) {
			this.x = x;
			this.y = y;
		}
		
		public double getX() { return x;}
		public double getY() { return y;}
	}
}
