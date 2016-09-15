/*
 * Copyright 2016 Hippo Seven
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

package com.hippo.image;

/*
 * Created by Hippo on 9/12/2016.
 */

import android.graphics.Bitmap;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class BitmapDecoder {
    private BitmapDecoder() {}

    private static final String LOG_TAG = BitmapDecoder.class.getSimpleName();

    @IntDef({CONFIG_AUTO, CONFIG_RGB_565, CONFIG_ARGB_8888})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Config {}

    /**
     * The same as {@link #CONFIG_RGB_565} if the image is opaque,
     * otherwise {@link #CONFIG_ARGB_8888}
     */
    public static final int CONFIG_AUTO = 0;
    /**
     * The same as {@link android.graphics.Bitmap.Config#RGB_565}.
     */
    public static final int CONFIG_RGB_565 = 1;
    /**
     * The same as {@link android.graphics.Bitmap.Config#ARGB_8888}.
     */
    public static final int CONFIG_ARGB_8888 = 2;

    /**
     * Only decode image info.
     *
     * @param is The image source.
     * @param info The struct to set image info.
     * @return Return {@code true} if
     */
    public static boolean decode(InputStream is, @NonNull ImageInfo info) {
        return nativeDecodeInfo(is, info);
    }

    /**
     * config is {@code CONFIG_AUTO}.
     * ratio is {@code 1}.
     *
     * @see #decode(InputStream, int, int)
     */
    public static Bitmap decode(InputStream is) {
        return nativeDecodeBitmap(is, CONFIG_AUTO, 1);
    }

    /**
     * ratio is {@code 1}.
     *
     * @see #decode(InputStream, int, int)
     */
    public static Bitmap decode(InputStream is, @Config int config) {
        return nativeDecodeBitmap(is, config, 1);
    }

    /**
     * Decode bitmap from {@code InputStream}. Return {@code null} if out of memory.
     *
     * @param is The image source.
     * @param config One of {@link #CONFIG_AUTO}, {@link #CONFIG_RGB_565} and {@link #CONFIG_ARGB_8888}
     * @param ratio If set to a value > 1, requests the decoder to subsample the original.
     *               image, returning a smaller image to save memory. Power of 2 is not necessary.
     * @return The decoded bitmap, or null if the image data could not be
     *         decoded.
     */
    public static Bitmap decode(InputStream is, @Config int config, int ratio) {
        return nativeDecodeBitmap(is, config, ratio);
    }

    // For native code
    private static Bitmap createBitmap(int width, int height, int config) {
        final Bitmap.Config conf;
        switch (config) {
            case CONFIG_RGB_565:
                conf = Bitmap.Config.RGB_565;
                break;
            case CONFIG_ARGB_8888:
                conf = Bitmap.Config.ARGB_8888;
                break;
            default:
                Log.e(LOG_TAG, "Can't convert this config to Bitmap.Config: " + config);
                return null;
        }

        try {
            // TODO remove
            Log.d(LOG_TAG, "Create bitmap " + width + " x " + height + " " + conf.name());
            return Bitmap.createBitmap(width, height, conf);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to create bitmap", e);
            return null;
        }
    }

    static {
        System.loadLibrary("image");
    }

    private static native boolean nativeDecodeInfo(InputStream is, ImageInfo info);

    private static native Bitmap nativeDecodeBitmap(InputStream is, int config, int ratio);
}
