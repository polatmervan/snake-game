import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.LinkedList;
import java.util.Random;

public class Main extends Application {

    // Spielfeldgröße in Anzahl der Felder
    private static final int WIDTH = 20; 
    private static final int HEIGHT = 20;
    private static final int BLOCK_SIZE = 25;
    
    // Anfangsgeschwindigkeit (Millisekunden zwischen Updates)
    private long speed = 200_000_000; // 200ms in Nanosekunden (AnimationTimer arbeitet in ns)
    
    private LinkedList<Point> snake = new LinkedList<>();
    private Direction direction = Direction.RIGHT;
    private boolean running = false;
    
    private Point food;
    private Random rand = new Random();
    
    private long lastUpdateTime = 0;
    
    enum Direction {
        UP, DOWN, LEFT, RIGHT
    }
    
    class Point {
        int x, y;
        Point(int x, int y){
            this.x = x;
            this.y = y;
        }
    }
    
    @Override
    public void start(Stage primaryStage) {
        Canvas canvas = new Canvas(WIDTH * BLOCK_SIZE, HEIGHT * BLOCK_SIZE);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        initGame();
        draw(gc);
        
        Scene scene = new Scene(new StackPane(canvas));
        
        // Steuerung über Keyboard
        scene.setOnKeyPressed(e -> {
            if(!running) running = true;
            KeyCode code = e.getCode();
            switch(code){
                case UP:
                    if(direction != Direction.DOWN) direction = Direction.UP;
                    break;
                case DOWN:
                    if(direction != Direction.UP) direction = Direction.DOWN;
                    break;
                case LEFT:
                    if(direction != Direction.RIGHT) direction = Direction.LEFT;
                    break;
                case RIGHT:
                    if(direction != Direction.LEFT) direction = Direction.RIGHT;
                    break;
                // Geschwindigkeitsanpassung
                case Q:
                    // schneller -> Speed verringern
                    speed = Math.max(50_000_000, speed - 25_000_000); 
                    break;
                case E:
                    // langsamer -> Speed erhöhen
                    speed = Math.min(1_000_000_000, speed + 25_000_000);
                    break;
                default:
                    break;
            }
        });
        
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (running) {
                    if ((now - lastUpdateTime) > speed) {
                        update();
                        draw(gc);
                        lastUpdateTime = now;
                    }
                }
            }
        };
        
        timer.start();
        
        primaryStage.setTitle("Snake Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private void initGame() {
        snake.clear();
        // Startposition der Schlange in der Mitte des Feldes
        snake.add(new Point(WIDTH/2, HEIGHT/2));
        snake.add(new Point(WIDTH/2-1, HEIGHT/2));
        snake.add(new Point(WIDTH/2-2, HEIGHT/2));
        
        generateFood();
        
        direction = Direction.RIGHT;
        running = false;
    }
    
    private void update() {
        // Kopf der Schlange
        Point head = snake.getFirst();
        Point newPoint = switch(direction){
            case UP -> new Point(head.x, head.y - 1);
            case DOWN -> new Point(head.x, head.y + 1);
            case LEFT -> new Point(head.x - 1, head.y);
            case RIGHT -> new Point(head.x + 1, head.y);
        };

        // Randcheck - wenn aus dem Spielfeld raus, von der anderen Seite wieder rein (Wrap-Around) 
        // (Kann natürlich je nach Wunsch auch Game Over sein)
        if (newPoint.x < 0) newPoint.x = WIDTH - 1;
        if (newPoint.x >= WIDTH) newPoint.x = 0;
        if (newPoint.y < 0) newPoint.y = HEIGHT - 1;
        if (newPoint.y >= HEIGHT) newPoint.y = 0;
        
        // Prüfen, ob Schlangenkopf in den Körper beißt
        for (Point p : snake) {
            if (p.x == newPoint.x && p.y == newPoint.y) {
                // Game Over, neu starten
                initGame();
                return;
            }
        }

        // Essen gefressen?
        snake.addFirst(newPoint);
        if (newPoint.x == food.x && newPoint.y == food.y) {
            generateFood();
        } else {
            snake.removeLast();
        }
    }
    
    private void draw(GraphicsContext gc) {
        // Hintergrund
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, WIDTH * BLOCK_SIZE, HEIGHT * BLOCK_SIZE);
        
        // Essen zeichnen
        gc.setFill(Color.RED);
        gc.fillOval(food.x * BLOCK_SIZE, food.y * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
        
        // Schlange zeichnen
        gc.setFill(Color.GREEN);
        for(Point p : snake) {
            gc.fillRect(p.x * BLOCK_SIZE, p.y * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
        }
    }
    
    private void generateFood() {
        food = new Point(rand.nextInt(WIDTH), rand.nextInt(HEIGHT));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
