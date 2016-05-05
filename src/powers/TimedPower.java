package powers;

import engine.Core;
import engine.EventStream;

abstract class TimedPower extends Power {

    abstract double duration();

    @Override
    EventStream getDeact() {
        return Core.delay(duration(), onAct);
    }
}
