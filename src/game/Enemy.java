package game;

import engine.Signal;
import graphics.Graphics3D;
import util.Color4;
import util.RegisteredEntity;
import util.Vec3;

public class Enemy extends RegisteredEntity {

    public static final double HITBOX_SIZE = 1.5;

    @Override
    public void createInner() {
        Signal<Vec3> position = Premade3D.makePosition(this);
        onRender(() -> Graphics3D.drawCube(position.get().subtract(new Vec3(HITBOX_SIZE)), 2 * HITBOX_SIZE, Color4.RED));
    }
}
