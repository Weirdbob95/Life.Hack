package powers;

import engine.Signal;

public class PowerManager {

    public static final Signal<Power> current = new Signal(null), next = new Signal(null);
    public static double energy = 100;

    static {
        next.throttle(.1).filter(() -> next.get() != null).onEvent(() -> next.set((Power) null));
    }

    static boolean spendEnergy(double e) {
        if (energy < e) {
            return false;
        }
        energy -= e;
        return true;
    }
}
