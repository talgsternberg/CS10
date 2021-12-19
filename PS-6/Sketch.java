import java.util.Set;
import java.util.TreeMap;

/**
 * Holds a map of shapes, where each shape has an ID
 */
public class Sketch {
    protected TreeMap<Integer, Shape> sketch;
    protected int currID;


    public Sketch() {
        sketch = new TreeMap<>();
        currID = 0;
    }

    public void addShape(Shape s, int ID) { sketch.put(ID, s); }

    public Shape getShape(int ID) { return sketch.get(ID); }

    public TreeMap<Integer, Shape> getSketch() { return sketch; }

    public Set<Integer> getDescendingIDs() { return sketch.descendingKeySet(); }

    public Set<Integer> getNavigableIDs() { return sketch.navigableKeySet(); }

    // every time you call this, you're adding a new shape but you want to get the old shape's id
    public int getID() {
        currID++;
        return currID-1;
    }

    public void processRequest(Request r) {
        // see what type is
        // do correct action (draw – put in new shape) (move it – call the shape's move method – do short assignment)
        // (process recolor) (else: delete request – remove it from the map)

    }

    @Override
    public String toString() {
        String s = "";
        for (Integer ID : getNavigableIDs()) {
            s += sketch.get(ID) + ":";
        }
        return s;
    }
}
