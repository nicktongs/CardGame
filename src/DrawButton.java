public class DrawButton extends ClickableRectangle {
    public DrawButton(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
    }

    public String getLabel() {
        return "Draw";
    }
}
