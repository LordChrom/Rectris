import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.stage.Stage;

public class Square extends Application{
    public static final int size = 22;

    private Stage stage = new Stage();
    private Position gridPosition;      //position in the field of the square. Not used by this class but used by others to keep things organized

    public static void main(String[] args) { launch(args); }
    public String toString(){ return "Square at ( "+stage.getX()+" , "+stage.getY()+" )"+ "with relative grid position of "+ gridPosition; }
    public Square(){
        start(stage);
    }
    public Square(int x, int y){
        start(stage);
        setXY(x,y);
    }

    public void start(Stage stage){
        stage.setResizable(false);
        this.stage=stage;
        Canvas canvas = new Canvas(size,1);
        Group root = new Group(canvas);
        Scene scene = new Scene(root);
        this.stage.setScene(scene);
        stage.setWidth(size);
        stage.setHeight(size);
        stage.setResizable(false);
        this.stage.show();
    }
    public void kill(){ stage.close(); }

    public void setXY(int x, int y){
        stage.setX(x);
        stage.setY(y);
    }
    public void setOpacity(double opacity){ stage.setOpacity(opacity); }
    public void setGPos(Position pos){ gridPosition = pos; }
    public Position getGPos(){ return gridPosition; }
}