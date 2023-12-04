package JECS;

class Entity<T> {
    int id;
    T components; //HashMap<String, JECSComponent> classNameToComponent;
    public Entity(int id, T comps) {
        this.id = id;
        components = comps; //classNameToComponent = new HashMap<>();
    }
}