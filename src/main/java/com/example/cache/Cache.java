package com.example.cache;


public class Cache {

    private Retriever mRetriever;
    private CacheStore<String, byte[]> mCacheStore;

    public Cache(CacheStore<String, byte[]> cacheStore, Retriever retriever) {
        this.mRetriever = retriever;
        this.mCacheStore = cacheStore;
    }

    /**
     * Checks if the file referenced in the url is in the cache
     * if it's not then it'll retrieve it, put it in the cache and return to the user
     * @param url
     */
    public byte[] get(String url) {
        if(url == null || url.trim().isEmpty()) {
            return null;
        }

        byte[] data = mCacheStore.retrieve(url);
        if (data != null) {
            return data;
        } else {
            data = this.mRetriever.retrieve(url);

            if (data != null) {
                this.mCacheStore.store(url, data);
            }

            return data;
        }
    }
    
}
