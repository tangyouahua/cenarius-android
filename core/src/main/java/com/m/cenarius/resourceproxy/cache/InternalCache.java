package com.m.cenarius.resourceproxy.cache;

import android.content.Context;
import android.text.TextUtils;

import com.m.cenarius.route.Route;
import com.m.cenarius.utils.AppContext;
import com.m.cenarius.utils.io.IOUtils;
import com.m.cenarius.Constants;
import com.m.cenarius.utils.LogUtils;
import com.jakewharton.disklrucache.DiskLruCache;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 默认缓存池，js，css，png等资源会用默认缓存池来存储，是有大小限制的{@code DiskLruCache}
 *
 * 存储位置在/data/data/cache下
 */
class InternalCache implements ICache {

    public static final String TAG = InternalCache.class.getSimpleName();

    private DiskLruCache mDiskCache;

    public InternalCache() {
        File directory = new File(AppContext.getInstance().getDir(Constants.CACHE_HOME_DIR,
                Context.MODE_PRIVATE), Constants.DEFAULT_DISK_FILE_PATH);
        try {
            mDiskCache = DiskLruCache.open(directory, Constants.VERSION, 2,
                    Constants.CACHE_SIZE);
        } catch (IOException e) {
            LogUtils.e(TAG, e.getMessage());
        }
    }

    public boolean putCache(Route route, byte[] bytes) {
        if (null == mDiskCache) {
            return false;
        }
        DiskLruCache.Editor editor = null;
        String key = route.fileHash;
        // 如果存在，则先删掉之前的缓存
        removeCache(route);
        OutputStream outputStream = null;
        try {
            editor = mDiskCache.edit(key);
            if (null == editor) {
                return false;
            }
            editor.set(0, String.valueOf(bytes.length));
            outputStream = editor.newOutputStream(1);
            outputStream.write(bytes);
            outputStream.flush();
            editor.commit();
            editor = null;
            return true;
        } catch (Exception e) {
            LogUtils.e(TAG, e.getMessage());
            try {
                mDiskCache.remove(key);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } finally {
            if (null != editor) {
                try {
                    editor.commit();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != outputStream) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public CacheEntry getCache(Route route) {
        if (null == mDiskCache) {
            return null;
        }
        InputStream inputStream = null;
        try {
            DiskLruCache.Snapshot snapshot = mDiskCache.get(route.fileHash);
            if (null == snapshot) {
                return null;
            }
            LogUtils.i(TAG, "hit");
            long length = 0;
            String storedLength = snapshot.getString(0);
            if (!TextUtils.isEmpty(storedLength)) {
                length = Long.parseLong(storedLength);
            }
            inputStream = snapshot.getInputStream(1);
            return new CacheEntry(length, new ByteArrayInputStream(IOUtils.toByteArray(inputStream)));
        } catch (Exception e) {
            LogUtils.e(TAG, e.getMessage());
        } finally {
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public void clear() {
        if (null != mDiskCache) {
            try {
                mDiskCache.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public CacheEntry findCache(Route route) {
        return getCache(route);
    }

    @Override
    public boolean removeCache(Route route) {
        try {
            LogUtils.i(TAG, "remove cache  : url " + route.uri);
            return mDiskCache.remove(route.fileHash);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
