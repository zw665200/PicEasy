package com.picfix.tools.utils;

import android.content.Context;
import android.graphics.Bitmap;

import com.picfix.tools.callback.HttpCallback;
import com.picfix.tools.config.Constant;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Herr_Z
 * @description:
 * @date : 2021/8/26 11:25
 */
public class BitmapUtil {

    public static void createBMP(Context context, Bitmap bitmap, HttpCallback callback) {
        if (bitmap != null) {
            int w = bitmap.getWidth(), h = bitmap.getHeight();
            int[] pixels = new int[w * h];
            bitmap.getPixels(pixels, 0, w, 0, 0, w, h);

            byte[] rgb = addBMP_RGB_888(pixels, w, h);
            byte[] header = addBMPImageHeader(rgb.length);
            byte[] infos = addBMPImageInfosHeader(w, h);
            byte[] buffer = new byte[54 + rgb.length];
            System.arraycopy(header, 0, buffer, 0, header.length);
            System.arraycopy(infos, 0, buffer, 14, infos.length);
            System.arraycopy(rgb, 0, buffer, 54, rgb.length);
            try {
                FileOutputStream fos = new FileOutputStream(FileUtil.getSDPath(context)
                        + Constant.PICTURE_PATH + System.currentTimeMillis() + ".bmp");
                fos.write(buffer);
                callback.onSuccess();
            } catch (IOException e) {
                e.printStackTrace();
                callback.onFailed("生成失败");
            }
        }
    }


    //BMP文件头
    private static byte[] addBMPImageHeader(int size) {
        byte[] buffer = new byte[14];
        buffer[0] = 0x42;
        buffer[1] = 0x4D;
        buffer[2] = (byte) (size);
        buffer[3] = (byte) (size >> 8);
        buffer[4] = (byte) (size >> 16);
        buffer[5] = (byte) (size >> 24);
        buffer[6] = 0x00;
        buffer[7] = 0x00;
        buffer[8] = 0x00;
        buffer[9] = 0x00;
        buffer[10] = 0x36;
        buffer[11] = 0x00;
        buffer[12] = 0x00;
        buffer[13] = 0x00;
        return buffer;
    }


    //BMP文件信息头
    private static byte[] addBMPImageInfosHeader(int w, int h) {
        byte[] buffer = new byte[40];
        buffer[0] = 0x28;
        buffer[1] = 0x00;
        buffer[2] = 0x00;
        buffer[3] = 0x00;
        buffer[4] = (byte) (w);
        buffer[5] = (byte) (w >> 8);
        buffer[6] = (byte) (w >> 16);
        buffer[7] = (byte) (w >> 24);
        buffer[8] = (byte) (h);
        buffer[9] = (byte) (h >> 8);
        buffer[10] = (byte) (h >> 16);
        buffer[11] = (byte) (h >> 24);
        buffer[12] = 0x01;
        buffer[13] = 0x00;
        buffer[14] = 0x18;
        buffer[15] = 0x00;
        buffer[16] = 0x00;
        buffer[17] = 0x00;
        buffer[18] = 0x00;
        buffer[19] = 0x00;
        buffer[20] = 0x00;
        buffer[21] = 0x00;
        buffer[22] = 0x00;
        buffer[23] = 0x00;
        buffer[24] = (byte) 0xE0;
        buffer[25] = 0x01;
        buffer[26] = 0x00;
        buffer[27] = 0x00;
        buffer[28] = 0x02;
        buffer[29] = 0x03;
        buffer[30] = 0x00;
        buffer[31] = 0x00;
        buffer[32] = 0x00;
        buffer[33] = 0x00;
        buffer[34] = 0x00;
        buffer[35] = 0x00;
        buffer[36] = 0x00;
        buffer[37] = 0x00;
        buffer[38] = 0x00;
        buffer[39] = 0x00;
        return buffer;
    }


    private static byte[] addBMP_RGB_888(int[] b, int w, int h) {
        int len = b.length;
        System.out.println(b.length);
        byte[] buffer = new byte[w * h * 3];
        int offset = 0;
        for (int i = len - 1; i >= w; i -= w) {
            //DIB文件格式最后一行为第一行，每行按从左到右顺序
            int start = i - w + 1;
            for (int j = start; j <= i; j++) {
                buffer[offset] = (byte) (b[j]);
                buffer[offset + 1] = (byte) (b[j] >> 8);
                buffer[offset + 1] = (byte) (b[j] >> 16);
                offset += 3;
            }
        }
        return buffer;
    }
}
