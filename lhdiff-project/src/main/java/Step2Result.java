import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Step2Result {
    public List<int[]> anchors; // Pairs of [oldIndex, newIndex]
    public Set<Integer> unmappedOld;
    public Set<Integer> unmappedNew;

    public Step2Result(int oldSize, int newSize) {
        this.anchors = new ArrayList<>();
        this.unmappedOld = new HashSet<>();
        this.unmappedNew = new HashSet<>();
    }
}
