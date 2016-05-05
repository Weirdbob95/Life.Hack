package powers;

import engine.Core;
import engine.EventStream;
import engine.Input;
import game.Player;
import graphics.Graphics3D;
import graphics.Window3D;
import util.Color4;
import util.Vec3;

public class StabPower extends TimedPower {

    public StabPower() {
        Core.delay(.1, onAct).onEvent(() -> {
            //Stab
            Player.player.velocity.set(new Vec3(0));
            Vec3 start = Player.player.position.get().add(new Vec3(0, 0, .8)).add(Window3D.facing.toVec3().cross(new Vec3(0,0,1)).withLength(.2));
            Vec3 end = start.add(Window3D.facing.toVec3());
            Core.render.until(Core.time().map(t -> t < .5)).onEvent(() -> {
                Graphics3D.drawLine(start, end, Color4.WHITE);
            });
        });
        whileActive.forEach(dt -> {
            if (time.get() < .1) {
                Player.player.velocity.edit(v -> Window3D.facing.toVec3().multiply(30));
            } else {
                Player.player.velocity.set(new Vec3(0));
            }
        });
    }

    @Override
    boolean canAct() {
        return spendEnergy(10) && time.get() > .2;
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
