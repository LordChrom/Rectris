public class AppNumber {
    private Rect[] windows = new Rect[0];
    public void show(int x, int y, int number) {
        hide();
        String scoreText = String.valueOf(number);
        for (int i = 0; i < scoreText.length(); i++) {
            char digit = scoreText.charAt(i);
            switch (digit) {
                case '0':
                    addRect(x, y, 100, 20);
                    addRect(x, y + 80, 100, 20);
                    addRect(x, y, 20, 100);
                    addRect(x + 80, y, 20, 100);
                    break;
                case '1':
                    addRect(x + 40, y, 20, 100);
                    break;
                case '2':
                    addRect(x + 80, y, 20, 60);
                    addRect(x, y + 40, 20, 60);
                    addRect(x, y, 100, 20);
                    addRect(x, y + 40, 100, 20);
                    addRect(x, y + 80, 100, 20);
                    break;
                case '3':
                    addRect(x + 80, y, 20, 100);
                    addRect(x, y, 100, 20);
                    addRect(x, y + 40, 100, 20);
                    addRect(x, y + 80, 100, 20);
                    break;
                case '4':
                    addRect(x, y + 40, 100, 20);
                    addRect(x, y, 20, 60);
                    addRect(x + 80, y, 20, 100);
                    break;
                case '5':
                    addRect(x, y, 20, 60);
                    addRect(x + 80, y + 40, 20, 60);
                    addRect(x, y, 100, 20);
                    addRect(x, y + 40, 100, 20);
                    addRect(x, y + 80, 100, 20);
                    break;
                case '6':
                    addRect(x, y, 100, 20);
                    addRect(x, y + 40, 100, 20);
                    addRect(x, y + 80, 100, 20);
                    addRect(x, y, 20, 100);
                    addRect(x + 80, y + 40, 20, 60);
                    break;
                case '7':
                    addRect(x + 80, y, 20, 100);
                    addRect(x, y, 100, 20);
                    break;
                case '8':
                    addRect(x, y, 100, 20);
                    addRect(x, y + 40, 100, 20);
                    addRect(x, y + 80, 100, 20);
                    addRect(x, y, 20, 100);
                    addRect(x + 80, y, 20, 100);
                    break;
                case '9':
                    addRect(x, y, 100, 20);
                    addRect(x, y + 40, 100, 20);
                    addRect(x, y + 80, 100, 20);
                    addRect(x, y, 20, 60);
                    addRect(x + 80, y, 20, 100);
                    break;
                default:
            }
            x += 120;
        }
    }
    public void hide(){
        for(Rect rect: windows)
            rect.kill();
        windows = new Rect[0];
        System.gc();
    }
    private void addRect(int x, int y, int width, int height){
        Rect[] newWindows = new Rect[windows.length+1];
        System.arraycopy(windows,0,newWindows,0,windows.length);
        newWindows[windows.length] = new Rect(width, height, x, y);
        windows = newWindows;
    }
}