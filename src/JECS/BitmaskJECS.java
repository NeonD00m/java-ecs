package JECS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Timer;

public class BitmaskJECS implements ECSInterface {
    HashMap<String, Integer> componentIds;
    ResizeArray<Entity<Integer>> entities;
    ResizeArray<ResizeArray<JECSComponent>> world;
    LinkedList<ECSSystem> systems;
    Timer loop;

    public BitmaskJECS() {
        systems = new LinkedList<>();
        componentIds = new HashMap<>();
        entities = new ResizeArray<Entity<Integer>>(10, Entity[]::new);
        world = new ResizeArray<ResizeArray<JECSComponent>>(10, ResizeArray[]::new);
    }

    public void component(String className) {
        int index = (int) Math.pow(2, componentIds.size());
        System.out.println(className + " : " + Integer.toBinaryString(index));
        componentIds.put(className, index);
        world.set(index, new ResizeArray<>(10, JECSComponent[]::new));
    }

    public void insert(int entityId, JECSComponent comp) {
        int compId = componentIds.get(comp.className());
        entities.get(entityId).components |= compId;
        world.get(entityId).set(compId, comp);
    }

    public void startLoop() {
        if (loop != null) {
            return;
        }
        loop = new Timer();
        loop.scheduleAtFixedRate(new LoopTimerTask(this, systems), 0, 17);
    }

    public void quit() {
        loop.cancel();
        loop = null;
    }

    public ECSInterface addSystem(ECSSystem system) {
        systems.add(system);
        return this;
    }

    public boolean contains(int id) {
        return id >= 0 && world.get(id) != null;
    }

    public void despawn(int id) {
        world.remove(id);
        entities.remove(id);
    }

    public void remove(int id, String componentName) {
        world.get(id).remove(componentIds.get(componentName));
    }

    public JECSComponent[][] query(String[] components) {
        int combo = 0;
        for (String name : components) {
            combo |= componentIds.getOrDefault(name, 0);
        }
        ArrayList<JECSComponent[]> list = new ArrayList<>(components.length);
        for (Entity<Integer> ent : entities.arr) {
            if (ent == null) { continue; }
            if ((ent.components & combo) != combo) { continue; }
            JECSComponent[] arr = new JECSComponent[components.length];
            boolean skip = false;
            for (int i = 0; i < components.length; i++) {
                String compName = components[i];
                JECSComponent comp = world.get(ent.id).get(componentIds.get(compName));
                if (comp == null) {
                    skip = true;
                    break;
                }
                arr[i] = comp;
            }
            if (skip) { continue; }
            list.add(arr);
        }
        JECSComponent[][] results = new JECSComponent[list.size()][components.length];
        for (int i = 0; i < list.size(); i++) {
            results[i] = list.get(i);
        }
        return results;
    }

    public JECSComponent[] get(int id, String[] components) {
        int combo = 0;
        for (String name : components) {
            combo |= componentIds.getOrDefault(name, 0);
        }
        if (entities.get(id) != null && (entities.get(id).components & combo) == combo) {
            JECSComponent[] arr = new JECSComponent[components.length];
            for (int i = 0; i < components.length; i++) {
                String compName = components[i];
                JECSComponent comp = world.get(id).get(componentIds.get(compName));
                arr[i] = comp;
            }
            return arr;
        }
        return null;
    }

    public JECSComponent[] getSingle(String[] components) {
        int combo = 0;
        for (String name : components) {
            combo |= componentIds.getOrDefault(name, 0);
        }
        for (Entity<Integer> ent : entities.arr) {
            if (ent == null) { continue; }
            if ((ent.components & combo) != combo) { continue; }
            JECSComponent[] arr = new JECSComponent[components.length];
            for (int i = 0; i < components.length; i++) {
                String compName = components[i];
                JECSComponent comp = world.get(ent.id).get(componentIds.get(compName));
                arr[i] = comp;
            }
            return arr;
        }
        return null;
    }

    public int getSingleByIndex(String[] components) {
        int combo = 0;
        for (String name : components) {
            combo |= componentIds.getOrDefault(name, 0);
        }
        for (Entity<Integer> ent : entities.arr) {
            if (ent == null) { continue; }
            if ((ent.components & combo) != combo) { continue; }
            return ent.id;
        }
        return -1;
    }

    public int spawn(JECSComponent[] list) {
        int id = entities.next();
        world.set(id, new ResizeArray<>(componentIds.size(), JECSComponent[]::new));
        Entity<Integer> ent = new Entity<>(id, 0);
        entities.set(id, ent);
        for (JECSComponent comp : list) {
            insert(id, comp);
        }
        return id;
    }

    public int spawnAt(int id, JECSComponent[] list) {
        world.set(id, new ResizeArray<>(componentIds.size(), JECSComponent[]::new));
        Entity<Integer> ent = new Entity<>(id, 0);
        entities.set(id, ent);
        for (JECSComponent comp : list) {
            insert(id, comp);
        }
        return id;
    }
}
