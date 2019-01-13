package de.ebertp.HomeDroid.Utils.Compatibility;

import android.content.Context;
import android.graphics.Bitmap;
import android.provider.MediaStore;

public class ThumbNailGetter {

    public static Bitmap getThumbnail(Context ctx, int imageId) {
        try {
            return MediaStore.Images.Thumbnails.getThumbnail(
                    ctx.getContentResolver(),
                    imageId, MediaStore.Images.Thumbnails.MINI_KIND, null);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();

            try {

                return MediaStore.Images.Thumbnails.getThumbnail(
                        ctx.getContentResolver(),
                        imageId, MediaStore.Images.Thumbnails.MINI_KIND, null);
            } catch (OutOfMemoryError e2) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
