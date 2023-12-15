package JECS;

import java.util.*;

// component vector
class HashVector {
    private final int hashed;
    private final String[] components;
    public HashVector() {
        hashed = 0;
        components = new String[0];
    }
    public HashVector(String[] components) {
        PriorityQueue<String> pq = new PriorityQueue<>();
        pq.addAll(Arrays.asList(components));

        //initialize
        int index = 0;
        String[] ordered = new String[components.length];
        StringBuilder str = new StringBuilder();
        for (String node : pq) {
            str.append(node);
            ordered[index++] = node;
        }
        hashed = str.toString().hashCode();
        this.components = ordered;
    }
    public HashVector remove(String componentId) {
        String[] newComponents = components.clone();
        for (int i = 0; i < newComponents.length; i++) {
            if (components[i].equals(componentId)) {
                newComponents[i] = null;
                break;
            }
        }
        return new HashVector(newComponents);
    }
    public HashVector add(String componentId) {
        String[] newComponents = new String[components.length + 1];
        System.arraycopy(components, 0, newComponents, 0, components.length);
        newComponents[components.length] = componentId;
        return new HashVector(newComponents);
    }
    public String get(int i) { return components[i]; }
    public int size() { return components.length; }
    public int hashCode() { return hashed; }
    public String toString() {
        return "<" + Arrays.toString(components) + ">";
    }
}
// entity record
class EntityRecord {Archetype archetype; int row; int id;
    public EntityRecord(Archetype a, int r, int i){archetype = a;row = r;id = i;}}
// archetype record
class ArchetypeRecord {int column; public ArchetypeRecord(int c) {column = c;}}
// archetype edge for the archetype graph
class ArchetypeEdge {final Archetype add; final Archetype remove;
    public ArchetypeEdge(Archetype a, Archetype r){add = a;remove = r;}}
// the archetypes themselves with all the components involved
class Archetype {
    private static final int ROWS = 2;
    int id, length;
    HashVector type;
    JECSComponent[][] columns;
    HashMap<String, ArchetypeEdge> edges;
    HashMap<Integer, EntityRecord> rowToEntity;
    public Archetype(HashVector componentTypes, int archetypeId) {
        length = ROWS;
        id = archetypeId;
        type = componentTypes;
        edges = new HashMap<>();
        columns = new JECSComponent[componentTypes.size()][ROWS];
        rowToEntity = new HashMap<>(ROWS);
    }
    public int findFirst() {
        for (int i = 0; i <= columns.length; i++) {
            if (columns[0][i] != null) {
                return i;
            }
        }
        return 0;
    }
    public int nextRow() {
        int result = columns.length;
        for (int i = 0; i <= columns.length; i++) {
            if (i == columns.length) {
                expand();
            } else if (columns[0][i] == null) {
                result = i;
                break;
            }
        }
        return result;
    }
    public void expand() {
        length *= 2;
        JECSComponent[][] oldColumns = columns;
        columns = new JECSComponent[oldColumns.length][length];
        for (int col = 0; col < oldColumns.length; col++) {
            System.arraycopy(oldColumns[col], 0, columns[col], 0, oldColumns[0].length);
        }
    }
}

public class ArchetypeJECS implements ECSInterface {
    //ComponentId: String, EntityId: Integer, ArchetypeId: Integer
    HashMap<HashVector, Archetype> archetype_index;
    ResizeArray<EntityRecord> entity_index;
    HashMap<String, ResizeArray<ArchetypeRecord>> component_index;
    private final HashVector empty = new HashVector();
    LinkedList<ECSSystem> systems;
    Timer loop;

    public ArchetypeJECS() {
        systems = new LinkedList<>();
        archetype_index = new HashMap<>();
        component_index = new HashMap<>();
        archetype_index.put(empty, new Archetype(empty, 0));
        entity_index = new ResizeArray<>(2, EntityRecord[]::new);
//        entity_index = new HashMap<>(2);
    }

    //PRIVATE METHODS
    //initiate archetypes lazily: adds a component column to an existing archetype
    private Archetype addToArchetype(Archetype source, String componentId) {
        HashVector vector = source.type.add(componentId);
        Archetype created = archetype_index.get(vector);
        //if the archetype we want doesn't exist, create it
        if (created == null) {
            System.out.println("lazily created archetype: " + vector);
            created = new Archetype(vector, archetype_index.size());
            for (int i = 0; i < vector.size(); i++) {
                component_index.get(vector.get(i)).set(created.id, new ArchetypeRecord(i));
            }
        }
        archetype_index.put(vector, created);
        System.out.println("added to archetype_index: " + archetype_index);
        // add the ArchetypeEdges for if componentId is added or removed from the archetypes
        source.edges.put(componentId, new ArchetypeEdge(created, null));
        created.edges.put(componentId, new ArchetypeEdge(null, source));
        return created;
    }
    // for changing an entity's archetype when a component is inserted
    private void moveEntity(int entityId, Archetype archetype, int previousRow, Archetype nextArchetype) {
        //find empty row in nextArchetype
        EntityRecord entityRecord = entity_index.get(entityId);
        int rowFound = nextArchetype.nextRow();
        //update entityRecord with new row and new archetype
        entityRecord.row = rowFound;
        entityRecord.archetype = nextArchetype;
        //remove an entity from the previous archetype at previousRow
        archetype.rowToEntity.remove(previousRow);
        for (JECSComponent[] column : archetype.columns) {
            JECSComponent component = column[previousRow];
            column[previousRow] = null;
            //add it to the nextArchetype at the new row
            ArchetypeRecord archetypeRecord = component_index.get(component.className()).get(nextArchetype.id);
            if (archetypeRecord == null) { continue; } // this component was removed from the entity
            nextArchetype.columns[archetypeRecord.column][rowFound] = component;
        }
        nextArchetype.rowToEntity.put(rowFound, entityRecord);
    }

    //PUBLIC METHODS
    public void component(String componentName) {
        //make the array of archetypeIds to ArchetypeRecords
        component_index.put(componentName, new ResizeArray<>(2, ArchetypeRecord[]::new));
    }
    public int spawn(JECSComponent[] list) {
        //make the entity, initially as archetype []
        Archetype emptyArchetype = archetype_index.get(empty);
        int rowFound = emptyArchetype.nextRow();
        int id = entity_index.next();
        EntityRecord entityRecord = new EntityRecord(emptyArchetype, rowFound, id);
        entity_index.set(id, entityRecord);
        emptyArchetype.rowToEntity.put(rowFound, entityRecord);
        for (JECSComponent component : list) {
            insert(id, component);
        }
        System.out.println(entityRecord.archetype.type);
        System.out.println(Arrays.deepToString(entityRecord.archetype.columns));
        return id;
    }
    public int spawnAt(int id, JECSComponent[] list) {
        //make the entity, initially as archetype []
        Archetype emptyArchetype = archetype_index.get(empty);
        int rowFound = emptyArchetype.nextRow();
        EntityRecord entityRecord = new EntityRecord(emptyArchetype, rowFound, id);
        entity_index.set(id, entityRecord);
        emptyArchetype.rowToEntity.put(rowFound, entityRecord);
        for (JECSComponent component : list) {
            insert(id, component);
        }
        return id;
    }
    public boolean contains(int entityId) {
        return entity_index.containsKey(entityId);
    }
    public void insert(int entityId, JECSComponent componentInstance) {
        String component = componentInstance.className();
        EntityRecord entityRecord = entity_index.get(entityId);
        Archetype archetype = entityRecord.archetype;
        ArchetypeEdge archetypeEdge = archetype.edges.get(component);
        Archetype nextArchetype;
        if (archetypeEdge == null) { //create archetypes and edges lazily
            nextArchetype = addToArchetype(archetype, component);
        } else {
            nextArchetype = archetypeEdge.add;
        }
        moveEntity(entityId, archetype, entityRecord.row, nextArchetype);
        // add componentInstance to new archetype
        int column = component_index.get(component).get(nextArchetype.id).column;
        nextArchetype.columns[column][entityRecord.row] = componentInstance;
    }
    public void despawn(int entityId) {
        EntityRecord entityRecord = entity_index.get(entityId);
        Archetype archetype = entityRecord.archetype;
        //clear the components from the archetype
        for (JECSComponent[] column : archetype.columns) {
            column[entityRecord.row] = null;
        }
        //remove entity from entity_index
        entity_index.remove(entityId);
        archetype.rowToEntity.remove(entityRecord.row);
    }
    public void remove(int entityId, String componentName) {
        EntityRecord entityRecord = entity_index.get(entityId);
        Archetype archetype = entityRecord.archetype;
        Archetype nextArchetype = archetype.edges.get(componentName).remove;
        moveEntity(entityId, archetype, entityRecord.row, nextArchetype);
    }
    public JECSComponent[][] query(String[] components) {
        Archetype archetype = archetype_index.get(new HashVector(components));
        ArrayList<JECSComponent[]> list = new ArrayList<>(components.length);

        for (int row = 0; row < archetype.rowToEntity.size(); row++) {
            JECSComponent[] arr = new JECSComponent[components.length];
            for (int i = 0; i < components.length; i++) {
                int column = component_index.get(components[i]).get(archetype.id).column;
                arr[i] = archetype.columns[column][row];
            }
            list.add(arr);
        }
        //TODO: make query also look through all of the "add" archetypes

        return list.toArray(new JECSComponent[list.size()][components.length]);
    }
    public JECSComponent get(int entityId, String component) {
        EntityRecord entityRecord = entity_index.get(entityId);
        Archetype archetype = entityRecord.archetype;
        ResizeArray<ArchetypeRecord> archetypes = component_index.get(component);
        ArchetypeRecord archetypeRecord = archetypes.get(archetype.id);
        return archetype.columns[archetypeRecord.column][entityRecord.row];
    }
    public JECSComponent[] get(int entityId, String[] components) {
        JECSComponent[] result = new JECSComponent[components.length];
        EntityRecord entityRecord = entity_index.get(entityId);
        Archetype archetype = entityRecord.archetype;
        for (int i = 0; i < components.length; i++) {
            ResizeArray<ArchetypeRecord> archetypes = component_index.get(components[i]);
            ArchetypeRecord archetypeRecord = archetypes.get(archetype.id);
            result[i] = archetype.columns[archetypeRecord.column][entityRecord.row];
        }
        return result;
    }
    public JECSComponent[] getSingle(String[] components) {
        JECSComponent[] result = new JECSComponent[components.length];
        HashVector type = new HashVector(components);
        Archetype archetype = archetype_index.get(type);
        if (archetype == null) {
            System.out.println("Can't query archetypes for " + type + " because archetype doesn't exist yet");
            return null;
        }
        int row = archetype.findFirst();
        for (int i = 0; i < components.length; i++) {
            ResizeArray<ArchetypeRecord> archetypes = component_index.get(components[i]);
            ArchetypeRecord archetypeRecord = archetypes.get(archetype.id);
            result[i] = archetype.columns[archetypeRecord.column][row];
        }
        return result;
    }
    public int getSingleByIndex(String[] components) {
        Archetype archetype = archetype_index.get(new HashVector(components));
        return archetype.rowToEntity.get(archetype.findFirst()).id;
    }
    public ECSInterface addSystem(ECSSystem system) {
        systems.add(system);
        return this;
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
}