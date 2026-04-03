package GL_Engine;

public class GEngine_Functions{
    private GEngine_Main engine;

    public GEngine_Functions(GEngine_Main engine) {
        this.engine = engine;
    }

    public void fill_color(int r, int g, int b, int a){
        int color = engine.pixel_fn_obj.set_pixel(r,g,b,a);

        for (int i = 0; i < engine.FBO.length; i++){
            engine.FBO[i] = color;
        }
    }

    public void set_pixel_xy (int color, int[] pos){
        if (pos[0] > this.engine.size_x || pos[0] < 0) return;
        if (pos[1] > this.engine.size_y || pos[1] < 0) return;

        if (pos[0] != 0) pos[0] -= 1;
        if (pos[1] != 0) pos[1] -= 1;
        this.engine.FBO[this.engine.pixel_fn_obj.get_index_xy(pos, this.engine.size_x, this.engine.size_y)] = color;
    }

    public void create_gradient_rgba(int RGB_R1, int RGB_R2, int[] pos1, int[] pos2){
        int hls_color_base = engine.pixel_fn_obj.RGB_to_HLS(RGB_R1);
        int hls_color_inter = engine.pixel_fn_obj.RGB_to_HLS(RGB_R2);

        int[] pixel_base = new int[4];
        int[] pixel_inter = new int[4];
        int[] pixel_t = new int[4];

        engine.pixel_fn_obj.get_unzip_pixel(hls_color_base, pixel_base);
        engine.pixel_fn_obj.get_unzip_pixel(hls_color_inter, pixel_inter);
        engine.pixel_fn_obj.get_unzip_pixel(RGB_R1, pixel_t);
        pixel_base[3] = pixel_t[3];
        engine.pixel_fn_obj.get_unzip_pixel(RGB_R2, pixel_t);
        pixel_inter[3] = pixel_t[3];

        int index_ips[] = {
                this.engine.pixel_fn_obj.get_index_xy(pos1, this.engine.size_x, this.engine.size_y),
                this.engine.pixel_fn_obj.get_index_xy(pos2, this.engine.size_x, this.engine.size_y)
        };

        int count_pixel = engine.pixel_fn_obj.get_count_pixels(index_ips[0], index_ips[1]);
        int t = 0;

        for (int i = index_ips[0]; i < index_ips[1]+1; i++){
            t = (i - index_ips[0]);
            pixel_t[0] = pixel_base[0] + t * (pixel_inter[0] - pixel_base[0]) / count_pixel;

            if (pixel_inter[1] != pixel_base[1])
                pixel_t[1] = pixel_base[1] + t * (pixel_inter[1] - pixel_base[1]) / count_pixel;
            else pixel_t[1] = pixel_base[1];

            if (pixel_inter[2] != pixel_base[2])
                pixel_t[2] = pixel_base[2] + t * (pixel_inter[2] - pixel_base[2]) / count_pixel;
            else pixel_t[2] = pixel_base[2];

            if (pixel_base[3] != pixel_inter[3])
                pixel_t[3] = pixel_base[3] + t * (pixel_inter[3] - pixel_base[3]) / count_pixel;
            else pixel_t[3] = pixel_base[3];

            hls_color_inter = this.engine.pixel_fn_obj.set_pixel(pixel_t[0], pixel_t[1], pixel_t[2], pixel_t[3]);
            this.engine.FBO[i] = this.engine.pixel_fn_obj.HSL_to_RGB(hls_color_inter);

        }
    }

    public void resize_FBO(int x, int y){
        this.engine.resize_FBO(x,y);
    }
}
