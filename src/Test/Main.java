package Test;

import JECS.JECS;
import JECS.JECSComponent;
import JECS.ECSSystem;
import JECS.ECSInterface;

public class Main {
    public static void main(String[] args) {
        JECS game = new JECS();
        int testId = game.spawn(new JECSComponent[]{
                new StringComponent("Tree"),
                new HealthComponent(5),
        });
        System.out.println("LJ ID: " + game.spawn(new JECSComponent[]{
                new LumberjackComponent()
        }));
        game
//                .addSystem(world ->
//                    System.out.println("printing component: " +
//                        ((StringComponent) world.get(testId, new String[]{"StringComponent"})[0]).field
//                ))
                .addSystem(world -> {
                    LumberjackComponent person = (LumberjackComponent) world.getSingle(new String[]{"LumberjackComponent"})[0];
                    if (!world.contains(person.treeId)) {
                        // find a tree
                        person.treeId = world.getSingleByIndex(new String[]{"StringComponent", "HealthComponent"});

                        // if no tree, quit
                        if (person.treeId == -1) {
                            world.quit();
                            System.out.println("quit simulation");
                        } else {
                            System.out.println("lumberjack found a tree");
                        }
                    } else {
                        if (person.stepsToWait == 0) {
                            // cut down the tree
                            HealthComponent treeHealth = (HealthComponent) world.get(person.treeId, new String[]{"HealthComponent"})[0];
                            treeHealth.health--;
                            person.stepsToWait = 60;
                            if (treeHealth.health == 0) {
                                world.despawn(person.treeId);
                                System.out.println("the lumberjack cut down the tree!");
                            } else {
                                System.out.println("lumberjack axed the tree (Health: " + treeHealth.health + ")");
                            }
                        } else {
                            // muster up the energy
                            person.stepsToWait--;
                            if (person.stepsToWait == 30) {
                                System.out.println("About to attempt to chop down the tree again!");
                            }
                        }
                    }
                })
//                .addSystem(world -> System.out.println(world.query(new String[]{}).length + " entities in the world currently."))
                .startLoop();
    }
}