import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import java.util.ArrayList;

/*
* this is the main method for my Tetris program
* most of it makes sense, but there's just a couple things to note
* the int "gameState" is 0 when the game is in normal play, 1 when paused, and 2 on the game over screen.
*
* The right and left edges are the only windows set up to respond when a key is pressed.
* Windows can only detect key presses when focused. When a new window is created it is focused.
* As such, whenever I create a new window, I also put the right edge back on top with rightEdge.putOnTop()
*/

public class Main extends Application {
    public static final int stageWidth = 10;
    public static final int stageHeight = 20;

    private static boolean[][] backgroundData;                      //the field, true means there is a square, false means empty
    private ArrayList<Square> backgroundSquares;                    //hols all the squares that are part of the background
    private Rect leftEdge,rightEdge,levelLLeft, levelLBottom;       //left and right edge of field, pieces of the letter 'L' for the level display
    private Tetromino piece,nextPiece;                              //moving piece and stationary piece that displays what will be next
    private int boardWidth,x,y,score,level,gameState,linesCleared;  //pixel width of board, x y position of board.
    private long lastDown,gravityTime;                              //time piece last moved down and ms per piece fall due to gravity
    private AppNumber scoreDisplay,levelDisplay;                    //objects used to display numbers on screen
    private boolean ghostsEnabled;                                  //whether or not to show the transparent drop projection

    private void initialize(){//initializes all the variables
        backgroundData = new boolean[stageWidth][stageHeight];
        boardWidth = Square.size*stageWidth;
        lastDown = System.currentTimeMillis();

        piece = new Tetromino();
        nextPiece = new Tetromino();
        backgroundSquares = new ArrayList<>();
        scoreDisplay = new AppNumber();
        levelDisplay = new AppNumber();
        ghostsEnabled = true;

        leftEdge = new Rect(15, stageHeight * Square.size, 20, 20);
        rightEdge = new Rect(15, stageHeight * Square.size, 20 + boardWidth, 20);
        leftEdge.addEventHandler(KeyEvent.KEY_PRESSED, inputHandler);
        rightEdge.addEventHandler(KeyEvent.KEY_PRESSED, inputHandler);
        leftEdge.setResizable(false);
        rightEdge.setResizable(false);
        levelLLeft = new Rect(0,0,0,0);
        levelLBottom = new Rect(0,0,0,0);

        reset();
    }
    private void doGameLoop(){
        //run several times per second, does all periodic tasks
        //Only really checks for piece landing, gravity and game over, as inputs are handled when they happen.
        snapToEdge();
        if (gameState == 0) {//normal play
            if (System.currentTimeMillis() > lastDown + gravityTime) {  //if time elapsed since last descent is more than needed for gravity
                piece.moveDown();
                lastDown = System.currentTimeMillis();                  //marks last time of piece moving down
            }
            if (piece.hasLanded()) {
                addPieceToBackground();
                spawnPiece();
            }
            if (piece.spawnedLanded()) {    //true if the piece detected a collision when spawned. AKA when you lose
                gameState = 2;
                showScore();
            }
        }
    }
    private void reset() {
        hideScore();
        linesCleared = 0;
        level = 0;
        score = 0;
        for (int x = 0; x < stageWidth; x++) //resets the field by iterating through all xy points and setting to false
            for (int y = 0; y < stageHeight; y++)
                backgroundData[x][y] = false;
        for(Square sqr: backgroundSquares)
            sqr.kill();
        backgroundSquares.clear();

        gameState = 0; //return to normal play
        spawnPiece();
        updateLevel();
        snapToEdge();
        System.gc();
    }
    private void end(){ //closes all windows.
        piece.kill();
        nextPiece.kill();
        for(Square sqr: backgroundSquares)
            sqr.kill();
        hideScore();
        leftEdge.kill();
        rightEdge.kill();
    }

    private void updateLevel(){
        //determines time in ms for each descent of the piece due to gravity according to standardized Tetris formula
        gravityTime = (long) (1000* Math.pow((0.8-(double)level*0.007),level));
    }
    private void snapToEdge() { //updates position of all items if left edge of stage is moved
        if (leftEdge.getX() != x || leftEdge.getY() != y) { //detects movement
            x = leftEdge.getX() + 15;                   //puts x at the right side of the left edge so (0,0) for other objects is of the play area
            y = leftEdge.getY();
            piece.setFrameXY(x, y);                     //tells the piece where the field is
            nextPiece.setFrameXY(x,y);                  //tells the nextPiece where the field is
            rightEdge.setPosition(x + boardWidth, y);   //moves right edge
            piece.updateSquarePositions();              //tells the piece to update where its squares are
            nextPiece.updateSquarePositions();          //does the same for the nextPiece
            updateSquarePositions();                    //moves the squares in the background
        }
    }
    private void updateSquarePositions(){   //moves the squares in the background to where they should be
        for(Square sqr: backgroundSquares){
            Position pos = sqr.getGPos();
            sqr.setXY(x+pos.x*Square.size,y+pos.y*Square.size); //x and y * size of each piece, plus positioning of play field
        }
    }
    private void addPieceToBackground() {
        Position[] squarePositions = piece.getSquarePositions(); //gets positions of each square in the piece
        for(Position pos: squarePositions) {
            Square sqr = new Square();              //makes new square
            sqr.setGPos(pos);                       //sets square's position in the grid (not on the screen)
            backgroundSquares.add(sqr);
            backgroundData[pos.x][pos.y] = true;    //marks that the tile is now occupied
        }
        updateSquarePositions();    //moves squares into their proper places on the screen
    }
    private void spawnPiece(){
        piece.kill();                               //gets rid of old piece
        nextPiece.kill();                           //gets rid of old nextPiece
        scanRows();                                 //scans rows to see if previous piece filled one
        piece = new Tetromino(nextPiece.getType()); //makes the new piece spawn as the type shown to the user
        nextPiece = new Tetromino(stageWidth+3,2);  //makes new 'next' piece and puts it beyond the board
        if(ghostsEnabled)
            piece.createGhost();
        snapToEdge();
        rightEdge.putOnTop();
        lastDown=System.currentTimeMillis();
    }

    private void scanRows() {
        int simultaneousRowClears = 0; //keeps track of how many rows are cleared in a single scan AKA by a single piece

        for (int y = 0; y < stageHeight; y++) {                     //iterates through each row from top to bottom
            boolean rowFullSoFar = true;                            //starts off as true. Keeps track of whether the scanned part of the row is true
            for (int x = 0; x < stageWidth && rowFullSoFar; x++)    //loops until empty tile reached, in which case rowFullSoFar will be false, or line end
                rowFullSoFar = backgroundData[x][y];                //rowFullSoFar is updated to match currently scanned tile.

            if (rowFullSoFar) {                                     //if this is true after the above loop, the entire row is full
                clearRow(y);
                simultaneousRowClears++;
            }
        }
        int[] rowPoints = {0,40,100,300,1200};                      //number of points for different amount of simultaneous clears
        score += rowPoints[simultaneousRowClears]*(level+1);        //scores points based on simultaneous clears and level
        if(simultaneousRowClears>0)    //if anything was changed
            updateSquarePositions();
    }
    private void clearRow(int y){  //clears row at level y and moves down those above
        ArrayList<Square> squaresToRemove = new ArrayList<>(); //keeps track of which squares to remove

        for(Square sqr: backgroundSquares)  //looks at all background squares
            if(sqr.getGPos().y==y)          //mark for removal if it's on the row
                squaresToRemove.add(sqr);
        for(Square sqr: backgroundSquares)  //moves down all squares from above the row, done after previous loop for squares moving into this row
            if (sqr.getGPos().y < y)
                sqr.getGPos().down();
        for(Square sqr: squaresToRemove){
            sqr.kill();
            backgroundSquares.remove(sqr);
        }

        for(; y>0; y--)                                         // starts on current row, iterates up to row 1
            for(int x = 0;x<stageWidth;x++)                     //goes through each tile in the row
                backgroundData[x][y] = backgroundData[x][y-1];  //sets tile to above tile
        for(int x = 0; x<stageWidth;x++)                        //clears top row
            backgroundData[x][0] = false;
        linesCleared++;
        if(linesCleared>=10){
            linesCleared=0;
            level++;
            updateLevel();
        }
    }

    private void showScore() { //shows current score and level, called on pause and game over
        scoreDisplay.show(x - 15, y + Square.size * stageHeight + 50, score);   //shows score 50 below stage and aligned at the left
        levelLBottom = new Rect(60,22,x+boardWidth+180,y+78);                   //shows letter 'L' for level
        levelLLeft = new Rect(22,100,x+boardWidth+180,y);                       //continues 'L'
        levelDisplay.show(x+boardWidth+250,y,level+1);                          //shows what level player is on
        rightEdge.putOnTop();
    }
    private void hideScore(){
        scoreDisplay.hide();
        levelDisplay.hide();
        levelLLeft.kill();
        levelLBottom.kill();
    }
    private void toggleGhosts(){    //toggles showing of projected landing positions
        if(ghostsEnabled){
            piece.killGhost();
            ghostsEnabled=false;
        }else{
            piece.createGhost();
            rightEdge.putOnTop();
            ghostsEnabled=true;
        }
    }

    private final EventHandler<KeyEvent> inputHandler = e -> { //this is called when a key is pressed
        if(gameState==0) {//if in normal play
            switch(e.getCode()){    //switch for what key is pressed
                case DOWN:
                    piece.moveDown();
                    lastDown = System.currentTimeMillis();  //marks this as last time piece moved down
                    break;
                case LEFT:
                    piece.moveLeft();
                    break;
                case RIGHT:
                    piece.moveRight();
                    break;
                case X:
                    piece.rotateClockwise();
                    break;
                case Z:
                    piece.rotateCounterClockwise();
                    break;
                case SHIFT:
                    piece.hardDown();
                    break;
                case PERIOD:
                    gameState = 1; //pause
                    showScore();
                    break;
            }
        }else{
            if(gameState == 1 && e.getCode() == KeyCode.PERIOD) { // if paused and pause is pressed.
                gameState = 0;
                hideScore();
            }
        }
        if (e.getCode() == KeyCode.R)
            reset();
        if (e.getCode() == KeyCode.ALT)
            toggleGhosts();
        if (e.getCode() == KeyCode.Q)
            end();
    };
    public void start(Stage primaryStage) {
        initialize(); //called at program start
        new AnimationTimer() {public void handle(long time) {
            doGameLoop(); //called multiple times per second
        }}.start();
    }
    public static boolean[][] getBackgroundData(){ return  backgroundData; }
}