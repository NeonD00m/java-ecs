package JECS;

public interface ECSInterface {
    boolean contains(int id);
    void despawn(int id);
    void remove(int id, String componentName);
    JECSComponent[][] query(String[] components);
    JECSComponent[] get(int id, String[] components);
    JECSComponent[] get_single(String[] components);
    int spawn(JECSComponent[] list);
    int spawnAt(int id, JECSComponent[] list);
    void insert(int entityId, JECSComponent componentInstance);
    ECSInterface addSystem(ECSSystem system);
    void startLoop();
}

