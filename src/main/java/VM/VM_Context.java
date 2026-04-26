package VM;

public class VM_Context {
    public int cursor;
    public int opcode_cursor;
    public int[] register;
    public int[] register_u;
    public int[] reg_ch_color;
    public boolean[] register_flags;

    public VM_Context(){
        this.cursor = 0;
        this.register = new int[8];
        this.register_u = new int[8];
        this.reg_ch_color = new int[4];
        this.register_flags = new boolean[8];
    }

}
