package game;

import engine.Core;
import engine.Input;
import engine.Signal;
import graphics.Graphics2D;
import graphics.Window3D;
import graphics.loading.SpriteContainer;
import static gui.TypingManager.isTyping;
import map.CubeMap;
import static map.CubeMap.WORLD_SIZE;
import networking.Client;
import static networking.MessageType.SNOWBALL;
import org.lwjgl.input.Keyboard;
import static org.lwjgl.input.Keyboard.KEY_A;
import static org.lwjgl.input.Keyboard.KEY_D;
import static org.lwjgl.input.Keyboard.KEY_S;
import static org.lwjgl.input.Keyboard.KEY_SPACE;
import static org.lwjgl.input.Keyboard.KEY_W;
import powers.DashPower;
import powers.StabPower;
import util.*;
import static util.Color4.BLACK;

public class Player extends RegisteredEntity {

    public static Player player;

    public Signal<Vec3> position, prevPos, velocity;
    public Signal<Vec2> moveDir = new Signal(new Vec2(0));

    @Override
    protected void createInner() {
        player = this;
        //Create the player's variables
        position = Premade3D.makePosition(this);
        prevPos = Premade3D.makePrevPosition(this);
        velocity = Premade3D.makeVelocity(this);
        Mutable<Integer> ammoCount = new Mutable(3);
        Mutable<Double> moveSpeed = new Mutable(8.);

        position.set(WORLD_SIZE.multiply(.5));

        //Make the camera automatically follow the player
        position.doForEach(v -> Window3D.pos = v.add(new Vec3(0, 0, .8)));

        //Make the player collide with the floor
        Signal<CollisionInfo> collisions = Premade3D.makeCollisions(this, new Vec3(.3, .3, .9));
        Signal<Boolean> onGround = addChild(Core.update.map(() -> velocity.get().z <= 0 && CubeMap.isSolid(position.get().add(new Vec3(0, 0, -.01)), new Vec3(.3, .3, .9))));

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
                velocity.edit(v -> v.toVec2().interpolate(moveDir.get().multiply(moveSpeed.get()), Math.pow(onGround.get() ? .0001 : .1, dt)).toVec3().withZ(v.z));
            }
        });

        new DashPower();
        new StabPower();

        //Force the player to stay inside the room
        position.filter(p -> !p.containedBy(new Vec3(0), WORLD_SIZE)).forEach(p -> {
            position.set(p.clamp(new Vec3(0), WORLD_SIZE.subtract(new Vec3(.0001))));
        });

        //Jumping
        add(Input.whileKey(Keyboard.KEY_SPACE, true).filter(onGround).onEvent(() -> {
            velocity.edit(v -> v.withZ(8));
        }));

        //Wall Jumping
        add(Input.whenKey(KEY_SPACE, true).onEvent(() -> {
            if (collisions.get() != null) {
                if (collisions.get().hitX || collisions.get().hitY) {
                    if (velocity.get().z > 0) {
                        if (!onGround.get()) {
                            velocity.edit(v -> v.add(collisions.get().normal().withLength(8)).withZ(8));
                            //Window3D.facing = Window3D.facing.withT(velocity.get().direction());
                        }
                    }
                }
            }
        }));

        //Gathering ammo
        add(Input.whenMouse(1, true).limit(.75).onEvent(() -> {
            if (ammoCount.o <= 2) {
                moveSpeed.o = moveSpeed.o * .5;
                Core.timer(.75, () -> {
                    ammoCount.o++;
                    moveSpeed.o = moveSpeed.o / .5;
                });
            }
        }));

        //Draw ammo
        Core.renderLayer(100).onEvent(() -> {
            Window3D.guiProjection();

            Graphics2D.fillRect(new Vec2(800, 50), new Vec2(300, 100), Color4.gray(.5));
            Graphics2D.drawRect(new Vec2(800, 50), new Vec2(300, 100), BLACK);
            for (int i = 0; i < ammoCount.o; i++) {
                Graphics2D.drawSprite(SpriteContainer.loadSprite("ball"), new Vec2(850 + 100 * i, 100), new Vec2(2), 0, BallAttack.BALL_COLOR);
            }

            Window3D.resetProjection();
        });

        //Throwing snowballs
        add(Input.whenMouse(0, true).limit(.5).onEvent(() -> {
            if (ammoCount.o > 0) {
                Vec3 pos = position.get().add(new Vec3(0, 0, .8));
                Vec3 vel = Window3D.facing.toVec3().withLength(30);

                Client.sendMessage(SNOWBALL, pos, vel, -1);

                BallAttack b = new BallAttack();
                b.create();
                b.get("position", Vec3.class).set(pos);
                b.get("velocity", Vec3.class).set(vel);
                ammoCount.o--;
            }
        }));
    }
}
