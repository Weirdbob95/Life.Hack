package game;

import engine.Signal;
import graphics.Graphics3D;
import util.Color4;
import util.RegisteredEntity;
import util.Vec3;

public class Enemy extends RegisteredEntity {

    public static final double HITBOX_SIZE = 1.5;
    public Signal<Vec3> position, prevPos, velocity;

    @Override
    public void createInner() {
        position = Premade3D.makePosition(this);
        prevPos = Premade3D.makePrevPosition(this);
        velocity = Premade3D.makeVelocity(this);
        Premade3D.makeCollisions(this, new Vec3(HITBOX_SIZE));
        Premade3D.makeGravity(this, new Vec3(0, 0, -15));
        onRender(() -> Graphics3D.drawCube(position.get().subtract(new Vec3(HITBOX_SIZE)), 2 * HITBOX_SIZE, Color4.RED));
    }
}
