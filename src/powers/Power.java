package powers;

import engine.Core;
import engine.EventStream;
import engine.Signal;

abstract class Power {

    public static Power current;
    public static double energy;

    public static boolean spendEnergy(double e) {
        if (e > energy) {
            return true;
        }
        energy -= e;
        return true;
    }

    EventStream attemptAct, onAct, onFinish;
    Signal<Boolean> isActive;
    Signal<Double> whileActive, time;

    public Power() {
        attemptAct = getAttemptAct();
        onAct = attemptAct.filter(() -> current == null).filter(this::canAct).onEvent(() -> {
            current = this;
            isActive.set(true);
            time.set(0.);
        });
        onFinish = getDeact();
        onFinish.onEvent(() -> {
            current = null;
            isActive.set(false);
            time.set(0.);
        });
        isActive = new Signal(false);
        whileActive = Core.update.filter(isActive);
        time = Core.time();
    }

    void finish() {
        onFinish.sendEvent();
    }

    abstract boolean canAct();

    abstract EventStream getAttemptAct();

    abstract EventStream getDeact();
}
