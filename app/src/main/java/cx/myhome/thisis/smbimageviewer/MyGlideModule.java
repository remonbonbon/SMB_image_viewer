package cx.myhome.thisis.smbimageviewer;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.module.GlideModule;

import java.io.InputStream;

class MyGlideModule implements GlideModule {
    @Override public void applyOptions(Context context, GlideBuilder builder) {
//        int diskCacheSize = 50 * 1024 * 1024;    // MB
//        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, diskCacheSize));

//        MemorySizeCalculator calculator = new MemorySizeCalculator(context);
//        int defaultMemoryCacheSize = calculator.getMemoryCacheSize();
//        int defaultBitmapPoolSize = calculator.getBitmapPoolSize();
//        Log.i("MyGlideModule", "defaultMemoryCacheSize=" + defaultMemoryCacheSize);
//        Log.i("MyGlideModule", "defaultBitmapPoolSize=" + defaultBitmapPoolSize);
        int memoryCacheSize = 100 * 1024 * 1024;  // MB
        int bitmapPoolSize = memoryCacheSize * 2;
        builder.setMemoryCache(new LruResourceCache(memoryCacheSize));
        builder.setBitmapPool(new LruBitmapPool(bitmapPoolSize));

        builder.setDecodeFormat(DecodeFormat.PREFER_ARGB_8888);
    }

    @Override public void registerComponents(Context context, Glide glide) {
        glide.register(String.class, InputStream.class, new SmbLoader.Factory());
    }
}
