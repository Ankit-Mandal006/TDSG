import java.awt.Point;
import java.util.Random;

public class WaveManager {
    private int currentWave = 1;
    private int bossesDefeated = 0;
    private Random random = new Random();

    public int getCurrentWave() {
        return currentWave;
    }

    public int getBossesDefeated() {
        return bossesDefeated;
    }

    public void startWave(int wave, GamePanel game) {
        currentWave = wave;

        // Clear enemies and bosses for new wave
        game.enemies.clear();
        game.bosses.clear();

        switch (currentWave) {
            case 1:
                spawnEnemies(game, Enemy.class, 5);
                break;
            case 2:
                spawnEnemies(game, Enemy.class, 20);
                break;
            case 3:
                spawnEnemies(game, Enemy.class, 30);
                spawnEnemies(game, FastChargerEnemy.class, 3);
                break;
            case 4:
                spawnBoss(game);
                spawnEnemies(game, Enemy.class, 8);
                break;
            case 5:
                spawnEnemies(game, RangedShooterEnemy.class, 16);
                spawnEnemies(game, Enemy.class, 5);
                break;
            case 6:
                spawnEnemies(game, RangedShooterEnemy.class, 16);
                spawnEnemies(game, Enemy.class, 25);
                spawnEnemies(game, FastChargerEnemy.class, 5);
                break;
            case 7:
                spawnEnemies(game, RangedShooterEnemy.class, 20);
                spawnEnemies(game, Enemy.class, 15);
                spawnEnemies(game, FastChargerEnemy.class, 10);
                break;
            case 8:
                spawnBoss(game);
                spawnBoss(game);
                spawnEnemies(game, Enemy.class, 8);
                spawnEnemies(game, FastChargerEnemy.class, 6);
                spawnEnemies(game, RangedShooterEnemy.class, 6);
                break;
            case 9:
                spawnEnemies(game, RangedShooterEnemy.class, 25);
                spawnEnemies(game, Enemy.class, 25);
                spawnEnemies(game, FastChargerEnemy.class, 20);
                break;
            default:
                spawnEnemies(game, Enemy.class, 10 + currentWave * 2);
                spawnEnemies(game, RangedShooterEnemy.class, 5 + currentWave * 2);
                spawnEnemies(game, FastChargerEnemy.class, 3 + currentWave * 2);
                if (currentWave % 3 == 0) {
                    spawnBoss(game);
                }if (currentWave % 6 == 0) {
                    spawnBoss(game);
                }
                if (currentWave % 9 == 0) {
                    spawnBoss(game);
                }
                break;
        }
    }

    public void updateWave(GamePanel game) {
        boolean allBossesDead = !game.bosses.isEmpty() && game.bosses.stream().allMatch(Boss::isDead);

        if (game.enemies.isEmpty() && (game.bosses.isEmpty() || allBossesDead)) {
            if (allBossesDead) {
                bossesDefeated += game.bosses.size();
            }
            startWave(currentWave + 1, game);
        }
    }

    private void spawnEnemies(GamePanel game, Class<? extends Enemy> enemyClass, int count) {
        for (int i = 0; i < count; i++) {
            Point pos = getRandomOffscreenPosition();
            Enemy enemy = createEnemyInstance(enemyClass, pos.x, pos.y);
            if (enemy != null) {
                game.enemies.add(enemy);
            }
        }
    }


    private Enemy createEnemyInstance(Class<? extends Enemy> enemyClass, int x, int y) {
        try {
            return enemyClass.getDeclaredConstructor(int.class, int.class).newInstance(x, y);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void spawnBoss(GamePanel game) {
        Point bossPoint = Boss.getRandomSpawnPoint();
        game.bosses.add(new Boss(bossPoint.x, bossPoint.y));
    }

    private Point getRandomOffscreenPosition() {
        int edge = random.nextInt(4); // 0=left, 1=right, 2=top, 3=bottom
        int x, y;

        switch (edge) {
            case 0: // left
                x = -50; // offscreen left
                y = random.nextInt(GamePanel.HEIGHT);
                break;
            case 1: // right
                x = GamePanel.WIDTH + 50; // offscreen right
                y = random.nextInt(GamePanel.HEIGHT);
                break;
            case 2: // top
                x = random.nextInt(GamePanel.WIDTH);
                y = -50; // offscreen top
                break;
            case 3: // bottom
                x = random.nextInt(GamePanel.WIDTH);
                y = GamePanel.HEIGHT + 50; // offscreen bottom
                break;
            default:
                x = -50;
                y = -50;
                break;
        }

        return new Point(x, y);
    }


}
