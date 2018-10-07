package nxt.addons;

import org.json.simple.JSONArray;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

/**
 * Delegate json array operations to json simple and wrap it with convenience methods
 * This class does not really keep a list, but it implements a list in order to delegate iteration to the underlying JSONArray
 * in order to support streaming into String.
 */
public class JA extends AbstractList {

    private final JSONArray ja;

    public JA() {
        this.ja = new JSONArray();
    }

    public JA(JSONArray ja) {
        this.ja = ja;
    }

    public JA(Object ja) {
        this.ja = (JSONArray)ja;
    }

    public JSONArray toJSONArray() {
        return ja;
    }

    public int size() {
        return ja.size();
    }

    public Iterator<JO> iterator() {
        List<Object> lo = new ArrayList<>(ja);
        List<JO> list = lo.stream().map(JO::valueOf).collect(Collectors.toList());
        return list.iterator();
    }

    public List<String> values() {
        List<Object> lo = (List)ja;
        return lo.stream().map(e -> (String)e).collect(Collectors.toList());
    }

    public List<JO> objects() {
        List<Object> lo = (List)ja;
        return lo.stream().map(e -> e instanceof JO ? (JO)e : new JO(e)).collect(Collectors.toList());
    }

    public void add(JO jo) {
        ja.add(jo.toJSONObject());
    }

    public JO get(int i) {
        return new JO(ja.get(i));
    }

    public Object getObject(int i) {
        return ja.get(i);
    }

    /**
     * Required by JSON#encodeArray()
     * @return iterator
     */
    @Override
    public ListIterator listIterator() {
        return ja.listIterator();
    }
}
