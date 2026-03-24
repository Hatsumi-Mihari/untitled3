package VM;

import GL_Engine.GEngine_Functions;

import java.nio.ByteBuffer;

public class VM_Main {
    private static final VM_OPcode vmOpcode = new VM_OPcode();
    private GEngine_Functions Gapi;
    private VM_Context context;
    private byte[] program;
    private int tick = 16;

    private final int sizeHeaderBytes = 3;

    private int execOpcode = -1;
    private int execLenPayload = -1;
    private byte[] execPayload;
    private boolean debugExecute = true;


    public VM_Main(byte[] program, GEngine_Functions Gapi){
        this.program = program;
        this.Gapi = Gapi;
        this.context = new VM_Context();
        this.vmOpcode.VM_context(Gapi);
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
    }

    public int getSizeByteCode(){
        return this.program.length;
    }
}
