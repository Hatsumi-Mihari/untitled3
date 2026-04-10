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

    public void create_gradient_rgba(int RGB_R1, int RGB_R2, int[] pos1, int[] pos2, int angle){
        int hls_color_base = engine.pixel_fn_obj.RGB_to_HLS(RGB_R1);
        int hls_color_inter = engine.pixel_fn_obj.RGB_to_HLS(RGB_R2);

        int[] pixel_base = new int[4];
        int[] pixel_inter = new int[4];
        int[] pixel_t = new int[4];
        int[] temp_point = new int[2];
        int row_t = 0;

        float angle_rad = angle * 3.14159f / 180.0f;
        double cos_a = Math.cos(angle_rad);
        double sin_a = Math.sin(angle_rad);

        int scale = (pos2[0] - pos1[0]) + 1;
        if (scale <= 0) scale = 1;

        int dt_dx = (int)(cos_a * 65536.0f) / scale;
        int dt_dy = (int)(sin_a * 65536.0f) / scale;
        float cx = (pos1[0] + pos2[0]) / 2.0f;
        float cy = (pos1[1] + pos2[1]) / 2.0f;
        float dx_to_center = cx - pos1[0];
        float dy_to_center = cy - pos1[1];
        row_t = 32768 - (int)((dx_to_center * dt_dx + dy_to_center * dt_dy ));


        engine.pixel_fn_obj.get_unzip_pixel(hls_color_base, pixel_base);
        engine.pixel_fn_obj.get_unzip_pixel(hls_color_inter, pixel_inter);
        engine.pixel_fn_obj.get_unzip_pixel(RGB_R1, pixel_t);
        pixel_base[3] = pixel_t[3];
        engine.pixel_fn_obj.get_unzip_pixel(RGB_R2, pixel_t);
        pixel_inter[3] = pixel_t[3];

        for (int y = pos1[1]; y <= pos2[1]; y++){
            int current_t = row_t;
            int row_offset = y * this.engine.size_x;
            for (int x = pos1[0]; x <= pos2[0]; x++){
                int t = current_t;
                if (t < 0) t = 0; if (t > 65536) t = 65536;

                int alpha = t >> 8;

                pixel_t[0] = pixel_base[0] + (alpha * (pixel_inter[0] - pixel_base[0]) >> 8);
                pixel_t[1] = pixel_base[1] + (alpha * (pixel_inter[1] - pixel_base[1]) >> 8);
                pixel_t[2] = pixel_base[2] + (alpha * (pixel_inter[2] - pixel_base[2]) >> 8);
                pixel_t[3] = pixel_base[3] + (alpha * (pixel_inter[3] - pixel_base[3]) >> 8);

                temp_point[0] = x;
                temp_point[1] = y;
                hls_color_base = this.engine.pixel_fn_obj.set_pixel(pixel_t[0], pixel_t[1], pixel_t[2], pixel_t[3]);
                this.engine.FBO[row_offset + x] = this.engine.pixel_fn_obj.HSL_to_RGB(hls_color_base);
                current_t += dt_dx;
            }
            row_t += dt_dy;
        }
    }

    public void sqr_drw(int RGB_R1, int[] pos1, int[] pos2){
        int[] temp_point = new int[2];

        for (int y = pos1[1]; y <= pos2[1]; y++){
            for (int x = pos1[0]; x <= pos2[0]; x++){
                temp_point[0] = x;
                temp_point[1] = y;
                this.engine.FBO[this.engine.pixel_fn_obj.get_index_xy(temp_point, this.engine.size_x, this.engine.size_y)] = RGB_R1;
            }
        }
    }

    public void SET_WINDOW(int x0, int y0, int x1, int y1){
        this.engine.set_window_draw(x0, y0, x1, y1);
    }

    public void resize_FBO(int x, int y){
        this.engine.resize_FBO(x,y);
    }
}
