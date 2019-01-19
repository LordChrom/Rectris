public class Tetromino {
    private static final char[] pieceNames = {'I','J','L','O','S','T','Z'};

    private final Position[] positions = new Position[4];       //positions of pieces
    private final Position[] testPositions = new Position[4];   //when moving, testPositions are updated, then collision is checked before real ones are
    private final Square[] squares = new Square[4];             //the square objects
    private char type;                                          //type of piece
    private Tetromino ghost;                                    //transparent projection of where piece will land
    private int[][][] pieceData;                                //holds data for piece depending on type
    private int rotation, testRotation, x, y,frameX, frameY;    //rotation, offset from origin, and the xy position of the stage
    private boolean landed, spawnedLanded, isGhostly, hasGhost; //various flags

    //The following arrays are piece data.
    //each piece is stored in different pages for each rotation position.
    //each page is a set of 4 x y points for where on the field to put the squares relative to the piece's center.
    private final static int[][][] I = {{{-2,0},{-1,0},{0,0},{1,0}},{{0,-1},{0,0},{0,1},{0,2}}};
    private final static int[][][] J = {{{-1,0},{0,0},{1,0},{1,1}},{{0,-1},{1,-1},{0,0},{0,1}},{{-1,-1},{-1,0},{0,0},{1,0}},{{0,-1},{0,0},{-1,1},{0,1}}};
    private final static int[][][] L = {{{-1,0},{0,0},{1,0},{-1,1}},{{0,-1},{0,0},{0,1},{1,1}},{{1,-1},{-1,0},{0,0},{1,0}},{{-1,-1},{0,-1},{0,0},{0,1}}};
    private final static int[][][] O = {{{-1,0},{0,0},{-1,1},{0,1}}};
    private final static int[][][] S = {{{0,0},{1,0},{-1,1},{0,1}},{{0,-1},{0,0},{1,0},{1,1}}};
    private final static int[][][] T = {{{-1,0},{0,0},{1,0},{0,1}},{{0,-1},{0,0},{1,0},{0,1}},{{0,-1},{-1,0},{0,0},{1,0}},{{0,-1},{-1,0},{0,0},{0,1}}};
    private final static int[][][] Z = {{{-1,0},{0,0},{0,1},{1,1}},{{1,-1},{0,0},{1,0},{0,1}}};
//    private final static int[][][] I = {{{-1,0},{0,0},{1,0},{2,0}},{{0,-1},{0,0},{0,1},{0,2}}};
//    private final static int[][][] J = {{{-1,-1},{-1,0},{0,0},{1,0}},{{0,-1},{0,0},{0,1},{-1,1}}, {{-1,0},{0,0},{1,0},{1,1}},{{1,-1},{0,-1},{0,0},{0,1}}};
//    private final static int[][][] L = {{{-1,0},{0,0},{1,0},{1,-1}},{{-1,-1},{0,-1},{0,0},{0,1}}, {{-1,1},{-1,0},{0,0},{1,0}},{{0,-1},{0,0},{0,1},{1,1}}};
//    private final static int[][][] O = {{{0,0},{0,1},{1,1},{1,0}}};
//    private final static int[][][] S = {{{-1,0},{0,0},{0,-1},{1,-1}},{{-1,-1},{-1,0},{0,0},{0,1}}};
//    private final static int[][][] T = {{{0,0},{-1,0},{1,0},{0,-1}},{{0,0},{-1,0},{0,-1},{0,1}}, {{0,0},{-1,0},{1,0},{0,1}},{{0,0},{1,0},{0,-1},{0,1}}};
//    private final static int[][][] Z = {{{-1,-1},{0,-1},{0,0},{1,0}},{{1,-1},{1,0},{0,0},{0,1}}};

    public Tetromino(){ setPiece(randomType()); }
//    public Tetromino(char type,int x, int y){ setPiece(type,x,y); }
    public Tetromino(int x, int y){ setPiece(randomType(),x,y);}
    public Tetromino(char type){ setPiece(type);}
    public String toString(){
        String output = ""+type+" tetromino";
        if(isGhostly)
            output="ghostly "+output;
        if(spawnedLanded)
            output="spawnedLanded "+output;
        return output;
    }
    private static char randomType(){ //returns a random piece type char. Math.random() wasn't evenly distributed enough.
        int index = (int)(Math.random()*7);
        return pieceNames[index];
    }

    private void setPiece(char type){ setPiece(type, Main.stageWidth/2, 0); } //default xy is the middle of the screen, top
    private void setPiece(char type, int x, int y) {
        this.x = x;
        this.y = y;
        spawnedLanded = false;
        rotation = 0;
        testRotation = 0;
        this.type = type;
        landed = false;
        switch (type) {
            case 'I':
                pieceData = I;
                break;
            case 'J':
                pieceData = J;
                break;
            case 'L':
                pieceData = L;
                break;
            case 'O':
                pieceData = O;
                break;
            case 'S':
                pieceData = S;
                break;
            case 'T':
                pieceData = T;
                break;
            case 'Z':
                pieceData = Z;
                break;
            default:
                pieceData = I; //if, someone makes an invalid piece, it'll spawn as the best piece
                this.type = 'I';
        }
        int[][] coords = pieceData[0]; //initial xy points for squares
        for (int i = 0; i < 4; i++) {
            positions[i] = new Position(coords[i][0]+x, coords[i][1]+y);
            testPositions[i] = new Position(coords[i][0]+x, coords[i][1]+y);
        }
        for (int i = 0; i < 4; i++)
            squares[i] = new Square(0,0); //initializes squares
        updateSquarePositions();
        if(testCollision()) //detects if the piece was obstructed during spawning AKA game over
            spawnedLanded = true;
        move();
    }

    private void move(){    //after test positions are moved, if there's no collision,
                            //this will be called to finalize the move by updating the real positions to the test ones
        for (int i = 0; i < 4; i++)
            positions[i].set(testPositions[i]);
        if(hasGhost)    //updates the ghost's projection also
            updateGhost();
        rotation = testRotation;
        updateSquarePositions();
    }
    private void resetTestPos(){    //sets the test position to match the the real ones
        for (int i = 0; i < 4; i++)
            testPositions[i].set(positions[i]);
        testRotation=rotation;
    }
    public void moveDown(){
        resetTestPos();
        for(Position pos: testPositions)
            pos.down();
        if(testCollision()) {
            resetTestPos();
            landed=true;
        }else{
            move();
            y++;
        }
    }
    public void moveLeft(){
        resetTestPos();
        for(Position pos: testPositions)
            pos.left();
        if(testCollision()) {
            resetTestPos();
        }else{
            move();
            x--;
        }
    }
    public void moveRight(){
        resetTestPos();
        for(Position pos: testPositions)
            pos.right();
        if(testCollision()) {
            resetTestPos();
        }else{
            move();
            x++;
        }
    }
    public void hardDown(){
        while(!landed)
            moveDown();
    }
    public void rotateClockwise(){
        testRotation--;
        updateRotation();
    }
    public void rotateCounterClockwise() {
        testRotation++;
        updateRotation();
    }
    private void updateRotation(){
        while(testRotation<0)//keeps rotation positive
            testRotation+=4;
        testRotation%=pieceData.length; //keeps rotation in bounds
        int[][] coords = pieceData[testRotation]; //gets the piece data from the rotation's page
        for (int i = 0; i < 4; i++)
            testPositions[i] = new Position(coords[i][0]+ x, coords[i][1]+ y);
        if (testCollision()) {
            if(y==0){                   //collision detected, BUT the piece is in the top row, and there's likely collision with the ceiling. Try moving it down
                for(Position pos: testPositions)
                    pos.down();
                if(testCollision()) {   //moving it down didn't work, collision not with ceiling, rotation can't happen.
                    resetTestPos();
                }else{                  //it worked! Thanks to this you can rotate any piece as soon as it spawns.
                    y++;
                    move();
                }
            }else { //collision detected, not row 0, no room to rotate
                resetTestPos();
            }
        } else { //no problems with rotation
            move();
        }
    }

    private boolean testCollision(){
        boolean collisionDetected = false;
        boolean[][] stage = Main.getBackgroundData();   //gets the data about the stage from the Main method
        for(Position pos: testPositions) {  //runs for every test position
            if(pos.inBounds(Main.stageWidth-1,Main.stageHeight-1)) { //if the position is in bounds
                if (stage[pos.x][pos.y])    //if the tile is full
                    collisionDetected = true;   //collision detected
            }else{ //if the position is out of bounds, collision detected
                collisionDetected = true;
            }
        }
        return collisionDetected;
    }
    public void updateSquarePositions(){
        for (int i = 0; i < 4; i++) {
            Position pos = positions[i];
            squares[i].setGPos(pos);
            squares[i].setXY(frameX + pos.x*Square.size, frameY + pos.y*Square.size);   //uses same math as the main method
        }
    }
    public void setFrameXY(int x, int y){   //updates knowledge of the position of the stage
        frameX = x;
        frameY = y;
        updateSquarePositions();            //moves squares into place
        if(hasGhost)                        //if the piece has a ghost move that too
            ghost.setFrameXY(x,y);
    }
    public void kill(){
        for (Square sqr : squares)
            sqr.kill();
        if(hasGhost)
            ghost.kill();
    }

    private void ghostify(){    //sets piece as a ghost, makes it transparent and sets a flag
        for(Square sqr: squares)
            sqr.setOpacity(0.4);
        isGhostly =true;
    }
    private void copyCat(Tetromino base){   //copies the positions of a different tetromino, used for ghosts
        for(int i=0;i<4;i++)
            positions[i].set(base.getSquarePositions()[i]);
        resetTestPos();
        updateSquarePositions();
        landed=base.hasLanded();
    }
    public void createGhost(){  //creates a ghost, if it doesn't already have one
        if(!hasGhost) {
            ghost = new Tetromino(type);
            ghost.ghostify();
            hasGhost = true;
            updateGhost();
        }
    }
    public void killGhost(){    //if there is a ghost, gets rid of it.
        if(hasGhost) {
            ghost.kill();
            hasGhost=false;
        }
    }
    private void updateGhost(){ //makes the ghost mimic this piece and where it'd be if it fell
        ghost.copyCat(this);
        ghost.hardDown();
    }

    public boolean spawnedLanded(){return spawnedLanded;}
    public boolean hasLanded(){return landed;}
    public char getType(){ return type;}
    public Position[] getSquarePositions(){ return positions; }
}