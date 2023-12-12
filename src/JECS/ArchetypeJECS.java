package JECS;

import java.util.*;

// component vector
class HashVector {
    private final int hashed;
    private final String[] components;
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
}
// entity record
class EntityRecord {Archetype archetype; int row;
    public EntityRecord(Archetype a, int r){archetype = a;row = r;}}
// archetype record
class ArchetypeRecord {int column; public ArchetypeRecord(int c) {column = c;}}
// archetype edge for the archetype graph
class ArchetypeEdge {final Archetype add; final Archetype remove;
    public ArchetypeEdge(Archetype a, Archetype r){add = a;remove = r;}}
// the archetypes themselves with all the components involved
class Archetype {
    private static final int ROWS = 2;
    int id;
    HashVector type;
    JECSComponent[][] columns;
    HashMap<String, ArchetypeEdge> edges;
    public Archetype(HashVector componentTypes, int aId) {
        id = aId;
        type = componentTypes;
        edges = new HashMap<>();
        columns = new JECSComponent[componentTypes.size()][ROWS];
    }

    public void expand() {
        JECSComponent[][] oldColumns = columns;
        columns = new JECSComponent[oldColumns.length][oldColumns[0].length * 2];
        for (int col = 0; col < oldColumns.length; col++) {
            System.arraycopy(oldColumns[col], 0, columns[col], 0, oldColumns[0].length);
        }
    }
}

public class ArchetypeJECS {
    //ComponentId: String, EntityId: Integer, ArchetypeId: Integer
    HashMap<HashVector, Archetype> archetype_index;
    HashMap<Integer, EntityRecord> entity_index; //TODO: change to ResizeArray
    HashMap<String, HashMap<Integer, ArchetypeRecord>> component_index;

    public ArchetypeJECS() {
        entity_index = new HashMap<>();
        archetype_index = new HashMap<>();
        component_index = new HashMap<>();
    }

    //PRIVATE METHODS
    //initiate archetypes lazily: adds a component column to an existing archetype
    private Archetype addToArchetype(Archetype source, String componentId) {
        //TODO: ARCHETYPE MIGHT ALREADY EXIST BUT NOT HAVE THE EDGE NEEDED
        Archetype created = new Archetype(source.type.add(componentId), archetype_index.size());
        archetype_index.put(created.type, created);
        source.edges.put(componentId, new ArchetypeEdge(created, null));
        created.edges.put(componentId, new ArchetypeEdge(null, source));
        return created;
    }
    // for changing an entity's archetype when a component is inserted
    private void moveEntity(int entityId, Archetype archetype, int previousRow, Archetype nextArchetype) {
        //find empty row in nextArchetype
        EntityRecord entityRecord = entity_index.get(entityId);
        int rowFound = nextArchetype.columns.length;
        for (int i = 0; i <= nextArchetype.columns.length; i++) {
            if (i == nextArchetype.columns.length) {
                nextArchetype.expand();
            } else if (nextArchetype.columns[0][i] == null) {
                rowFound = i;
                break;
            }
        }
        //update entityRecord with new row and new archetype
        entityRecord.row = rowFound;
        entityRecord.archetype = nextArchetype;
        //remove an entity from the previous archetype at previousRow
        for (JECSComponent[] column : archetype.columns) {
            JECSComponent component = column[previousRow];
            column[previousRow] = null;
            //add it to the nextArchetype at the new row
            ArchetypeRecord archetypeRecord = component_index.get(component.className()).get(nextArchetype.id);
            if (archetypeRecord == null) { continue; } // this component was removed from the entity
            nextArchetype.columns[archetypeRecord.column][rowFound] = component;
        }
    }

    //PUBLIC METHODS
    public int spawn(JECSComponent[] list) {
        //TODO: make the entity spawn, initially as archetype []
        return -1;
    }
    public boolean contains(int entityId) {
        return entity_index.containsKey(entityId);
    }
    public void insert(int entityId, JECSComponent componentInstance) {
        String component = componentInstance.className();
        EntityRecord entityRecord = entity_index.get(entityId);
        Archetype archetype = entityRecord.archetype;
        Archetype nextArchetype = archetype.edges.get(component).add;
        if (nextArchetype == null) { //create archetypes lazily
            nextArchetype = addToArchetype(archetype, component);
        }
        moveEntity(entityId, archetype, entityRecord.row, nextArchetype); // move entity to new archetype
        // add componentInstance to new archetype
        int column = component_index.get(component).get(nextArchetype.id).column;
        nextArchetype.columns[column][entityRecord.row] = componentInstance;
    }
    public void despawn(int entityId) {
        //TODO: remove entity from entity_index and the archetype
    }
    public void remove(int entityId, String componentName) {
        EntityRecord entityRecord = entity_index.get(entityId);
        Archetype archetype = entityRecord.archetype;
        Archetype nextArchetype = archetype.edges.get(componentName).remove;
        moveEntity(entityId, archetype, entityRecord.row, nextArchetype);
    }
    public JECSComponent get(int entityId, String component) {
        EntityRecord entityRecord = entity_index.get(entityId);
        Archetype archetype = entityRecord.archetype;
        HashMap<Integer, ArchetypeRecord> archetypes = component_index.get(component);
        ArchetypeRecord archetypeRecord = archetypes.get(archetype.id);
        return archetype.columns[archetypeRecord.column][entityRecord.row];
    }
}