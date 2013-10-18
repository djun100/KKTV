package com.fedorvlasov.lazylist2;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import android.graphics.Bitmap;

public class MemoryCache {
    private HashMap<String, SoftReference<String>> cache=new HashMap<String, SoftReference<String>>();
    
    public String get(String id){
        if(!cache.containsKey(id))
            return null;
        SoftReference<String> ref=cache.get(id);
        return ref.get();
    }
    
    public void put(String id, String string){
        cache.put(id, new SoftReference<String>(string));
    }

    public void clear() {
        cache.clear();
    }
}