package no.sandramoen.commanderqueen.actors.characters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

import no.sandramoen.commanderqueen.actors.utils.baseActors.BaseActor3D;
import no.sandramoen.commanderqueen.utils.BaseGame;
import no.sandramoen.commanderqueen.utils.Stage3D;

public class Player extends BaseActor3D {
    public boolean isMoving;
    public static float movementSpeed = 10f;

    private float rotateSpeed = 90f * .05f;
    private float totalTime = 0;
    private Stage3D stage3D;
    private Stage stage;

    private float bobFrequency = 4;
    private float bobAmount = .015f;
    private float bobCounter = 0;

    private boolean isForcedToMove;
    private float forceMoveY = movementSpeed;
    private float forceMoveZ = movementSpeed;
    private float forceTime;
    private final float SECONDS_FORCED_TO_MOVE = .25f;

    public boolean isShaking;
    private float shakeAmount;

    private float rollAngle;
    private final float ROLL_ANGLE_MAX = .8f;
    private final float ROLL_INCREMENT = .02f;

    public Player(float y, float z, Stage3D stage3D, float rotation, Stage stage) {
        super(0, y, z, stage3D);
        this.stage3D = stage3D;
        this.stage = stage;

        buildModel(1.7f, 3f, 1.7f, true);
        setBaseRectangle();
        isVisible = false;
        turnPlayer(rotation);
    }

    @Override
    public void act(float dt) {
        super.act(dt);
        if (isPause)
            return;

        if (isForcedToMove)
            forceMove(dt);
        else
            keyboardPolling(dt);
        mousePolling();
        stage3D.rollCamera(rollAngle);

        stage3D.camera.position.y = position.y;
        stage3D.camera.position.z = position.z;

        if (isShaking && !isForcedToMove)
            shake();
        else
            headBobbing(dt);

        if (isMoving)
            BaseGame.metalWalkingMusic.setVolume(BaseGame.soundVolume);
        else
            BaseGame.metalWalkingMusic.setVolume(0);
    }

    public void muzzleLight() {
        stage3D.lightManager.addMuzzleLight(position);
    }

    public void forceMoveAwayFrom(BaseActor3D source) {
        isForcedToMove = true;
        if (position.y - source.position.y <= 1) forceMoveY *= -1;
        if (position.z - source.position.z <= 1) forceMoveZ *= -1;
        forceTime = totalTime + SECONDS_FORCED_TO_MOVE;
    }

    public void shakeyCam(float duration, float amount) {
        isShaking = true;
        shakeAmount = amount;
        stage.addAction(Actions.sequence(
                Actions.delay(duration),
                Actions.run(() -> isShaking = false)
        ));
    }

    private void shake() {
        stage3D.camera.position.set(
                position.x + MathUtils.random(0f, shakeAmount),
                position.y + MathUtils.random(0f, shakeAmount),
                position.z + MathUtils.random(0f, shakeAmount)
        );
    }

    private void forceMove(float dt) {
        if (totalTime <= forceTime)
            moveBy(0f, forceMoveY * dt, forceMoveZ * dt);
        else
            isForcedToMove = false;
    }

    private void headBobbing(float dt) {
        totalTime += dt;

        if (BaseGame.isHeadBobbing) {
            bobCounter += bobFrequency * dt;
            if ((int) bobCounter % 2 == 0 && isMoving)
                stage3D.moveCameraUp(bobAmount);
            else if (isMoving)
                stage3D.moveCameraUp(-bobAmount);
            else
                resetXPosition();
        } else {
            resetXPosition();
        }
    }

    private void resetXPosition() {
        position.x = 0;
        stage3D.camera.position.x = 0;
    }

    private void keyboardPolling(float dt) {
        if (Gdx.input.isKeyPressed(Keys.W)) {
            moveForward(-movementSpeed * dt);
            isMoving = true;
        } else {
            isMoving = false;
        }

        if (Gdx.input.isKeyPressed(Keys.A)) {
            moveRight(movementSpeed * dt);
            isMoving = true;
            rollAngle = MathUtils.clamp(rollAngle -= ROLL_INCREMENT, -ROLL_ANGLE_MAX, ROLL_ANGLE_MAX);
        } else {
            isMoving = false;
        }

        if (Gdx.input.isKeyPressed(Keys.S)) {
            moveForward(movementSpeed * dt);
            isMoving = true;
        } else {
            isMoving = false;
        }

        if (Gdx.input.isKeyPressed(Keys.D)) {
            moveRight(-movementSpeed * dt);
            isMoving = true;
            rollAngle = MathUtils.clamp(rollAngle += ROLL_INCREMENT, -ROLL_ANGLE_MAX, ROLL_ANGLE_MAX);
        } else {
            isMoving = false;
        }

        if (!Gdx.input.isKeyPressed(Keys.A) && !Gdx.input.isKeyPressed(Keys.D))
            resetRoll();
    }

    private void resetRoll() {
        if (rollAngle > 0)
            rollAngle -= ROLL_INCREMENT;
        else if (rollAngle < 0)
            rollAngle += ROLL_INCREMENT;
    }

    private void turnPlayer(float angle) {
        if (angle == 0) return;
        stage3D.turnCameraX(angle);
        turnBy(angle);
    }

    private void mousePolling() {
        if (totalTime < .15f)
            return;

        if (Gdx.input.getDeltaX() < 300)
            turnPlayer(rotateSpeed * Gdx.input.getDeltaX() * BaseGame.mouseMovementSensitivity);
    }
}
