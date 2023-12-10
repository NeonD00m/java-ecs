package JECS;

interface NewArray<T> {
    T[] construct(int capacity);
}

class ResizeArray<T> {
    NewArray<T> constructor;
    T[] arr;
    private int size;
    public ResizeArray(int capacity, NewArray<T> constructor) {
        arr = constructor.construct(capacity);
        this.constructor = constructor;
        size = 0;
    }

    public T get(int index) {
        return arr[index];
    }

    public int next() {
        int index = size;
        while (index < arr.length && arr[index] != null) {
            index++;
        }
        return index;
    }

    public void add(T value) {
        if (size == arr.length) {
            resize(2);
        }
        while (arr[size] != null) {
            size++;
        }
        arr[size++] = value;
    }

    public void set(int index, T value) {
        if (arr.length <= index) {
            resize(1 + Math.floorDiv(index, arr.length));
        }
        arr[index] = value;
    }

    public void remove(int index) {
        arr[index] = null;
    }

    private void resize(int multi) {
        T[] old = arr;
        arr = constructor.construct(old.length * multi);
        System.arraycopy(old, 0, arr, 0, old.length);
    }
}
