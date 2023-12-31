package Level;

import GameObject.Frame;

import java.util.HashMap;

// This class is a base class for all enemies in the game -- all enemies should extend from it
public class Enemy2 extends MapEntity {



    public Enemy2(float x, float y, HashMap<String, Frame[]> animations, String startingAnimation) {
        super(x, y, animations, startingAnimation);
    }

    public Enemy2(float x, float y, Frame[] frames) {
        super(x, y, frames);
    }

    public Enemy2(float x, float y, Frame frame) {
        super(x, y, frame);
    }

    public Enemy2(float x, float y) {
        super(x, y);
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    public void update(Player player) {
        super.update();
        if (intersects(player)) {
            touchedPlayer(player);
        }
    }

    // A subclass can override this method to specify what it does when it touches the player
    public void touchedPlayer(Player player) {
        player.hurtPlayer(this);
    }
}
