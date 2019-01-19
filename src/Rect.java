import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.stage.Stage;
import javafx.scene.input.KeyEvent;

class Rect extends Application{
    public static void main(String[] args) { launch(args); }
    public String toString(){ return "Rect at ( "+stage.getX()+" , "+stage.getY()+" )   with size ("+stage.getWidth()+" , "+stage.getHeight()+" )"; }
    private Stage stage = new Stage();

    public Rect(int width, int height, int x, int y){
        start(stage);
        setSize(width, height);
        setPosition(x, y);
    }
    public void start(Stage stage){
        stage.setResizable(false);
        this.stage=stage;
        Canvas canvas = new Canvas(1,1);
        Group root = new Group(canvas);
        Scene scene = new Scene(root);
        this.stage.setScene(scene);
        this.stage.show();
    }

    private void setSize(int width, int height){
        stage.setWidth(width);
        stage.setHeight(height);
    }
    public void setPosition(int x, int y){
        stage.setX(x);
        stage.setY(y);
    }

    public void addEventHandler(EventType<KeyEvent> eventType, EventHandler<KeyEvent> handler){ stage.addEventHandler(eventType,handler); }

    public int getX(){return (int) stage.getX();}
    public int getY(){return (int) stage.getY();}

    public void setResizable(Boolean b){stage.setResizable(b);}
    public void putOnTop(){
        stage.close();
        stage.show();
    }
    public void kill(){ stage.close(); }
}