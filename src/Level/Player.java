package Level;

import Engine.Key;
import Engine.KeyLocker;
import Engine.Keyboard;
import Engine.PlayMusic;
import GameObject.GameObject;
import GameObject.SpriteSheet;
import Utils.AirGroundState;
import Utils.Direction;
import Level.Inventory;
import java.util.Timer;
import java.util.TimerTask;
import java.util.List;

import java.io.IOException;
import java.util.ArrayList;

import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public abstract class Player extends GameObject {

    // values that affect player movement
    // these should be set in a subclass
    protected float walkSpeed = 0;
    protected float gravity = 0;
    protected float jumpHeight = 0;
    protected float jumpDegrade = 0;
    protected float terminalVelocityY = 0;
    protected float momentumYIncrease = 0;
    protected PlayMusic backgroundMusic;
    protected float sprintSpeedMultiplier = 2.0f;
    protected boolean isSprinting = false;
    protected int currentMap;
    protected long sprintEndTime = 0;
    private boolean canDoubleJump = false; // indicates if the player has the ability to double jump
    private boolean hasDoubleJumped = false; // indicates if the player has already double jumped

    // values used to handle player movement
    protected float jumpForce = 0;
    protected float momentumY = 0;
    protected float moveAmountX, moveAmountY;
    protected float lastAmountMovedX, lastAmountMovedY;

    // values used to keep track of player's current state
    protected PlayerState playerState;
    protected PlayerState previousPlayerState;
    protected Direction facingDirection;
    protected AirGroundState airGroundState;
    protected AirGroundState previousAirGroundState;
    protected LevelState levelState;

    // classes that listen to player events can be added to this list
    protected ArrayList<PlayerListener> listeners = new ArrayList<>();

    // define keys
    protected KeyLocker keyLocker = new KeyLocker();
    protected Key JUMP_KEY = Key.UP;
    protected Key MOVE_LEFT_KEY = Key.LEFT;
    protected Key MOVE_RIGHT_KEY = Key.RIGHT;
    protected Key CROUCH_KEY = Key.DOWN;

    // flags
    protected boolean isInvincible = false; // if true, player cannot be hurt by enemies (good for testing)

    private Inventory inventory;

    public Player(SpriteSheet spriteSheet, float x, float y, String startingAnimationName, int currentMap) {
        super(spriteSheet, x, y, startingAnimationName);
        this.currentMap = currentMap;
        facingDirection = Direction.RIGHT;
        airGroundState = AirGroundState.AIR;
        previousAirGroundState = airGroundState;
        playerState = PlayerState.STANDING;
        previousPlayerState = playerState;
        levelState = LevelState.RUNNING;
        this.inventory = new Inventory(3);

        String backgroundMusicPath = "TheJourneyBegins.wav"; // Replace with your actual music file path

        if (this.currentMap == 1) {
            backgroundMusicPath = "TheJourneyBegins.wav";
        } else if (this.currentMap == 2) {
            backgroundMusicPath = "TimeWindow.wav";
        }

        try {
            backgroundMusic = new PlayMusic(backgroundMusicPath);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void updateDoubleJump() {
        this.canDoubleJump = true;
    }

    private Level1 currentLevel; // assuming you have access to your current level in the Player class

    public void pauseMusic() {
        currentLevel.getAudioManager().stopSound();
    }

    public void resumeMusic() {
        currentLevel.getAudioManager().playSound();
    }

    public void stopMusic() {
        currentLevel.getAudioManager().stopSound();
    }

    public void pauseMusic2() {
        currentLevel.getAudioManager().stopSound();
    }

    public void resumeMusic2() {
        currentLevel.getAudioManager().playSound();
    }

    public void stopMusic2() {
        currentLevel.getAudioManager().stopSound();
    }
    // ... Rest of your Player class methods ...

    public void update() {

        moveAmountX = 0;
        moveAmountY = 0;

        // if player is currently playing through level (has not won or lost)
        if (levelState == LevelState.RUNNING) {
            applyGravity();

            // update player's state and current actions, which includes things like
            // determining how much it should move each frame and if its walking or jumping
            do {
                previousPlayerState = playerState;
                handlePlayerState();
            } while (previousPlayerState != playerState);

            previousAirGroundState = airGroundState;

            // move player with respect to map collisions based on how much player needs to
            // move this frame
            lastAmountMovedX = super.moveXHandleCollision(moveAmountX);
            lastAmountMovedY = super.moveYHandleCollision(moveAmountY);

            handlePlayerAnimation();

            updateLockedKeys();

            // update player's animation
            super.update();

        }

        // if player has beaten level
        else if (levelState == LevelState.LEVEL_COMPLETED) {
            updateLevelCompleted();
        }

        // if player has lost level
        else if (levelState == LevelState.PLAYER_DEAD) {
            updatePlayerDead();
        }

        // if player has beaten level
        else if (levelState == LevelState.PREVIOUSLEVEL) {
            updatePREVIOUSLEVEL();
        }
    }

    private void updatePREVIOUSLEVEL() {
    }

    // add gravity to player, which is a downward force
    protected void applyGravity() {
        moveAmountY += gravity + momentumY;
    }

    // based on player's current state, call appropriate player state handling
    // method
    protected void handlePlayerState() {
        switch (playerState) {
            case STANDING:
                playerStanding();
                break;
            case WALKING:
                playerWalking();
                break;
            case CROUCHING:
                playerCrouching();
                break;
            case JUMPING:
                playerJumping();
                break;
        }
    }

    // player STANDING state logic
    protected void playerStanding() {
        // if walk left or walk right key is pressed, player enters WALKING state
        if (Keyboard.isKeyDown(MOVE_LEFT_KEY) || Keyboard.isKeyDown(MOVE_RIGHT_KEY)) {
            playerState = PlayerState.WALKING;
        }

        // if jump key is pressed, player enters JUMPING state
        else if (Keyboard.isKeyDown(JUMP_KEY) && !keyLocker.isKeyLocked(JUMP_KEY)) {
            keyLocker.lockKey(JUMP_KEY);
            playerState = PlayerState.JUMPING;
        }

        // if crouch key is pressed, player enters CROUCHING state
        else if (Keyboard.isKeyDown(CROUCH_KEY)) {
            playerState = PlayerState.CROUCHING;
        }
    }

    // player WALKING state logic
    protected void playerWalking() {
        // if walk left key is pressed, move player to the left
        float speed = isSprinting ? walkSpeed * sprintSpeedMultiplier : walkSpeed;

        if (Keyboard.isKeyDown(MOVE_LEFT_KEY)) {
            moveAmountX -= speed;
            facingDirection = Direction.LEFT;
        } else if (Keyboard.isKeyDown(MOVE_RIGHT_KEY)) {
            moveAmountX += speed;
            facingDirection = Direction.RIGHT;
        }

        // if walk right key is pressed, move player to the right
        else if (Keyboard.isKeyDown(MOVE_RIGHT_KEY)) {
            moveAmountX += walkSpeed;
            facingDirection = Direction.RIGHT;
        } else if (Keyboard.isKeyUp(MOVE_LEFT_KEY) && Keyboard.isKeyUp(MOVE_RIGHT_KEY)) {
            playerState = PlayerState.STANDING;
        }

        // if jump key is pressed, player enters JUMPING state
        if (Keyboard.isKeyDown(JUMP_KEY) && !keyLocker.isKeyLocked(JUMP_KEY)) {
            keyLocker.lockKey(JUMP_KEY);
            playerState = PlayerState.JUMPING;
        }

        // if crouch key is pressed,
        else if (Keyboard.isKeyDown(CROUCH_KEY)) {
            playerState = PlayerState.CROUCHING;
        }
    }

    public void activateSprint(int duration) {
        isSprinting = true;
        sprintEndTime = System.currentTimeMillis() + duration;
    }

    // player CROUCHING state logic
    protected void playerCrouching() {
        // if crouch key is released, player enters STANDING state
        if (Keyboard.isKeyUp(CROUCH_KEY)) {
            playerState = PlayerState.STANDING;
        }

        // if jump key is pressed, player enters JUMPING state
        if (Keyboard.isKeyDown(JUMP_KEY) && !keyLocker.isKeyLocked(JUMP_KEY)) {
            keyLocker.lockKey(JUMP_KEY);
            playerState = PlayerState.JUMPING;
        }
    }

    protected void playerJumping() {
        if (previousAirGroundState == AirGroundState.GROUND) {
            hasDoubleJumped = false; // Reset double jump when on ground

            // sets animation to a JUMP animation based on which way player is facing
            currentAnimationName = facingDirection == Direction.RIGHT ? "JUMP_RIGHT" : "JUMP_LEFT";

            // player is set to be in air and then player is sent into the air
            airGroundState = AirGroundState.AIR;
            jumpForce = jumpHeight;
            if (jumpForce > 0) {
                moveAmountY -= jumpForce;
                jumpForce -= jumpDegrade;
                if (jumpForce < 0) {
                    jumpForce = 0;
                }
            }

        } else if (airGroundState == AirGroundState.AIR) {

            if (!hasDoubleJumped && canDoubleJump && Keyboard.isKeyDown(JUMP_KEY) && !keyLocker.isKeyLocked(JUMP_KEY)) {
                jumpForce = jumpHeight;
                hasDoubleJumped = true;
                keyLocker.lockKey(JUMP_KEY); // Lock the key after the double jump
            }

            if (jumpForce > 0) {
                moveAmountY -= jumpForce;
                jumpForce -= jumpDegrade;
                if (jumpForce < 0) {
                    jumpForce = 0;
                }
            }

            // allows you to move left and right while in the air
            if (Keyboard.isKeyDown(MOVE_LEFT_KEY)) {
                moveAmountX -= walkSpeed;
            } else if (Keyboard.isKeyDown(MOVE_RIGHT_KEY)) {
                moveAmountX += walkSpeed;
            }

            // if player is falling, increases momentum as player falls so it falls faster
            // over time
            if (moveAmountY > 0) {
                increaseMomentum();
            }
        }

        // if player last frame was in air and this frame is now on ground, player
        // enters STANDING state
        else if (previousAirGroundState == AirGroundState.AIR && airGroundState == AirGroundState.GROUND) {
            playerState = PlayerState.STANDING;
        }
    }

    // while player is in air, this is called, and will increase momentumY by a set
    // amount until player reaches terminal velocity
    protected void increaseMomentum() {
        momentumY += momentumYIncrease;
        if (momentumY > terminalVelocityY) {
            momentumY = terminalVelocityY;
        }
    }

    protected void updateLockedKeys() {
        if (Keyboard.isKeyUp(JUMP_KEY)) {
            keyLocker.unlockKey(JUMP_KEY);
        }
    }

    // anything extra the player should do based on interactions can be handled here
    protected void handlePlayerAnimation() {
        if (playerState == PlayerState.STANDING) {
            // sets animation to a STAND animation based on which way player is facing
            this.currentAnimationName = facingDirection == Direction.RIGHT ? "STAND_RIGHT" : "STAND_LEFT";

            // handles putting goggles on when standing in water
            // checks if the center of the player is currently touching a water tile
            int centerX = Math.round(getBounds().getX1()) + Math.round(getBounds().getWidth() / 2f);
            int centerY = Math.round(getBounds().getY1()) + Math.round(getBounds().getHeight() / 2f);
            MapTile currentMapTile = map.getTileByPosition(centerX, centerY);
            if (currentMapTile != null && currentMapTile.getTileType() == TileType.WATER) {
                this.currentAnimationName = facingDirection == Direction.RIGHT ? "SWIM_STAND_RIGHT" : "SWIM_STAND_LEFT";
            }
        } else if (playerState == PlayerState.WALKING) {
            // sets animation to a WALK animation based on which way player is facing
            this.currentAnimationName = facingDirection == Direction.RIGHT ? "WALK_RIGHT" : "WALK_LEFT";
        } else if (playerState == PlayerState.CROUCHING) {
            // sets animation to a CROUCH animation based on which way player is facing
            this.currentAnimationName = facingDirection == Direction.RIGHT ? "CROUCH_RIGHT" : "CROUCH_LEFT";
        } else if (playerState == PlayerState.JUMPING) {
            // if player is moving upwards, set player's animation to jump. if player moving
            // downwards, set player's animation to fall
            if (lastAmountMovedY <= 0) {
                this.currentAnimationName = facingDirection == Direction.RIGHT ? "JUMP_RIGHT" : "JUMP_LEFT";
            } else {
                this.currentAnimationName = facingDirection == Direction.RIGHT ? "FALL_RIGHT" : "FALL_LEFT";
            }
        }
    }

    @Override
    public void onEndCollisionCheckX(boolean hasCollided, Direction direction, MapEntity entityCollidedWith) {
    }

    @Override
    public void onEndCollisionCheckY(boolean hasCollided, Direction direction, MapEntity entityCollidedWith) {
        // if player collides with a map tile below it, it is now on the ground
        // if player does not collide with a map tile below, it is in air
        if (direction == Direction.DOWN) {
            if (hasCollided) {
                momentumY = 0;
                airGroundState = AirGroundState.GROUND;
            } else {
                playerState = PlayerState.JUMPING;
                airGroundState = AirGroundState.AIR;
            }
        }

        // if player collides with map tile upwards, it means it was jumping and then
        // hit into a ceiling -- immediately stop upwards jump velocity
        else if (direction == Direction.UP) {
            if (hasCollided) {
                jumpForce = 0;
            }
        }
    }

    // other entities can call this method to hurt the player
    public void hurtPlayer(MapEntity mapEntity) {
        if (!isInvincible) {
            // if map entity is an enemy, kill player on touch
            if (mapEntity instanceof Enemy) {
                levelState = LevelState.PLAYER_DEAD;
            }
        }
    }

    // other entities can call this to tell the player they beat a level
    public void completeLevel() {
        levelState = LevelState.LEVEL_COMPLETED;

    }

    // if player has beaten level, this will be the update cycle
    public void updateLevelCompleted() {
        try {
            // Stop the background music
            backgroundMusic.stop();

            // if player is not on ground, player should fall until it touches the ground
            if (airGroundState != AirGroundState.GROUND && map.getCamera().containsDraw(this)) {
                currentAnimationName = "FALL_RIGHT";
                applyGravity();
                increaseMomentum();
                super.update();
                moveYHandleCollision(moveAmountY);
            }
            // move player to the right until it walks off screen
            else if (map.getCamera().containsDraw(this)) {
                currentAnimationName = "WALK_RIGHT";
                super.update();
                moveXHandleCollision(walkSpeed);
            } else {
                // tell all player listeners that the player has finished the level
                for (PlayerListener listener : listeners) {
                    listener.onLevelCompleted();
                }
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
            // Optionally, you can log the error or handle it in some other way.
        }
    }

    public void previousLevel() {
        levelState = LevelState.PREVIOUSLEVEL;

    }

    // if player has beaten level, this will be the update cycle
    public void updatePreviousLevel() {
        try {
            // Stop the background music
            backgroundMusic.stop();

            // if player is not on ground, player should fall until it touches the ground
            if (airGroundState != AirGroundState.GROUND && map.getCamera().containsDraw(this)) {
                currentAnimationName = "FALL_LEFT";
                applyGravity();
                increaseMomentum();
                super.update();
                moveYHandleCollision(moveAmountY);
            }
            // move player to the right until it walks off screen
            else if (map.getCamera().containsDraw(this)) {
                currentAnimationName = "WALK_LEFT";
                super.update();
                moveXHandleCollision(walkSpeed);
            } else {
                // tell all player listeners that the player has finished the level
                for (PlayerListener listener : listeners) {
                    listener.onPreviousLevel();
                }
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
            // Optionally, you can log the error or handle it in some other way.
        }
    }

    private void fadeOutMusic() {
        if (backgroundMusic != null && backgroundMusic.getClip() != null) {
            Clip clip = (Clip) backgroundMusic.getClip();
            FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float range = volumeControl.getMaximum() - volumeControl.getMinimum();
            float targetVolume = volumeControl.getMinimum();
            long fadeDuration = 5000; // 5 seconds

            new Thread(() -> {
                for (float v = volumeControl.getValue(); v > targetVolume; v -= range / (fadeDuration / 100)) {
                    volumeControl.setValue(v);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                clip.stop();
            }).start();
        }
    }

    public void updatePlayerDead() {
        try {
            // Stop the background music
            backgroundMusic.stop();

            // change player animation to DEATH
            if (!currentAnimationName.startsWith("DEATH")) {
                if (facingDirection == Direction.RIGHT) {
                    currentAnimationName = "DEATH_RIGHT";
                } else {
                    currentAnimationName = "DEATH_LEFT";
                }
                super.update();
            }
            // if death animation not on last frame yet, continue to play out death
            // animation
            else if (currentFrameIndex != getCurrentAnimation().length - 1) {
                super.update();
            }
            // if death animation on last frame (it is set up not to loop back to start),
            // player should continually fall until it goes off screen
            else if (currentFrameIndex == getCurrentAnimation().length - 1) {
                if (map.getCamera().containsDraw(this)) {
                    moveY(3);
                } else {
                    // tell all player listeners that the player has died in the level
                    for (PlayerListener listener : listeners) {
                        listener.onDeath();
                    }
                }
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
            // Optionally, you can log the error or handle it in some other way.
        }
    }

    public void activateTemporaryPowerUp(String powerUpName) {
        if (addItemToInventory(powerUpName)) {
            switch (powerUpName) {
                case "DoubleJump":
                    this.canDoubleJump = true; // Enable double jump
                    break;
                case "Sprint":
                    this.isSprinting = true; // Enable sprint
                    this.walkSpeed *= this.sprintSpeedMultiplier; // Increase walking speed
                    break;
            }

            // Set a timer to remove the power-up from inventory and deactivate it after 15
            // seconds
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    removeItemFromInventory(powerUpName);
                    deactivatePowerUp(powerUpName);
                }
            }, 15000); // 15000 milliseconds = 15 seconds
        }
    }

    private void deactivatePowerUp(String powerUpName) {
        switch (powerUpName) {
            case "DoubleJump":
                this.canDoubleJump = false; // Disable double jump
                System.out.println("Double jump deactivated.");
                break;
            case "Sprint":
                this.isSprinting = false; // Disable sprint
                this.walkSpeed /= this.sprintSpeedMultiplier; // Reset walking speed
                System.out.println("Sprint deactivated.");
                break;
        }
    }

    public PlayerState getPlayerState() {
        return playerState;
    }

    public void setPlayerState(PlayerState playerState) {
        this.playerState = playerState;
    }

    public AirGroundState getAirGroundState() {
        return airGroundState;
    }

    public Direction getFacingDirection() {
        return facingDirection;
    }

    public void setFacingDirection(Direction facingDirection) {
        this.facingDirection = facingDirection;
    }

    public void setLevelState(LevelState levelState) {
        this.levelState = levelState;
    }

    public void addListener(PlayerListener listener) {
        listeners.add(listener);
    }

    // Add a method to add an item to the inventory
    public boolean addItemToInventory(String item) {
        return inventory.addItem(item);
    }

    // Add a method to check if the inventory contains an item
    public boolean hasItemInInventory(String item) {
        return inventory.hasItem(item);
    }

    // Add a method to remove an item from the inventory
    public boolean removeItemFromInventory(String item) {
        return inventory.removeItem(item);
    }

    // Add a method to get the inventory items as a List (if needed for drawing or
    // other purposes)
    public List<String> getInventoryItems() {
        return inventory.getItems();
    }

    public void PreviousLevel() {
        return;
    }
}
