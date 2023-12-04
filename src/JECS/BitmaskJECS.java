package JECS;

import java.util.HashMap;

public class BitmaskJECS {
    HashMap<String, Integer> componentIds;
    ResizeArray<Entity<Integer>> entities;
    ResizeArray<ResizeArray<JECSComponent>> world;

    public BitmaskJECS() {
        componentIds = new HashMap<>();
        entities = new ResizeArray<Entity<Integer>>(10, Entity[]::new);
        world = new ResizeArray<ResizeArray<JECSComponent>>(10, ResizeArray[]::new);
    }

    public int component(String className) {
        int index = componentIds.size() + 1;
        componentIds.put(className, index);
        world.set(index, new ResizeArray<>(10, JECSComponent[]::new));
        return index;
    }

    public int spawn() {
        int id = entities.next();
        Entity<Integer> ent = new Entity<>(id, 0);
        entities.add(ent);
        return id;
    }

    public void insert(int entityId, JECSComponent comp) {
        int compId = componentIds.get(comp.className());

    }


    public static void main(String[] args) {
        int x = 0x00001111;
        int y = 0x01010101;
        int z = 0x11000011;
        System.out.println("3: " + Integer.toBinaryString(3));
        System.out.println("2: " + Integer.toBinaryString(2));
        System.out.println("3 & 2: " + Integer.toBinaryString(3 & 2));
        System.out.println("\nx & y: " + Integer.toBinaryString(x & y));
        System.out.println("\nx & z: " + Integer.toBinaryString(x & z));
        System.out.println("\ny & z: " + Integer.toBinaryString(y & z));
        System.out.println("huge: " + Integer.toBinaryString(Integer.MAX_VALUE));
    }
}
