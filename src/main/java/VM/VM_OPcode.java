package VM;

import GL_Engine.GEngine_Functions;
import Logger.Logger;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class VM_OPcode {
    private static final List<Consumer<byte[]>> COMMANDS = new ArrayList<>(Collections.nCopies(255, null));
    private GEngine_Functions Gapi;
    private VM_ByteT byteEncode = new VM_ByteT();
    private VM_Context ctx;
    private Logger logger;
    private boolean logger_added;

    private void Clear(byte[] payload){
        this.Gapi.fill_color(0,0,0,0);
        logger.LOGI("Clear Frame Buffer");
    }

    private void FillColor(byte[] payload){
        this.Gapi.fill_color(
                payload[0] & 0xFF,
                payload[1] & 0xFF,
                payload[2] & 0xFF,
                payload[3]& 0xFF
        );

        logger.Log(String.format("Fill Frame Buffer Color: 0x%02X%02X%02X",
                payload[0] & 0xFF,
                payload[1] & 0xFF,
                payload[2] & 0xFF), "INFO FillColor"
        );
    }

    private void DRAW_PIXEL(byte[] payload){
        int color_r1 = payload[0] & 0xFF |
                ((payload[1] & 0xFF) << 8) |
                ((payload[2] & 0xFF) << 16 ) |
                ((payload[3] & 0xFF) << 24 ) ;

        int[] pos1 = new int[2];

        switch (payload[4]){
            case 0x00:
                pos1[0] = this.byteEncode.BE16_encode(payload[5], payload[6]);
                pos1[1] = this.byteEncode.BE16_encode(payload[7], payload[8]);
                break;
            case 0x01:
                pos1[0] = this.ctx.register_u[payload[5] & 0xFF];
                pos1[1] = this.ctx.register_u[payload[6] & 0xFF];
                break;
        }


        this.Gapi.set_pixel_xy(color_r1, pos1);

        logger.Log(String.format("Pixel seted, pos: x = %d, y = %d",pos1[0], pos1[1]),
                "INFO SetPixel POS");
        logger.Log(String.format("Color = 0x%02X%02X%02X",
                        payload[0] & 0xFF,
                        payload[1] & 0xFF,
                        payload[2] & 0xFF,
                        payload[3] & 0xFF),
                "INFO SetPixel COLOR");
    }

    private void CrateGradientRGBA(byte[] payload){
        int color_r1 = payload[0] & 0xFF |
                ((payload[1] & 0xFF) << 8) |
                ((payload[2] & 0xFF) << 16 ) |
                ((payload[3] & 0xFF) << 24 ) ;

        int color_r2 = payload[4] & 0xFF |
                ((payload[5] & 0xFF) << 8) |
                ((payload[6] & 0xFF) << 16 ) |
                ((payload[7] & 0xFF) << 24 ) ;

        int[] pos1 = {
                this.byteEncode.BE16_encode(payload[8], payload[9]),
                this.byteEncode.BE16_encode(payload[10], payload[11])
        };

        int[] pos2 = {
                this.byteEncode.BE16_encode(payload[12], payload[13]),
                this.byteEncode.BE16_encode(payload[14], payload[15])
        };

        this.Gapi.create_gradient_rgba(color_r1, color_r2, pos1, pos2);

        logger.Log(String.format("Point1: x =  %d, y = %d | Point2: x = %d, y = %d",
                        pos1[0], pos1[1],
                        pos2[0], pos2[1]),
                "INFO CrateGradientRGBA POS");
        logger.Log(String.format("Colors: = 0x%02X%02X%02X_%02X | 0x%02X%02X%02X_%02X",
                        payload[0] & 0xFF,
                        payload[1] & 0xFF,
                        payload[2] & 0xFF,
                        payload[3] & 0xFF,

                        payload[4] & 0xFF,
                        payload[5] & 0xFF,
                        payload[6] & 0xFF,
                        payload[7] & 0xFF),
                "INFO CrateGradientRGBA COLOR");
    }

    private void ReSizeFBO(byte[] payload){
        int x = this.byteEncode.BE16_encode(payload[0], payload[1]);
        int y = this.byteEncode.BE16_encode(payload[2], payload[3]);

        this.Gapi.resize_FBO(x,y);
        logger.Log(String.format("FBO set size: %dx%d", x,y), "INFO ReSizeFBO");
    }






    private void JMP(byte[] payload){
        this.ctx.cursor = this.byteEncode.BE16_encode(payload[0], payload[1]);
        this.ctx.register[0x00] =  this.byteEncode.BE16_encode(payload[0], payload[1]);
        logger.Log(String.format("ADR: 0x%04X", this.ctx.cursor), "INFO, JMP TO");
    }

    private void LOAD(byte[] payload){
        if (payload[0] < 0 || payload[0] >= this.ctx.register_u.length) {
            this.logger.LOGE("Index out of range: index = " + payload[0]);
            return;
        }

        this.ctx.register_u[payload[0]] = this.byteEncode.BE32_encode(new byte[]{payload[1], payload[2], payload[3], payload[4]});
        logger.Log(String.format("REGISTER_U: %d, SET: %d",
                        payload[0] & 0xFF, this.ctx.register_u[payload[0]]),
                "INFO SET REG_U");
    }

    public void CMP(byte[] payload){
        if (payload[0] < 0 || payload[0] >= this.ctx.register_u.length) {
            this.logger.LOGE("Index arg 1, out of range: index = " + payload[0]);
            return;
        }

        if (payload[2] < 0 || payload[2] >= this.ctx.register_u.length) {
            this.logger.LOGE("Index arg 3, out of range: index = " + payload[2]);
            return;
        }

        switch (payload[1]){
            case 0x00:
                if (this.ctx.register_u[payload[0]] == this.ctx.register_u[payload[2]]) this.ctx.register_flags[0] = true;
                else this.ctx.register_flags[0] = false;
                break;
            case 0x01:
                if (this.ctx.register_u[payload[0]] < this.ctx.register_u[payload[2]]) this.ctx.register_flags[0] = true;
                else this.ctx.register_flags[0] = false;
                break;
            case 0x02:
                if (this.ctx.register_u[payload[0]] > this.ctx.register_u[payload[2]]) this.ctx.register_flags[0] = true;
                else this.ctx.register_flags[0] = false;
                break;
            case 0x03:
                if (this.ctx.register_u[payload[0]] <= this.ctx.register_u[payload[2]]) this.ctx.register_flags[0] = true;
                else this.ctx.register_flags[0] = false;
                break;
            case 0x04:
                if (this.ctx.register_u[payload[0]] >= this.ctx.register_u[payload[2]]) this.ctx.register_flags[0] = true;
                else this.ctx.register_flags[0] = false;
                break;
            case 0x05:
                if (this.ctx.register_u[payload[0]] != this.ctx.register_u[payload[2]]) this.ctx.register_flags[0] = true;
                else this.ctx.register_flags[0] = false;
                break;
        }

        int lable = this.byteEncode.BE16_encode(payload[3], payload[4]);
        if (this.ctx.register_flags[0] != true) {
            this.ctx.cursor = lable;
        }

        logger.Log(String.format("value 1 = %d, value 2 = %d, cmp_op = 0x%02X, flag = %b",
                        this.ctx.register_u[payload[0]], this.ctx.register_u[payload[2]], payload[1], this.ctx.register_flags[0]),
                "INFO CMP");
    }

    public void INC(byte[] payload){
        if (payload[0] < 0 || payload[0] >= this.ctx.register.length) {
            this.logger.LOGE("Index out of range: index = " + payload[0]);
            return;
        }

        int value_inc = this.byteEncode.BE32_encode( new byte[]{payload[1], payload[2], payload[3], payload[4]});
        this.ctx.register_u[payload[0]] += value_inc;

        logger.Log(String.format("REGISTER_U: %d, SET: %d",
                        this.ctx.register_u[1] & 0xFF, this.ctx.register_u[payload[0]]),
                "INFO INC");
    }

    public VM_OPcode(){
        COMMANDS.set(0x00, this::ReSizeFBO);
        COMMANDS.set(0x01, this::FillColor);
        COMMANDS.set(0x02, this::Clear);
        COMMANDS.set(0x03, this::CrateGradientRGBA);
        COMMANDS.set(0x04, this::DRAW_PIXEL);

        COMMANDS.set(0xF7, this::INC);
        COMMANDS.set(0xF8, this::CMP);
        COMMANDS.set(0xF9, this::LOAD);
        COMMANDS.set(0xFB, this::JMP);
    }

    public void setLogger(Logger logger){
        this.logger_added = true;
        this.logger = logger;
    }

    public void VM_context(GEngine_Functions Gapi){
        this.Gapi = Gapi;
    }

    public void execOpcode(int opcodeID, byte[] payload, VM_Context ctx){
        try{
            this.ctx = ctx;
            this.COMMANDS.get(opcodeID).accept(payload);
        }catch (Exception e){
            if (logger != null){
                logger.LOGE(String.format("Not found opcode, ID CODE: 0x%02X | cursor index: %d", opcodeID & 0xFF, ctx.cursor));
                logger.LOGE(e.getMessage());
                e.printStackTrace();
            }else{
                System.out.println(e.getMessage());
            }

        }

    }
}
