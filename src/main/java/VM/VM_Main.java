package VM;

import GL_Engine.GEngine_Functions;
import Logger.Logger;

import java.nio.ByteBuffer;

public class VM_Main {
    private Logger logger;
    private static final VM_OPcode vmOpcode = new VM_OPcode();
    private GEngine_Functions Gapi;
    private VM_Context context;
    private byte[] program;
    private int tick = 16;

    private final int sizeHeaderBytes = 3;

    private int execOpcode = -1;
    private int execLenPayload = -1;
    private byte[] execPayload;


    public VM_Main(byte[] program, GEngine_Functions Gapi, Logger logger){
        this.program = program;
        this.Gapi = Gapi;
        this.context = new VM_Context();
        this.vmOpcode.VM_context(Gapi);
        this.vmOpcode.setLogger(logger);
        this.logger = logger;
    }

    public void execute() throws InterruptedException {
        this.execOpcode = this.program[this.context.cursor];
        this.context.opcode_cursor = this.context.cursor;
        this.execLenPayload = (
                ((this.program[this.context.cursor + 1] & 0xFF) << 8) |
                (this.program[this.context.cursor + 2] & 0xFF)
        );

        this.context.cursor += this.sizeHeaderBytes;

        this.execPayload = new byte[this.execLenPayload];
        int exPayloadPoint = 0;
        for (int i = this.context.cursor; i < this.context.cursor + this.execLenPayload; i++){
            this.execPayload[exPayloadPoint] = this.program[i];
            exPayloadPoint++;
        }
        this.context.cursor += this.execLenPayload;

        this.vmOpcode.execOpcode(this.execOpcode & 0xFF, this.execPayload, this.context);
    }

    public int getCursorPos(){
        return this.context.cursor;
    }
    public int getCursorOpcodePoss(){ return  this.context.opcode_cursor;}

    public void resetVM(boolean b) {
        this.context.cursor = 0;
    }

    public int getOpcodeNowExec(){
        return this.execOpcode;
    }

    public void setTick(int new_tick){
        this.tick = new_tick;
    }

    public int getValueTick(){
        return this.tick;
    }

    public byte[] getByteCodeVM_Debug() {
        return this.program;
    }

    public void loadByteCode(byte[] code){
        this.program = code;
        this.context.cursor = 0;
    }

    public int getSizeByteCode(){
        return this.program.length;
    }

    public int[] getDebugRegsVM() {
        return this.context.register;
    }
    public int[] getDebugRegsU_VM() {
        return this.context.register_u;
    }
    public boolean[] getDebugRegsFlags(){
        return this.context.register_flags;
    }
}
