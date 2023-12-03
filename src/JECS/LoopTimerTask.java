package JECS;

import java.util.LinkedList;
import java.util.TimerTask;

public class LoopTimerTask extends TimerTask {
    ECSInterface engine;
    LinkedList<ECSSystem> systems;
    public LoopTimerTask(
            ECSInterface engine,
            LinkedList<ECSSystem> systems
    ) {
        this.engine = engine;
        this.systems = systems;
    }
    public void run() {
        for (ECSSystem system : systems) {
            system.loop(engine);
        }
    }
}
