package JECS;

import java.util.ArrayList;
import java.util.Timer;
import java.util.HashMap;
import java.util.LinkedList;

public class SimpleJECS implements ECSInterface {
    ResizeArray<Entity<HashMap<String, JECSComponent>>> world; // world[TYPE ID][ENTITY ID]
    LinkedList<ECSSystem> systems;
    int nextEntity;
    Timer loop;

    public SimpleJECS() {
        systems = new LinkedList<>();
        world = new ResizeArray<Entity<HashMap<String, JECSComponent>>>(10, Entity[]::new);
        nextEntity = 0;
    }

    //some types of ECS may need to know what components there are ahead of time
//    public <T extends JECSComponent> void component(Class<T> tClass) {
//        existingComponents.add(tClass.getName());
//    }

    public boolean contains(int id) {
        return id >= 0 && world.get(id) != null;
    }

    public void despawn(int id) {
        world.remove(id);
    }

    public void remove(int id, String componentName) {
        world.get(id).components.remove(componentName);
    }

    public int spawn(JECSComponent[] list) {
        int id = world.next();
        world.add(new Entity<>(id, new HashMap<>()));
        for (JECSComponent componentInstance : list) {
            insert(id, componentInstance);
        }
        return id;
    }

    public int spawnAt(int id, JECSComponent[] list) {
        if (world.get(id) != null) {
            System.out.println("Attempted to spawn entity at index " + id +
                    " when an entity at that index already existed.");
        } else {
            world.set(id, new Entity<>(id, new HashMap<>()));
        }
        for (JECSComponent componentInstance : list) {
            insert(id, componentInstance);
        }
        return id;
    }

    public void insert(int entityId, JECSComponent componentInstance) {
        Entity<HashMap<String, JECSComponent>> entity = world.get(entityId);
        String name = componentInstance.className();
        if (entity.components.containsKey(name)) {
            System.out.println("Already inserted a component of "+name+" to the entity: "+entityId+".");
            return;
        }
        entity.components.put(name, componentInstance);
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

    public JECSComponent[][] query(String[] components) {
        ArrayList<JECSComponent[]> list = new ArrayList<>(components.length);
        for (Entity<HashMap<String,JECSComponent>> ent : world.arr) {
            if (ent == null) { continue; }
            JECSComponent[] arr = new JECSComponent[components.length];
            boolean skip = false;
            for (int i = 0; i < components.length; i++) {
                String compName = components[i];
                JECSComponent comp = ent.components.get(compName);
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
        Entity<HashMap<String, JECSComponent>> ent = world.get(id);
        if (ent == null) {
            return null;
        }
        JECSComponent[] arr = new JECSComponent[components.length];
        for (int i = 0; i < components.length; i++) {
            String compName = components[i];
            JECSComponent comp = ent.components.get(compName);
            if (comp == null) {
                return new JECSComponent[0];
            }
            arr[i] = comp;
        }
        return arr;
    }

    public JECSComponent[] getSingle(String[] components) {
        for (Entity<HashMap<String, JECSComponent>> ent : world.arr) {
            if (ent == null) {continue;}
            JECSComponent[] arr = new JECSComponent[components.length];
            boolean skip = false;
            for (int i = 0; i < components.length; i++) {
                String compName = components[i];
                JECSComponent comp = ent.components.get(compName);
                if (comp == null) {
                    skip = true;
                    break;
                }
                arr[i] = comp;
            }
            if (skip) { continue; }
            return arr;
        }
        return null;
    }

    public int getSingleByIndex(String[] components) {
        for (Entity<HashMap<String, JECSComponent>> ent : world.arr) {
            if (ent == null) {continue;}
            boolean skip = false;
            for (String compName : components) {
                JECSComponent comp = ent.components.get(compName);
                if (comp == null) {
                    skip = true;
                    break;
                }
            }
            if (skip) { continue; }
            return ent.id;
        }
        return -1;
    }
}