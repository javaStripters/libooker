package ru.thecntgfy.libooker.utils;


import lombok.Value;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public record Pair<T>(T left, T right) implements Collection<T> {
    @Override
    public int size() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean contains(Object o) {
        return left.equals(o) && right.equals(o);
    }

    @Override
    public Iterator<T> iterator() {
        return List.of(left, right).iterator();
    }

    @Override
    public Object[] toArray() {
        return new Object[] {left, right};
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return null;
    }

    @Override
    public boolean add(T t) {
        return false;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {}
}
