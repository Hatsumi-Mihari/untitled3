package VM;

public class VM_ByteT {
    public VM_ByteT(){

    }

    public int BE16_encode(byte b1, byte b2){
        return (((b1 & 0xFF) << 8) | (b2 & 0xFF));
    }

    public int BE32_encode(byte[] b){
        return (((b[0] & 0xFF) << 24) |
                ((b[1] & 0xFF) << 16) |
                ((b[2] & 0xFF) << 8) |
                (b[3] & 0xFF));
    }
}
