package Test;

import JECS0.JECSComponent;

public class StringComponent extends JECSComponent {
    String field = "Hello, World!";
    public StringComponent() {}

    public StringComponent(String str) {field = str;}
}
