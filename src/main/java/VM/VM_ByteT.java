package VM;

public class VM_ByteT {
    public VM_ByteT(){

    }

    public int BE16_encode(byte b1, byte b2){
        return (((b1 & 0xFF) << 8) | (b2 & 0xFF));
    }
}
