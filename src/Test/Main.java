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
                        ((StringComponent) world.get_single(testId, new String[]{"StringComponent"})[0]).field
                ))
                .addSystem(world -> {
                    int counter = 0;
                    for (JECSComponent[] group : world.query(new String[]{"StringComponent"})) {
                        counter++;
                    }
                    System.out.println(counter + " entities in the world currently.");
                })
                .addSystem(world -> System.out.println("last system running"))
                .startLoop();
    }
}