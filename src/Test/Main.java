package Test;

import JECS.JECS;
import JECS.JECSComponent;
import JECS.ECSSystem;
import JECS.ECSInterface;

public class Main {
    public static void main(String[] args) {
        JECS game = new JECS();
        int testId = game.spawn(new JECSComponent[]{new StringComponent()});

        game.addSystem(world ->
                System.out.println("printing component: " +
                        ((StringComponent) world.get(testId, new String[]{"StringComponent"})[0]).field
                ))
                .addSystem(world -> System.out.println(world.query(new String[]{}).length + " entities in the world currently."))
                .startLoop();
    }
}