package com.example.cache;

interface CacheStore<K, V> {

    public boolean store(K key, V value);

    public V retrieve(K key);

    public boolean remove(K key);

    public void clear();
    
}
