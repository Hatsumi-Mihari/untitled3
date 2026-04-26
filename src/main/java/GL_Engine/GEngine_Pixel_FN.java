package GL_Engine;

import Logger.Logger;

import java.util.Arrays;
import java.util.Collections;

public class GEngine_Pixel_FN {
    private Logger logger;
    public GEngine_Pixel_FN(Logger logger ){}

    public int set_pixel( int tr, int tg, int tb, int ta){
        if (tr < 0) tr = 0;
        if (tg < 0) tg = 0;
        if (tb < 0) tb = 0;
        if (ta < 0) ta = 0;

        return tr & 0xFF | (tg & 0xFF) << 8 | (tb & 0xFF)<< 16 | (ta & 0xFF) << 24;
    }

    public void get_unzip_pixel(int pixel, int[] pixel_unzip){
        if (pixel_unzip.length == 4){
            pixel_unzip[0] = (pixel) & 0xFF;
            pixel_unzip[1] = (pixel >>> 8) & 0xFF;
            pixel_unzip[2] = (pixel >>> 16) & 0xFF;
            pixel_unzip[3] = (pixel >>> 24) & 0xFF;
        }else{
            pixel_unzip[0] = (pixel) & 0xFF;
            pixel_unzip[1] = (pixel >>> 8) & 0xFF;
            pixel_unzip[2] = (pixel >>> 16) & 0xFF;
        }
    }

    public int RGB_to_HLS(int color){
        int[] rgb = new int[4];
        this.get_unzip_pixel(color, rgb);

        int r = rgb[0], g = rgb[1], b = rgb[2];
        int max = Math.max(r, Math.max(g, b));
        int min = Math.min(r, Math.min(g, b));
        int delta = max - min;

        int light = (max + min) / 2;
        int sat = 0;
        int hue = 0;

        if (delta != 0) {
            if (light <= 127) {
                sat = (delta * 255) / (max + min);
            } else {
                sat = (delta * 255) / (510 - (max + min));
            }

            if (max == r) {
                hue = 42 * (g - b) / delta;
            } else if (max == g) {
                hue = 42 * (b - r) / delta + 85;
            } else {
                hue = 42 * (r - g) / delta + 170;
            }

            if (hue < 0) hue += 255;
            if (hue > 255) hue -= 255;
        }

        return set_pixel(hue, light, sat, rgb[3]);

    }

    private int hueToRGB(int h, int p, int q){
        if (h < 0) h += 255;
        if (h >= 255) h -= 255;

        if (h * 6 < 255) return p + ((q - p) * 6 * h) / 255; // 0-42
        if (h * 2 < 255) return q;                          // 43-127
        if (h * 3 < 510) return p + ((q - p) * (170 - h) * 6) / 255; // 128-170
        return p;
    }

    public int HSL_to_RGB(int hls_color){
        int[] hls_color_unzip = new int[4];
        this.get_unzip_pixel(hls_color, hls_color_unzip);

        // Hue = index 0, Light = index 1, Sat = index 2

        if (hls_color_unzip[2] == 0) return set_pixel(hls_color_unzip[1], hls_color_unzip[1], hls_color_unzip[1], 255);

        int p = 0;
        int q = 0;

        if (hls_color_unzip[1] <= 127) q = (hls_color_unzip[1] * (255 + hls_color_unzip[2]))/ 255;
        else q = (hls_color_unzip[1] + hls_color_unzip[2]) - ((hls_color_unzip[1] * hls_color_unzip[2])) / 255;
        p = 2 * hls_color_unzip[1] - q;

        int r = hueToRGB(hls_color_unzip[0] + 85, p ,q);
        int g = hueToRGB(hls_color_unzip[0], p ,q);
        int b = hueToRGB(hls_color_unzip[0] - 85, p ,q);

        return set_pixel(r,g,b,hls_color_unzip[3]);
    }

    public int get_index_xy(int[] point, int size_fbo_x, int size_fbo_y) {
        int x = point[0] ;
        int y = point[1] ;

        if (x < 0 || y < 0) return 0;
        if (x >= size_fbo_x || y >= size_fbo_y) return 0;

        return y * size_fbo_x + x;
    }

    public int get_count_pixels(int idxStart, int idxEnd){
        if (idxEnd > idxStart) return idxEnd - idxStart;
        else return idxStart - idxEnd;
    }

    public void debug_pixel_fn(int pixelD){
        int[] pixel = new int[4];
        this.get_unzip_pixel(pixelD, pixel);
        System.out.printf("\nR = %d, G = %d, B = %d, A = %d", pixel[0], pixel[1], pixel[2], pixel[4]);
    }
}
