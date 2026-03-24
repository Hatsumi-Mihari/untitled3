package GL_Engine;

public class GEngine_Pixel_FN {

    public GEngine_Pixel_FN(){}

    public int set_pixel( int tr, int tg, int tb, int ta){
        if (tr < 0) tr = 0;
        if (tg < 0) tg = 0;
        if (tb < 0) tb = 0;
        if (ta < 0) ta = 0;

        return tr & 0xFF | tg << 8 | tb << 16 | ta << 24;
    }

    public void get_unzip_pixel(int pixel, int[] pixel_unzip){
        pixel_unzip[0] = (pixel) & 0xFF;
        pixel_unzip[1] = (pixel >>> 8) & 0xFF;
        pixel_unzip[2] = (pixel >>> 16) & 0xFF;
        pixel_unzip[3] = (pixel >>> 24) & 0xFF;
    }

    public void debug_pixel_fn(int pixelD){
        int[] pixel = new int[4];
        this.get_unzip_pixel(pixelD, pixel);
        System.out.printf("\nR = %d, G = %d, B = %d, A = %d", pixel[0], pixel[1], pixel[2], pixel[4]);
    }
}
