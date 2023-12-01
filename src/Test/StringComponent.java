package Test;

import JECS.JECSComponent;

public class StringComponent extends JECSComponent {
    String field = "Hello, World!";
    public StringComponent() {}

    public StringComponent(String str) {field = str;}
}
