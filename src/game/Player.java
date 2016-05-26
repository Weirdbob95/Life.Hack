package game;

import engine.Core;
import engine.Input;
import engine.Signal;
import static game.Enemy.HITBOX_SIZE;
import graphics.Graphics2D;
import graphics.Window3D;
import static gui.TypingManager.isTyping;
import map.CubeMap;
import static map.CubeMap.WORLD_SIZE;
import map.Raycast;
import org.lwjgl.input.Keyboard;
import static org.lwjgl.input.Keyboard.*;
import powers.DashPower;
import powers.Power;
import powers.StabPower;
import powers.WallClimbPower;
import static util.Color4.*;
import util.Mutable;
import util.RegisteredEntity;
import util.Vec2;
import util.Vec3;

public class Player extends RegisteredEntity {

    public static Player player;

    public Signal<Vec3> position, prevPos, velocity;
    public Signal<Vec2> moveDir = new Signal(new Vec2(0));
    public Signal<Boolean> onGround, wallSlide;

    @Override
    protected void createInner() {
        player = this;
        //Create the player's variables
        position = Premade3D.makePosition(this);
        prevPos = Premade3D.makePrevPosition(this);
        velocity = Premade3D.makeVelocity(this);
        Mutable<Double> moveSpeed = new Mutable(8.);

        position.set(WORLD_SIZE.multiply(.5));

        //Make the camera automatically follow the player
        position.doForEach(v -> Window3D.pos = v.add(new Vec3(0, 0, .8)));

        //Make the player collide with the floor
        Signal<CollisionInfo> collisions = Premade3D.makeCollisions(this, new Vec3(.3, .3, .9));
        onGround = addChild(Core.update.map(() -> velocity.get().z <= 0 && CubeMap.isSolid(position.get().add(new Vec3(0, 0, -.01)), new Vec3(.3, .3, .9))));

        //Give the player basic first-person controls
        Premade3D.makeMouseLook(this, 5, -1.5, 1.5);
        Premade3D.makeGravity(this, new Vec3(0, 0, -15));

        onUpdate(dt -> {
            if (!isTyping()) {
                Vec2 dir = new Vec2(0);
                if (Input.keySignal(KEY_W).get()) {
                    dir = dir.add(new Vec2(1, 0));
                }
                if (Input.keySignal(KEY_S).get()) {
                    dir = dir.add(new Vec2(-1, 0));
                }
                if (Input.keySignal(KEY_A).get()) {
                    dir = dir.add(new Vec2(0, 1));
                }
                if (Input.keySignal(KEY_D).get()) {
                    dir = dir.add(new Vec2(0, -1));
                }
                if (!dir.equals(new Vec2(0))) {
                    moveDir.set(dir.rotate(Window3D.facing.t).normalize());
                } else {
                    moveDir.set(dir);
                }
                velocity.edit(v -> v.toVec2().interpolate(moveDir.get().multiply(moveSpeed.get()), Math.pow(onGround.get() ? .00001 : .1, dt)).toVec3().withZ(v.z));
            }
        });

        //Jumping
        add(Input.whileKey(Keyboard.KEY_SPACE, true).filter(onGround).onEvent(() -> {
            velocity.edit(v -> v.withZ(8));
        }));

        //Wall Slide
        wallSlide = collisions.map(c -> {
            if (c == null || onGround.get()) {
                return false;
            }
            if (c.normal().toVec2().dot(moveDir.get()) < 0) {
                return true;
            }
            return wallSlide.get();
        });
        add(wallSlide, Core.update.filter(wallSlide).forEach(dt -> {
            velocity.edit(v -> v.add(collisions.get().normal().multiply(-.1)));
            if (velocity.get().z < -2) {
                velocity.edit(v -> v.withZ(-2));
            }
        }));

        //Wall Jumping
        add(Input.whenKey(KEY_SPACE, true).filter(wallSlide).onEvent(() -> {
            velocity.edit(v -> v.add(collisions.get().normal().withLength(12)).withZ(8));
            //Window3D.facing = Window3D.facing.withT(velocity.get().direction());
        }));

        //Powers
        new DashPower();
        new WallClimbPower();
        new StabPower();

        //Create and destroy enemies
        add(Input.whenKey(KEY_1, true).onEvent(() -> {
            Raycast r = new Raycast(Window3D.pos, Window3D.pos.add(Window3D.facing.toVec3().multiply(20)));
            if (r.hitEnemy != null) {
                r.hitEnemy.destroy();
            } else if (r.hitWall) {
                Enemy e = new Enemy();
                e.create();
                e.get("position", Vec3.class).set(r.hitPos.add(r.normal.multiply(HITBOX_SIZE + .01)));
            }
        }));

        //Draw GUI
        Core.renderLayer(100).onEvent(() -> {
            Window3D.guiProjection();

            Graphics2D.drawEllipse(new Vec2(600, 400), new Vec2(10), RED, 20);

            Graphics2D.fillRect(new Vec2(900, 100), new Vec2(200, 50), gray(.8));
            Graphics2D.drawRect(new Vec2(900, 100), new Vec2(200, 50), BLACK);
            Graphics2D.fillRect(new Vec2(910, 110), new Vec2(180, 30), BLACK);
            Graphics2D.fillRect(new Vec2(910, 110), new Vec2(180 * Power.energy / 100, 30), ORANGE);
            Graphics2D.drawRect(new Vec2(910, 110), new Vec2(180, 30), BLACK);

            Window3D.resetProjection();
        });
    }
}
