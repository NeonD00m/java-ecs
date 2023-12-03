package Test;

import JECS.JECSComponent;

public class LumberjackComponent extends JECSComponent {
    int treeId = -1; // used with world.contains()
    int stepsToWait = 0; //set to 60 to wait 1 s
}
