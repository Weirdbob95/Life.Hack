package powers;

import engine.EventStream;
import engine.Input;
import game.Player;
import static org.lwjgl.input.Keyboard.KEY_LSHIFT;
import util.Vec2;

public class DashPower extends TimedPower {

    private Vec2 moveDir;

    public DashPower() {
        onAct.onEvent(() -> moveDir = Player.player.moveDir.get());
        whileActive.forEach(dt -> Player.player.velocity.edit(v -> moveDir.multiply(30).toVec3().withZ(v.z)));
        onFinish.onEvent(() -> Player.player.velocity.edit(v -> moveDir.multiply(8).toVec3().withZ(v.z)));
    }

    @Override
    boolean canAct() {
        return !Player.player.moveDir.get().equals(new Vec2(0)) && time.get() > .4 && !Player.player.wallSlide.get() && spendEnergy(30);
    }

    @Override
    double duration() {
        return .15;
    }

    @Override
    EventStream getAttemptAct() {
        return Input.whenKey(KEY_LSHIFT, true);
    }
}
