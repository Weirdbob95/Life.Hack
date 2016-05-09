package powers;

import engine.Core;
import engine.EventStream;
import engine.Signal;

public abstract class Power {

    public static Power current;
    public static double energy = 100;

    static {
        Core.update.forEach(dt -> energy = Math.min(100, energy + 20 * dt));
    }

    public static boolean spendEnergy(double e) {
        if (e > energy) {
            return false;
        }
        energy -= e;
        return true;
    }

    EventStream attemptAct, onAct, onFinish;
    Signal<Boolean> isActive;
    Signal<Double> whileActive, time;

    public Power() {
        isActive = new Signal(false);
        whileActive = Core.update.filter(isActive);
        time = Core.time();

        attemptAct = getAttemptAct();
        onAct = attemptAct.filter(() -> current == null).filter(this::canAct).onEvent(() -> {
            current = this;
            isActive.set(true);
            time.set(0.);
        });
        onFinish = getDeact().filter(isActive);
        onFinish.onEvent(() -> {
            current = null;
            isActive.set(false);
            time.set(0.);
        });
    }

    void finish() {
        onFinish.sendEvent();
    }

    abstract boolean canAct();

    abstract EventStream getAttemptAct();

    abstract EventStream getDeact();
}
