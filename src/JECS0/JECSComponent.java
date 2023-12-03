package JECS0;

public class JECSComponent {
    public String className() {
        String[] split = this.getClass().getName().split("\\.");
        return split[split.length - 1];
    }
}
