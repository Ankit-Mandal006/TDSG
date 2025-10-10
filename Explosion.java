public class Explosion {
    public double x, y;
    public int duration = 24;
    public int age = 0;

    public Explosion(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public boolean isAlive() {
        return age < duration;
    }
}
