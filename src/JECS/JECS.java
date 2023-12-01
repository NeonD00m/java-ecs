package JECS;

import java.util.ArrayList;
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

public class JECS implements ECSInterface {
    ArrayList<Entity> world;
    LinkedList<ECSSystem> systems;
    int nextEntity;

    public JECS() {
        systems = new LinkedList<>();
        world = new ArrayList<>(10);
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

    public JECSComponent[] get(int id, String[] components) {
        return new JECSComponent[0];
    }

    public JECSComponent[] get_single(int id, String[] components) {
        return new JECSComponent[0];
    }

    public int spawn(JECSComponent[] list) {
        return this.spawnAt(nextEntity++, list);
    }

    public int spawnAt(int id, JECSComponent[] list) {
        if (world.get(id) == null) {
            System.out.println("Attempted to spawn entity at index " + id +
                    " when an entity at that index already existed.");
        } else {
            world.set(id,new Entity(id));
        }
        for (JECSComponent componentInstance : list) {
            insert(id, componentInstance);
        }
        return id;
    }

    public void insert(int entityId, JECSComponent componentInstance) {
        Entity entity = world.get(entityId);
        String name = componentInstance.className();
        System.out.println("inserting comp: " + name);
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
        for (Entity ent : world) {
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
}