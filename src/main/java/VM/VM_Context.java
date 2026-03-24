package VM;

public class VM_Context {
    public int cursor;
    public int opcode_cursor;
    public int[] register;

    public VM_Context(){
        this.cursor = 0;
        this.register = new int[8];
    }

}
