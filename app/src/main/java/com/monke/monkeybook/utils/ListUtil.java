package com.monke.monkeybook.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ListUtil {

    public static <T> List<T> filter(List<T> list, ListLook<T> hook) {
        ArrayList<T> r = new ArrayList<>();
        for (T t : list) {
            if (hook.test(t)) {
                r.add(t);
            }
        }
        r.trimToSize();
        return r;
    }

    public static <T> List<T> removeDuplicate(List<T> list, Comparator<T> comparator) {
        if (list == null) return new ArrayList<>();
        Set<T> set = new TreeSet<>(comparator);
        set.addAll(list);
        return new ArrayList<>(set);
    }

    public interface ListLook<T> {
        boolean test(T t);
    }


}
