package JECS;

public interface ECSInterface {
    int spawn(JECSComponent[] list);
    int spawnAt(int id, JECSComponent[] list);
    boolean contains(int id);
    void insert(int entityId, JECSComponent componentInstance);
    void despawn(int id);
    void remove(int id, String componentName);
    JECSComponent[][] query(String[] components);
    JECSComponent[] get(int id, String[] components);
    JECSComponent[] getSingle(String[] components);
    int getSingleByIndex(String[] components);
    ECSInterface addSystem(ECSSystem system);
    void startLoop();
    void quit();
}

