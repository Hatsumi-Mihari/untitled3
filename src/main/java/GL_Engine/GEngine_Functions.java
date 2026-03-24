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

    public void resize_FBO(int x, int y){
        this.engine.resize_FBO(x,y);
    }
}
