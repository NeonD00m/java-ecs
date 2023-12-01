package JECS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

class Entity {
    int id;
    HashMap<String, JECSComponent> classNameToComponent;

    public Entity(int id) {
        this.id = id;
        classNameToComponent = new HashMap<>();
    }
}

class ResizeArray {
    Entity[] arr;
    private int size;
    public ResizeArray(int capacity) {
        arr = new Entity[capacity];
        size = 0;
    }

    public Entity get(int index) {
        return arr[index];
    }

    public int next() {
        int index = size;
        while (size >= arr.length || arr[size] != null) {
            index++;
        }
        return index;
    }

    public void add(Entity value) {
        if (size == arr.length) {
            resize(2);
        }
        while (arr[size] != null) {
            size++;
        }
        arr[size++] = value;
    }

    public void set(int index, Entity value) {
        if (arr.length <= index) {
            resize(1 + Math.floorDiv(index, arr.length));
        }
        arr[index] = value;
    }

    public void remove(int index) {
        arr[index] = null;
    }

    private void resize(int multi) {
        Entity[] old = arr;
        arr = new Entity[old.length * multi];
        System.arraycopy(old, 0, arr, 0, old.length);
    }
}

public class JECS implements ECSInterface {
    ResizeArray world;
    LinkedList<ECSSystem> systems;
    int nextEntity;

    public JECS() {
        systems = new LinkedList<>();
        world = new ResizeArray(10);
        nextEntity = 0;
    }

    //some types of ECS may need to know what components there are ahead of time
//    public <T extends JECSComponent> void component(Class<T> tClass) {
//        existingComponents.add(tClass.getName());
//    }

    public boolean contains(int id) {
        return world.get(id) != null;
    }

    public void despawn(int id) {
        world.remove(id);
    }

    public void remove(int id, String componentName) {
        world.get(id).classNameToComponent.remove(componentName);
    }

    public int spawn(JECSComponent[] list) {
        int id = world.next();
        world.add(new Entity(id));
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
            world.set(id, new Entity(id));
        }
        for (JECSComponent componentInstance : list) {
            insert(id, componentInstance);
        }
        return id;
    }

    public void insert(int entityId, JECSComponent componentInstance) {
        Entity entity = world.get(entityId);
        String name = componentInstance.className();
        if (entity.classNameToComponent.containsKey(name)) {
            System.out.println("YOU ALREADY INSERTED THIS COMPONENT DUMMY");
            return;
        }
        entity.classNameToComponent.put(name, componentInstance);
    }

    public void startLoop() {
        //right now it only calls the systems once, only steps the loop once
        for (ECSSystem system : systems) {
            system.loop(this);
        }
    }

    public ECSInterface addSystem(ECSSystem system) {
        systems.add(system);
        return this;
    }

    public JECSComponent[][] query(String[] components) {
        ArrayList<JECSComponent[]> list = new ArrayList<>(components.length);
        for (Entity ent : world.arr) {
            if (ent == null) { continue; }
            JECSComponent[] arr = new JECSComponent[components.length];
            boolean skip = false;
            for (int i = 0; i < components.length; i++) {
                String compName = components[i];
                JECSComponent comp = ent.classNameToComponent.get(compName);
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
        Entity ent = world.get(id);
        if (ent == null) {
            return new JECSComponent[0];
        }
        JECSComponent[] arr = new JECSComponent[components.length];
        for (int i = 0; i < components.length; i++) {
            String compName = components[i];
            JECSComponent comp = ent.classNameToComponent.get(compName);
            if (comp == null) {
                return new JECSComponent[0];
            }
            arr[i] = comp;
        }
        JECSComponent[] results = new JECSComponent[components.length];
        System.arraycopy(arr, 0, results, 0, components.length);
        return results;
    }

    public JECSComponent[] get_single(String[] components) {
        for (Entity ent : world.arr) {
            JECSComponent[] arr = new JECSComponent[components.length];
            boolean skip = false;
            for (int i = 0; i < components.length; i++) {
                String compName = components[i];
                JECSComponent comp = ent.classNameToComponent.get(compName);
                if (comp == null) {
                    skip = true;
                    break;
                }
                arr[i] = comp;
            }
            if (skip) { continue; }
            JECSComponent[] results = new JECSComponent[components.length];
            System.arraycopy(arr, 0, results, 0, components.length);
            return results;
        }
        return new JECSComponent[0];
    }
}