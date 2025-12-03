package searchengine.common.list;

import java.util.*;

public class ListUtils {
    public static <T> T getFirst(Collection<T> collection) throws NullPointerException {
        if (collection.isEmpty()) throw new NullPointerException("Collection is empty");

        return collection.iterator().next();
    }

    public static <T> T getLast(Collection<T> collection) throws NullPointerException {
        if (collection.isEmpty()) throw new NullPointerException("Collection is empty");

        T result = null;
        int i = 0;

        for (T item : collection)
            if (++i == collection.size()) result = item;

        return result;
    }

    public static <T extends Number & Comparable<T>> T maxValue(Collection<T> list) {
        if (list.isEmpty()) throw new NullPointerException("Collection is empty");

        List<T> listCopy = new ArrayList<>(list.size());
        listCopy.addAll(list);
        listCopy.sort(Collections.reverseOrder());

        return listCopy.iterator().next();
    }

    public static <T extends Number & Comparable<T>> T minValue(Collection<T> list) {
        if (list.isEmpty()) throw new NullPointerException("Collection is empty");

        List<T> listCopy = new ArrayList<>(list.size());
        listCopy.addAll(list);
        Collections.sort(listCopy);

        return listCopy.iterator().next();
    }

    public static <T> List<T> getSubList(Collection<T> collection, int offset, int limit) throws NullPointerException {
        if (collection.isEmpty()) throw new NullPointerException("Collection is empty");

        return collection.stream().skip(offset).limit(limit).toList();
    }

    public static <T> List<T> getSubList(Collection<T> collection, int offset) throws NullPointerException {
        if (collection.isEmpty()) throw new NullPointerException("Collection is empty");

        return collection.stream().skip(offset).toList();
    }

    public static int findNearestIntValue(int value, List<Integer> list) {
        if (list.isEmpty()) throw new NullPointerException("Collection is empty");

        List<Integer> listCopy = new ArrayList<>(list.size());
        listCopy.addAll(list);
        Collections.sort(listCopy);

        int previousComparison = Integer.MAX_VALUE;
        int result = getFirst(listCopy);

        for (Integer i : listCopy) {
            int actualComparison = Math.abs(value - i);

            if (previousComparison == Integer.MAX_VALUE)
                previousComparison = actualComparison;

            if (actualComparison > previousComparison)
                break;

            previousComparison = actualComparison;
            result = i;
        }

        return result;
    }

    public static <T extends Number & Comparable<T>> List<T> trimValuesToRange(Collection<T> list, T minValue, T maxValue) throws NullPointerException {
        if (list.isEmpty()) throw new NullPointerException("Collection is empty");
        if (minValue == null || maxValue == null) throw new NullPointerException("minValue and maxValue cannot be null");

        List<T> result = new ArrayList<>();

        for (T item : list)
            if (item.compareTo(minValue) >= 0 && item.compareTo(maxValue) <= 0)
                result.add(item);

        return result;
    }
}
