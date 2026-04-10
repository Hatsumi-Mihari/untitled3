package GL_Engine;

import Logger.Logger;

import javax.naming.SizeLimitExceededException;
import java.util.ArrayList;

public class GEngine_Main {
    protected int[] FBO;
    protected int size_x, size_y;
    protected GEngine_Pixel_FN pixel_fn_obj;
    private byte[] FBO_byte;
    protected int[] unzip_pixel = new int[4];
    private int[] window_dr = new int[4];
    private boolean FBO_byte_out = true;
    protected Logger logger;

    public GEngine_Main(int FBO_size, Logger logger) throws Exception {
        this.logger = logger;
        this.pixel_fn_obj = new GEngine_Pixel_FN(this.logger);
        if(FBO_size <= 0){
            throw new Exception("FBO lower capacity 0");
        }
        this.FBO = new int[FBO_size];

        for(int i = 0; i < this.unzip_pixel.length; i++){
            this.unzip_pixel[i] = 0;
        }

        for(int i = 0; i < this.FBO.length; i++){
            this.FBO[i] = this.pixel_fn_obj.set_pixel(0, 0 , 0,0);
        }

        if (FBO_byte_out) this.FBO_byte = new byte[FBO_size * 4];
    }

    public void resize_FBO(int size_x, int size_y){
        if (size_x < 0) return;
        if (size_y < 0) return;
        this.size_x = size_x;
        this.size_y = size_y;

        this.FBO = new int[size_x * size_y];
        if (FBO_byte_out) this.FBO_byte = new byte[(size_x * size_y) * 4];
    }

    public void set_window_draw(int x0, int y0, int x1, int y1){
        if (x0 > this.size_x | x0 < 0) {
            logger.LOGE("x0 > this.size_x l:49 G_main");
            return;
        }

        if (y0 > this.size_y | y0 < 0) {
            logger.LOGE("y0 > this.size_x l:54 G_main");
            return;
        }

        if (x1 > this.size_x | x1 < 0) {
            logger.LOGE("x1 > this.size_x l:59 G_main");
            return;
        }

        if (y1 > this.size_y | y1 < 0) {
            logger.LOGE("y1 > this.size_x l:64 G_main");
            return;
        }

        this.window_dr[0] = x0;
        this.window_dr[1] = y0;
        this.window_dr[2] = x1;
        this.window_dr[3] = y1;
    }

    public void render_CLI(){
        for (int i = 0; i < FBO.length; i++){
            pixel_fn_obj.get_unzip_pixel(this.FBO[i], this.unzip_pixel);
            System.out.printf("\u001B[38;2;%d;%d;%dm█",
                    this.unzip_pixel[0], this.unzip_pixel[1], this.unzip_pixel[2]);
        }
        System.out.printf("\u001B[0m");
    }

    public byte[] render_ByteArr(){
        int cursor = 0;
        int counter = 0;
        for (int pixel: this.FBO){
            this.pixel_fn_obj.get_unzip_pixel(pixel, this.unzip_pixel);
            for (int i = cursor; i < cursor+4;i++){
                this.FBO_byte[i] = (byte)this.unzip_pixel[counter];
                counter++;
            }
            cursor += 4;
            counter = 0;
        }

        return this.FBO_byte;
    }

    public int[] getSizeFBO(){
        return new int[] {this.size_x, this.size_y};
    };

    public String getSizeFBO_str(){
        String size = this.size_x + "x" + this.size_y;
        return size;
    }
}
