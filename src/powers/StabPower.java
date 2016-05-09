package powers;

import engine.Core;
import engine.EventStream;
import engine.Input;
import game.Particle;
import game.Player;
import graphics.Window3D;
import map.Raycast;
import static org.lwjgl.opengl.GL11.*;
import static util.Color4.gray;
import util.Vec3;

public class StabPower extends TimedPower {

    public StabPower() {
        Core.delay(.1, onAct).onEvent(() -> {
            //Stab
            Player.player.velocity.set(new Vec3(0));
            Vec3 start = Window3D.pos.add(Window3D.facing.toVec3().cross(new Vec3(0, 0, 1)).withLength(.2));
            Vec3 end = start.add(Window3D.facing.toVec3().multiply(5));

            Raycast r = new Raycast(start, end);
            if (r.hitEnemy != null) {
                r.hitEnemy.destroy();
            }
            if (r.hitPos != null) {
                Particle.explode(r.hitPos, gray(.9));
            }

            Core.render.until(Core.time().map(t -> t < .2)).onEvent(() -> {
                glDisable(GL_TEXTURE_2D);
                glLineWidth(8);
                gray(.9).glColor();
                glBegin(GL_LINES);
                start.glVertex();
                end.glVertex();
                glEnd();
            });
        });
        whileActive.forEach(dt -> {
            if (time.get() < .1) {
                Player.player.velocity.edit(v -> Window3D.forwards().multiply(30));
            } else {
                Player.player.velocity.edit(v -> new Vec3(0).withZ(v.z));
            }
        });
    }

    @Override
    boolean canAct() {
        return time.get() > .2 && spendEnergy(20);
    }

    @Override
    double duration() {
        return .3;
    }

    @Override
    EventStream getAttemptAct() {
        return Input.whenMouse(0, true);
    }
}
