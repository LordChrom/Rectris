public class Position {
    public int x,y;
    public Position(int x,int y){
        this.x=x;
        this.y=y;
    }

    public void set(Position p){
        x=p.x;
        y=p.y;
    }

    public void down(){ y++; }
    public void left(){ x--; }
    public void right(){ x++; }

    public boolean inBounds(int x, int y){
        return (this.x>=0 && this.y>=0) && (this.x<=x && this.y<=y);
    }
    public String toString(){
        return "("+x+","+y+")";
    }
}