/**
 * Copyright 2013 Alex Yanchenko
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.droidparts.net.image.cache;

import static org.droidparts.contract.Constants.BUFFER_SIZE;
import static org.droidparts.util.IOUtils.getFileList;
import static org.droidparts.util.IOUtils.silentlyClose;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.droidparts.util.HashCalc;
import org.droidparts.util.L;
import org.droidparts.util.ui.BitmapUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.util.Pair;

public class BitmapDiskCache {

	private static final String DEFAULT_DIR = "img";

	private static BitmapDiskCache instance;

	public static BitmapDiskCache getDefaultInstance(Context ctx) {
		if (instance == null) {
			File cacheDir = ctx.getExternalCacheDir();
			if (cacheDir != null) {
				instance = new BitmapDiskCache(new File(cacheDir, DEFAULT_DIR));
			} else {
				L.w("External cache dir null. Lacking 'android.permission.WRITE_EXTERNAL_STORAGE' permission?");
			}
		}
		return instance;
	}

	private final File cacheDir;

	public BitmapDiskCache(File cacheDir) {
		this.cacheDir = cacheDir;
		cacheDir.mkdirs();
	}

	public boolean put(String key, Bitmap bm,
			Pair<CompressFormat, Integer> cacheFormat) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			bm.compress(cacheFormat.first, cacheFormat.second, baos);
			return put(key, baos.toByteArray());
		} catch (Exception e) {
			L.w(e);
			return false;
		} finally {
			silentlyClose(baos);
		}
	}

	public boolean put(String key, byte[] bmArr) {
		File file = getCachedFile(key);
		BufferedOutputStream bos = null;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(file),
					BUFFER_SIZE);
			bos.write(bmArr);
			return true;
		} catch (Exception e) {
			L.w(e);
			return false;
		} finally {
			silentlyClose(bos);
		}
	}

	public Bitmap get(String key, int reqWidth, int reqHeight) {
		Bitmap bm = null;
		File file = getCachedFile(key);
		if (file.exists()) {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(file);
				bm = BitmapUtils.decodeScaled(fis, reqWidth, reqHeight);
				if (bm != null) {
					file.setLastModified(System.currentTimeMillis());
				}
			} catch (Exception e) {
				L.w(e);
			} finally {
				silentlyClose(fis);
			}
		}
		if (bm == null) {
			L.i("Cache miss for '%s'.", key);
		}
		return bm;
	}

	public void purgeFilesAccessedBefore(long timestamp) {
		for (File f : getFileList(cacheDir)) {
			if (f.lastModified() < timestamp) {
				f.delete();
			}
		}
	}

	private File getCachedFile(String key) {
		return new File(cacheDir, HashCalc.getMD5(key));
	}

}