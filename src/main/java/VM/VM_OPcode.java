package VM;

import GL_Engine.GEngine_Functions;
import GL_Engine.GEngine_Main;
import Logger.Logger;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class VM_OPcode {
    private static final List<Consumer<byte[]>> COMMANDS = new ArrayList<>(Collections.nCopies(255, null));
    private GEngine_Functions Gapi;
    private VM_ByteT byteEncode = new VM_ByteT();
    private VM_Context ctx;
    private Logger logger;
    private BiConsumer<Byte, byte[]> callback_render;
    private boolean logger_added;

    ///////////////////////////////////RENDER OPCODES//////////////////////////////////////////
    private void Clear(byte[] payload){
        this.Gapi.fill_color(0,0,0,0);
        if(this.logger.state_log_render_calls()) logger.LOGI("Clear Frame Buffer");
    }

    private void FillColor(byte[] payload){
        int[] color = new int[4];
        if(((payload[0] >> 7) & 1 )== 1){
            color[0] = payload[1] & 0xFF;
            color[1] = payload[2] & 0xFF;
            color[2] = payload[3] & 0xFF;
            color[3] = payload[4] & 0xFF;
        }else{
            this.Gapi.decode_color(color, this.ctx.register_u[payload[4]]);
        }

        this.Gapi.fill_color(
                color[0] & 0xFF,
                color[1] & 0xFF,
                color[2] & 0xFF,
                color[3]& 0xFF
        );

        if(this.logger.state_log_render_calls())logger.Log(String.format("Fill Frame Buffer Color: 0x%02X%02X%02X",
                payload[0] & 0xFF,
                payload[1] & 0xFF,
                payload[2] & 0xFF), "INFO FillColor"
        );
    }

    private void DRAW_PIXEL(byte[] payload){
        int color_r1 = 0;
        int[] pos1 = {0,0};
        if(((payload[0] >> 7) & 1 )== 1){
            color_r1 = payload[1] & 0xFF |
                    ((payload[2] & 0xFF) << 8) |
                    ((payload[3] & 0xFF) << 16 ) |
                    ((payload[4] & 0xFF) << 24 ) ;
        }else{
            color_r1 = this.ctx.register_u[payload[4]];
        }

        if(((payload[0] >> 6) & 1 )== 1){
            pos1[0] = this.byteEncode.BE16_encode(payload[5], payload[6]);
        }else{
            pos1[0] = this.ctx.register_u[payload[6]];
        }

        if(((payload[0] >> 5) & 1 )== 1){
            pos1[1] = this.byteEncode.BE16_encode(payload[7], payload[8]);
        }else{
            pos1[1] = this.ctx.register_u[payload[8]];
        }


        this.Gapi.set_pixel_xy(color_r1, pos1);

        if(this.logger.state_log_render_calls())logger.Log(String.format("Pixel seted, pos: x = %d, y = %d",pos1[0], pos1[1]),
                "INFO SetPixel POS");
        if(this.logger.state_log_render_calls())logger.Log(String.format("Color = 0x%02X%02X%02X",
                        payload[0] & 0xFF,
                        payload[1] & 0xFF,
                        payload[2] & 0xFF,
                        payload[3] & 0xFF),
                "INFO SetPixel COLOR");
    }

    private void CrateGradientRGBA(byte[] payload){
        int color_r1 = 0;
        int color_r2 = 0;
        int[] pos1 = {0,0};
        int[] pos2 = {0,0};
        int angle = 0;
        if(((payload[0] >> 7) & 1 )== 1){
            color_r1 = payload[1] & 0xFF |
                    ((payload[2] & 0xFF) << 8) |
                    ((payload[3] & 0xFF) << 16 ) |
                    ((payload[4] & 0xFF) << 24 ) ;
        }else{
            color_r1 =  this.ctx.register_u[payload[4]];
        }

        if(((payload[0] >> 6) & 1 )== 1){
            color_r2 = payload[5] & 0xFF |
                    ((payload[6] & 0xFF) << 8) |
                    ((payload[7] & 0xFF) << 16 ) |
                    ((payload[8] & 0xFF) << 24 ) ;
        }else{
            color_r2 =  this.ctx.register_u[payload[8]];
        }

        if(((payload[0] >> 5) & 1 )== 1){
            pos1[0] = this.byteEncode.BE16_encode(payload[9], payload[10]);
        }else{
            pos1[0] = this.ctx.register_u[payload[10]];
        }

        if(((payload[0] >> 4) & 1 )== 1){
            pos1[1] = this.byteEncode.BE16_encode(payload[11], payload[12]);
        }else{
            pos1[1] = this.ctx.register_u[payload[12]];
        }

        if(((payload[0] >> 3) & 1 )== 1){
            pos2[0] = this.byteEncode.BE16_encode(payload[13], payload[14]);
        }else{
            pos2[0] = this.ctx.register_u[payload[14]];
        }

        if(((payload[0] >> 2) & 1 )== 1){
            pos2[1] = this.byteEncode.BE16_encode(payload[15], payload[16]);
        }else{
            pos2[1] = this.ctx.register_u[payload[16]];
        }


        if(((payload[0] >> 1) & 1 )== 1){
            angle = this.byteEncode.BE16_encode(payload[17], payload[18]);
        }else{
            angle = this.ctx.register_u[payload[18]];
        }

        this.Gapi.create_gradient_rgba(color_r1, color_r2, pos1, pos2, angle);

        if(this.logger.state_log_render_calls())
            logger.Log(String.format("Point1: x =  %d, y = %d | Point2: x = %d, y = %d | Angle = %d | Colors: = 0x%02X%02X%02X_%02X | 0x%02X%02X%02X_%02X",
                        pos1[0], pos1[1],
                        pos2[0], pos2[1],
                        angle,
                        color_r1 & 0xFF,
                        (color_r1 << 8) & 0xFF,
                        (color_r1 << 16) & 0xFF,
                        (color_r1 << 24) & 0xFF,

                        color_r2 & 0xFF,
                        (color_r2 << 8) & 0xFF,
                        (color_r2 << 16) & 0xFF,
                        (color_r2 << 24) & 0xFF),
                "INFO CrateGradientRGBA");
    }

    private void ReSizeFBO(byte[] payload){
        int x = this.byteEncode.BE16_encode(payload[0], payload[1]);
        int y = this.byteEncode.BE16_encode(payload[2], payload[3]);

        this.Gapi.resize_FBO(x,y);
        if(this.logger.state_log_render_calls())
            logger.Log(String.format("FBO set size: %dx%d", x,y), "INFO ReSizeFBO");
    }

    private void SET_WINDOW_DRW(byte[] payload){
        int x0 = this.byteEncode.BE16_encode(payload[0], payload[1]);
        int y0 = this.byteEncode.BE16_encode(payload[2], payload[3]);

        int x1 = this.byteEncode.BE16_encode(payload[4], payload[5]);
        int y1 = this.byteEncode.BE16_encode(payload[6], payload[7]);
        logger.Log(String.format("FBO set size: %dx%d", x1,y1), "INFO ReSizeFBO");
        this.Gapi.SET_WINDOW(x0, y0, x1, y1);
    }

    private void SQR_DRW(byte[] payload){
        int color_r1 = 0;
        int[] pos1 = {0,0};
        int[] pos2 = {0,0};
        if(((payload[0] >> 7) & 1 )== 1){
            color_r1 = payload[1] & 0xFF |
                    ((payload[2] & 0xFF) << 8) |
                    ((payload[3] & 0xFF) << 16 ) |
                    ((payload[4] & 0xFF) << 24 ) ;
        }else{
            color_r1 = this.ctx.register_u[payload[4]];
        }

        if(((payload[0] >> 6) & 1 )== 1){
            pos1[0] = this.byteEncode.BE16_encode(payload[5], payload[6]);
        }else{
            pos1[0] = this.ctx.register_u[payload[6]];
        }

        if(((payload[0] >> 5) & 1 )== 1){
            pos1[1] = this.byteEncode.BE16_encode(payload[7], payload[8]);
        }else{
            pos1[1] = this.ctx.register_u[payload[8]];
        }

        if(((payload[0] >> 4) & 1 )== 1){
            pos2[0] = this.byteEncode.BE16_encode(payload[9], payload[10]);
        }else{
            pos2[0] = this.ctx.register_u[payload[10]];
        }

        if(((payload[0] >> 3) & 1 )== 1){
            pos2[1] = this.byteEncode.BE16_encode(payload[11], payload[12]);
        }else{
            pos2[1] = this.ctx.register_u[payload[12]];
        }
        if(this.logger.state_log_render_calls())
            logger.Log(String.format("Point1: x =  %d, y = %d | Point2: x = %d, y = %d | Colors: = 0x%02X%02X%02X_%02X ",
                        pos1[0], pos1[1],
                        pos2[0], pos2[1],
                        color_r1 & 0xFF,
                        (color_r1 << 8) & 0xFF,
                        (color_r1 << 16) & 0xFF,
                        (color_r1 << 24) & 0xFF),
                "INFO SQR_DRW POS & COLOR");
        this.Gapi.sqr_drw(color_r1, pos1, pos2);
    }

    private void LR_BRS(byte[] payload){
        this.Gapi.liner_brightness(payload[0]);
        if(this.logger.state_log_render_calls())
            logger.Log(String.format("global liner brightness set to %d %%", payload[0]), "INFO LR_BRS");
    }

    private void C_LOAD_HLS (byte[] payload){
        if (payload[1] < 0 || payload[1] >= this.ctx.register_u.length) {
            this.logger.LOGE("Index arg 1, out of range: index = " + payload[1]);
            return;
        }

        byte[] ch = new byte[4];
        int counter = 0;
        int counter_payload = 2;

        for (int i = 6; i >= 3; i--){
            if ((((payload[0]) >> i) & 1)  == 1) {
                ch[counter] = payload[counter_payload];
            }
            if ((((payload[0]) >> i) & 1)  == 0) {
                if (payload[counter_payload] < 0 || payload[counter_payload] >= this.ctx.reg_ch_color.length) {
                    this.logger.LOGE("Index arg 1, out of range: index = " + payload[counter_payload]);
                    return;
                }
                ch[counter] = (byte)this.ctx.reg_ch_color[payload[counter_payload]] ;
            }
            counter++;
            counter_payload++;
        }

        int color_hls = this.Gapi.c_encode_hls(ch[0] & 0xFF, ch[1] & 0xFF, ch[2] & 0xFF, ch[3] & 0xFF);
        this.ctx.register_u[payload[1]] = color_hls;
        if(this.logger.state_log_render_calls())
            logger.Log(String.format("hue = %d, luma = %d, sat = %d, alpha = %d, value_encode = 0x%02X%02X%02X_%02X", ch[0] & 0xFF, ch[1] & 0xFF, ch[2] & 0xFF, ch[3] & 0xFF, ch[0] & 0xFF, ch[1] & 0xFF, ch[2] & 0xFF, ch[3] & 0xFF), "INFO, C_LOAD_HLS");
    }

    private void COLOR_MODIFY (byte[] payload){
        if (payload[1] < 0 || payload[1] >= this.ctx.register_u.length) {
            this.logger.LOGE("Index arg 1, out of range: index = " + payload[1]);
            return;
        }

        if (payload[2] < 0 || payload[2] >= 4) {
            this.logger.LOGE("Index channel color, out of range: index = " + payload[2]);
            return;
        }


        int[] ch = new int[4];
        for(int i = 0; i < ch.length;i++){
            ch[i] = ch[i] & 0xFF;
        }
        this.Gapi.decode_color(ch, this.ctx.register_u[payload[1]]);

        if ((((payload[0]) >> 5) & 1)  == 1) {
            ch[payload[2]] = payload[3];
        } else if ((((payload[0] & 0xFF) >> 5) & 1)  == 0) {
            if (payload[3] < 0 || payload[3] >= this.ctx.register_u.length) {
                this.logger.LOGE("Index arg 3, out of range: index = " + payload[3]);
                return;
            }
            ch[payload[2]] = this.ctx.register_u[payload[3]];
        }

        this.ctx.register_u[payload[1]] = this.Gapi.c_encode_hls(ch[0] & 0xFF, ch[1] & 0xFF, ch[2] & 0xFF, ch[3] & 0xFF);
        if(this.logger.state_log_render_calls())
            logger.Log(String.format("COLOR_MODIFY channels"), "INFO COLOR_MODIFY");
    }

    private void HLS_TO_RGB(byte[] payload){
        if (payload[0] < 0 || payload[0] >= this.ctx.register_u.length) {
            this.logger.LOGE("Index arg 1, out of range: index = " + payload[0]);
            return;
        }

        if (payload[1] < 0 || payload[1] >= this.ctx.register_u.length) {
            this.logger.LOGE("Index arg 2, out of range: index = " + payload[1]);
            return;
        }

        this.ctx.register_u[payload[1]] = this.Gapi.hls_to_rgb(this.ctx.register_u[payload[0]]);
        if(this.logger.state_log_render_calls())
            logger.Log(String.format("HLS_TO_RGB Converted color spase"), "INFO HLS_TO_RGB");
    }

    private void RGB_TO_HLS(byte[] payload){
        if (payload[0] < 0 || payload[0] >= this.ctx.register_u.length) {
            this.logger.LOGE("Index arg 1, out of range: index = " + payload[0]);
            return;
        }

        if (payload[1] < 0 || payload[1] >= this.ctx.register_u.length) {
            this.logger.LOGE("Index arg 2, out of range: index = " + payload[1]);
            return;
        }

        this.ctx.register_u[payload[1]] = this.Gapi.rgb_to_hls(this.ctx.register_u[payload[0]]);
        if(this.logger.state_log_render_calls())
            logger.Log(String.format("RGB_TO_HLS Converted color spase"), "INFO HLS_TO_RGB");
    }

    private void RENDER(byte[] payload){
        this.callback_render.accept((byte)0xFE, this.Gapi.render_byte_fbo());
        this.ctx.register_flags[7] = true;
        if (this.logger.state_log_evets())
            logger.LOGI("RENDER");
    }

///////////////////////////////////BASIC OPCODES//////////////////////////////////////////

    private void JMP(byte[] payload){
        this.ctx.cursor = this.byteEncode.BE16_encode(payload[0], payload[1]);
        this.ctx.register[0x00] =  this.byteEncode.BE16_encode(payload[0], payload[1]);
        if (this.logger.state_log_vm_bs_opcode())
            logger.Log(String.format("ADR: 0x%04X", this.ctx.cursor), "INFO, JMP TO");
    }

    private void LOAD(byte[] payload){
        if (payload[0] < 0 || payload[0] >= this.ctx.register_u.length) {
            this.logger.LOGE("Index out of range: index = " + payload[0]);
            return;
        }

        this.ctx.register_u[payload[0]] = this.byteEncode.BE32_encode(new byte[]{payload[1], payload[2], payload[3], payload[4]});
        if (this.logger.state_log_vm_bs_opcode())
            logger.Log(String.format("REGISTER_U: %d, SET: %d",
                        payload[0] & 0xFF, this.ctx.register_u[payload[0]]),
                "INFO SET REG_U");
    }

    private void LOAD_TIME_NOW(byte[] payload){
        if (payload[0] < 0 || payload[0] >= this.ctx.register_u.length) {
            this.logger.LOGE("Index arg 1, out of range: index = " + payload[0]);
            return;
        }

        logger.Log(String.format("LOAD_TIME_NOW %d ms", this.ctx.register[2]), "INFO LOAD_TIME_NOW");
        this.ctx.register_u[payload[0]] = this.ctx.register[2];
    }

    private void LOAD_TICK(byte[] payload){
        if (payload[0] < 0 || payload[0] >= this.ctx.register_u.length) {
            this.logger.LOGE("Index arg 1, out of range: index = " + payload[0]);
            return;
        }

        logger.Log(String.format("LOAD_TICK %d ms", this.ctx.register[1]), "INFO LOAD_TICK");
        this.ctx.register_u[payload[0]] = this.ctx.register[1];
    }

    private void CMP(byte[] payload){
        if ((((payload[0]) >> 7) & 1)  == 1) {
          this.ctx.register[6] = this.byteEncode.BE32_encodeB(payload[1],payload[2],payload[3],payload[4]);
        }
        if ((((payload[0]) >> 7) & 1)  == 0) {
            if (payload[4] < 0 || payload[4] >= this.ctx.register_u.length) {
                this.logger.LOGE("Index arg 1, out of range: index = " + payload[4]);
                return;
            }
            this.ctx.register[6] = this.ctx.register_u[payload[4]];
        }

        if ((((payload[0]) >> 5) & 1) == 1) {
            this.ctx.register[7] = this.byteEncode.BE32_encodeB(payload[6],payload[7],payload[8],payload[9]);
        }
        if ((((payload[0]) >> 5) & 1)  == 0) {
            if (payload[9] < 0 || payload[9] >= this.ctx.register_u.length) {
                this.logger.LOGE("Index arg 2, out of range: index = " + payload[9]);
                return;
            }
            this.ctx.register[7] = this.ctx.register_u[payload[9]];
        }

        switch (payload[5]){
            case 0x00:
                if (this.ctx.register[6] == this.ctx.register[7]) this.ctx.register_flags[0] = true;
                else this.ctx.register_flags[0] = false;
                break;
            case 0x01:
                if (this.ctx.register[6] < this.ctx.register[7]) this.ctx.register_flags[0] = true;
                else this.ctx.register_flags[0] = false;
                break;
            case 0x02:
                if (this.ctx.register[6] > this.ctx.register[7]) this.ctx.register_flags[0] = true;
                else this.ctx.register_flags[0] = false;
                break;
            case 0x03:
                if (this.ctx.register[6] <= this.ctx.register[7]) this.ctx.register_flags[0] = true;
                else this.ctx.register_flags[0] = false;
                break;
            case 0x04:
                if (this.ctx.register[6] >= this.ctx.register[7]) this.ctx.register_flags[0] = true;
                else this.ctx.register_flags[0] = false;
                break;
            case 0x05:
                if (this.ctx.register[6] != this.ctx.register[7]) this.ctx.register_flags[0] = true;
                else this.ctx.register_flags[0] = false;
                break;
        }

        int lable = this.byteEncode.BE16_encode(payload[10], payload[11]);
        if (this.ctx.register_flags[0] != true) {
            this.ctx.cursor = lable;
        }

        if (this.logger.state_log_vm_bs_opcode())
            logger.Log(String.format("value 1 = %d, value 2 = %d, cmp_op = 0x%02X, flag = %b",
                        this.ctx.register[6] , this.ctx.register[7], payload[5], this.ctx.register_flags[0]),
                "INFO CMP");
    }

    private void INC(byte[] payload){
        if (payload[0] < 0 || payload[0] >= this.ctx.register.length) {
            this.logger.LOGE("Index out of range: index = " + payload[0]);
            return;
        }

        int value_inc = this.byteEncode.BE32_encode( new byte[]{payload[1], payload[2], payload[3], payload[4]});
        this.ctx.register_u[payload[0]] += value_inc;

        if (this.logger.state_log_vm_bs_opcode())
            logger.Log(String.format("REGISTER_U: %d, SET: %d",
                        payload[0] & 0xFF, this.ctx.register_u[payload[0]]),
                "INFO INC");
    }

    private void DEC(byte[] payload){
        if (payload[0] < 0 || payload[0] >= this.ctx.register.length) {
            this.logger.LOGE("Index out of range: index = " + payload[0]);
            return;
        }

        int value_inc = this.byteEncode.BE32_encode( new byte[]{payload[1], payload[2], payload[3], payload[4]});
        this.ctx.register_u[payload[0]] -= value_inc;

        if (this.logger.state_log_vm_bs_opcode())
            logger.Log(String.format("REGISTER_U: %d, SET: %d",
                        payload[0] & 0xFF, this.ctx.register_u[payload[0]]),
                "INFO DEC");
    }

    private void ADD(byte[] payload){
        if ((((payload[0]) >> 6) & 1)  == 1) {
            this.ctx.register[6] = this.byteEncode.BE32_encodeB(payload[2],payload[3],payload[4],payload[5]);
        }
        if ((((payload[0]) >> 6) & 1)  == 0) {
            if (payload[5] < 0 || payload[5] >= this.ctx.register_u.length) {
                this.logger.LOGE("Index arg 1, out of range: index = " + payload[5]);
                return;
            }
            this.ctx.register[6] = this.ctx.register_u[payload[5]];
        }

        if ((((payload[0]) >> 5) & 1)  == 1) {
            this.ctx.register[7] = this.byteEncode.BE32_encodeB(payload[6],payload[7],payload[8],payload[9]);
        }
        if ((((payload[0]) >> 5) & 1)  == 0) {
            if (payload[9] < 0 || payload[9] >= this.ctx.register_u.length) {
                this.logger.LOGE("Index arg 2, out of range: index = " + payload[9]);
                return;
            }
            this.ctx.register[7] = this.ctx.register_u[payload[9]];
        }

        if (payload[1] < 0 || payload[1] >= this.ctx.register_u.length) {
            this.logger.LOGE("Index arg 0, out of range: index = " + payload[9]);
            return;
        }

        this.ctx.register_u[payload[1]] = this.ctx.register[6] + this.ctx.register[7];
    }

    private void SUB(byte[] payload){
        if ((((payload[0]) >> 6) & 1)  == 1) {
            this.ctx.register[6] = this.byteEncode.BE32_encodeB(payload[2],payload[3],payload[4],payload[5]);
        }
        if ((((payload[0]) >> 6) & 1)  == 0) {
            if (payload[5] < 0 || payload[5] >= this.ctx.register_u.length) {
                this.logger.LOGE("Index arg 1, out of range: index = " + payload[5]);
                return;
            }
            this.ctx.register[6] = this.ctx.register_u[payload[5]];
        }

        if ((((payload[0]) >> 5) & 1)  == 1) {
            this.ctx.register[7] = this.byteEncode.BE32_encodeB(payload[6],payload[7],payload[8],payload[9]);
        }
        if ((((payload[0]) >> 5) & 1)  == 0) {
            if (payload[9] < 0 || payload[9] >= this.ctx.register_u.length) {
                this.logger.LOGE("Index arg 2, out of range: index = " + payload[9]);
                return;
            }
            this.ctx.register[7] = this.ctx.register_u[payload[9]];
        }

        if (payload[1] < 0 || payload[1] >= this.ctx.register_u.length) {
            this.logger.LOGE("Index arg 0, out of range: index = " + payload[1]);
            return;
        }

        this.ctx.register_u[payload[1]] = this.ctx.register[6] - this.ctx.register[7];
    }

    private void MUL(byte[] payload){
        if ((((payload[0]) >> 6) & 1)  == 1) {
            this.ctx.register[6] = this.byteEncode.BE32_encodeB(payload[2],payload[3],payload[4],payload[5]);
        }
        if ((((payload[0]) >> 6) & 1)  == 0) {
            if (payload[5] < 0 || payload[5] >= this.ctx.register_u.length) {
                this.logger.LOGE("Index arg 1, out of range: index = " + payload[5]);
                return;
            }
            this.ctx.register[6] = this.ctx.register_u[payload[5]];
        }

        if ((((payload[0]) >> 5) & 1)  == 1) {
            this.ctx.register[7] = this.byteEncode.BE32_encodeB(payload[6],payload[7],payload[8],payload[9]);
        }
        if ((((payload[0]) >> 5) & 1)  == 0) {
            if (payload[9] < 0 || payload[9] >= this.ctx.register_u.length) {
                this.logger.LOGE("Index arg 2, out of range: index = " + payload[9]);
                return;
            }
            this.ctx.register[7] = this.ctx.register_u[payload[9]];
        }

        if (payload[1] < 0 || payload[1] >= this.ctx.register_u.length) {
            this.logger.LOGE("Index arg 0, out of range: index = " + payload[9]);
            return;
        }

        this.ctx.register_u[payload[1]] = this.ctx.register[6] * this.ctx.register[7];
    }

    private void DIV(byte[] payload){
        if ((((payload[0]) >> 6) & 1)  == 1) {
            this.ctx.register[6] = this.byteEncode.BE32_encodeB(payload[2],payload[3],payload[4],payload[5]);
        }
        if ((((payload[0]) >> 6) & 1)  == 0) {
            if (payload[5] < 0 || payload[5] >= this.ctx.register_u.length) {
                this.logger.LOGE("Index arg 1, out of range: index = " + payload[5]);
                return;
            }
            this.ctx.register[6] = this.ctx.register_u[payload[5]];
        }

        if ((((payload[0]) >> 5) & 1)  == 1) {
            this.ctx.register[7] = this.byteEncode.BE32_encodeB(payload[6],payload[7],payload[8],payload[9]);
        }
        if ((((payload[0]) >> 5) & 1)  == 0) {
            if (payload[9] < 0 || payload[9] >= this.ctx.register_u.length) {
                this.logger.LOGE("Index arg 2, out of range: index = " + payload[9]);
                return;
            }
            this.ctx.register[7] = this.ctx.register_u[payload[9]];
        }

        if (payload[1] < 0 || payload[1] >= this.ctx.register_u.length) {
            this.logger.LOGE("Index arg 0, out of range: index = " + payload[9]);
            return;
        }

        this.ctx.register_u[payload[1]] = this.ctx.register[6] / this.ctx.register[7];
    }

    public VM_OPcode(){
        COMMANDS.set(0x00, this::ReSizeFBO);
        COMMANDS.set(0x01, this::FillColor);
        COMMANDS.set(0x02, this::Clear);
        COMMANDS.set(0x03, this::CrateGradientRGBA);
        COMMANDS.set(0x04, this::DRAW_PIXEL);
        COMMANDS.set(0x05, this::SQR_DRW);
        COMMANDS.set(0x06, this::LR_BRS);
        COMMANDS.set(0x07, this::C_LOAD_HLS);
        COMMANDS.set(0x08, this::COLOR_MODIFY);
        COMMANDS.set(0x09, this::HLS_TO_RGB);
        COMMANDS.set(0x0A, this::RENDER);
        COMMANDS.set(0x0B, this::RGB_TO_HLS);

        COMMANDS.set(0xF0, this::DIV);
        COMMANDS.set(0xF1, this::MUL);
        COMMANDS.set(0xF2, this::SUB);
        COMMANDS.set(0xF3, this::ADD);
        COMMANDS.set(0xF4, this::LOAD_TICK);
        COMMANDS.set(0xF5, this::LOAD_TIME_NOW);
        COMMANDS.set(0xF6, this::DEC);
        COMMANDS.set(0xF7, this::INC);
        COMMANDS.set(0xF8, this::CMP);
        COMMANDS.set(0xF9, this::LOAD);
        COMMANDS.set(0xFB, this::JMP);
    }

    public void setLogger(Logger logger){
        this.logger_added = true;
        this.logger = logger;
    }

    public void VM_context(GEngine_Functions Gapi, BiConsumer<Byte, byte[]> callback_render){
        this.Gapi = Gapi;
        this.callback_render = callback_render;
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
