package cx.myhome.thisis.smbimageviewer;

import android.content.Context;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.GenericLoaderFactory;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.stream.StreamModelLoader;

import java.io.IOException;
import java.io.InputStream;

import jcifs.smb.SmbFile;

class SmbLoader implements StreamModelLoader<String> {
    @Override public DataFetcher<InputStream> getResourceFetcher(final String uri, int width, int height) {
        return new DataFetcher<InputStream>() {
            private InputStream stream = null;

            @Override public InputStream loadData(Priority priority) throws Exception {
                stream = (new SmbFile(uri)).getInputStream();
                return stream;
            }
            @Override public void cleanup() {
                if (stream == null) return;
                try {
                    stream.close();
                    stream = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            @Override public String getId() {
                return uri;
            }
            @Override public void cancel() {
                // do nothing
            }
        };
    }

    public static class Factory implements ModelLoaderFactory<String, InputStream> {
        @Override public ModelLoader<String, InputStream> build(Context context, GenericLoaderFactory factories) {
            return new SmbLoader();
        }
        @Override public void teardown() {
            // nothing to do
        }
    }
}