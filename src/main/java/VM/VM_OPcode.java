package VM;

import GL_Engine.GEngine_Functions;

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

    private void Clear(byte[] payload){
        this.Gapi.fill_color(0,0,0,0);
        System.out.printf("Clear Frame Buffer\n");
    }

    private void FillColor(byte[] payload){
        this.Gapi.fill_color(
                payload[0] & 0xFF,
                payload[1] & 0xFF,
                payload[2] & 0xFF,
                payload[3]& 0xFF
        );

        System.out.printf("Fill Frame Buffer Color: 0x%02X%02X%02X\n",
                payload[0] & 0xFF,
                payload[1] & 0xFF,
                payload[2] & 0xFF
        );
    }

    private void JUMP_TO_SET_START(byte[] payload){
        this.ctx.register[0x00] = this.ctx.cursor;
    }

    private void JMP(byte[] payload){
        this.ctx.cursor = this.ctx.register[0x00];
        //this.ctx.register[0x00] = 0;
    }

    private void ReSizeFBO(byte[] payload){
        int x = this.byteEncode.BE16_encode(payload[0], payload[1]);
        int y = this.byteEncode.BE16_encode(payload[2], payload[3]);

        this.Gapi.resize_FBO(x,y);
        System.out.printf("FBO set size: %dx%d\n", x,y);
    }

    public VM_OPcode(){
        COMMANDS.set(0x00, this::ReSizeFBO);
        COMMANDS.set(0x01, this::FillColor);
        COMMANDS.set(0x02, this::Clear);
        COMMANDS.set(0xFA, this::JUMP_TO_SET_START);
        COMMANDS.set(0xFB, this::JMP);
    }

    public void VM_context(GEngine_Functions Gapi){
        this.Gapi = Gapi;
    }

    public void execOpcode(int opcodeID, byte[] payload, VM_Context ctx){
        try{
            this.ctx = ctx;
            this.COMMANDS.get(opcodeID).accept(payload);
        }catch (Exception e){
            System.out.printf("Not found opcode, ID CODE: 0x%02X | cursor index: %d \n", opcodeID & 0xFF, ctx.cursor);
        }

    }
}
