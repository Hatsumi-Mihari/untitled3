package WS_Server;

import GL_Engine.GEngine_Functions;
import VM.VM_ByteT;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class WS_EMU {
    private WS_Main socket;
    private GEngine_Functions gl;
    private VM_ByteT byteEncode = new VM_ByteT();
    private Consumer<Integer> setHandlerSetTickRate;
    private Consumer<byte[]> handlerLoadByteCodeVM;
    private Consumer<Boolean> handlerResetVM;
    private Supplier<byte[]> handlerGetByteCodeVM;
    private static final List<Consumer<byte[]>> END_POINTS = new ArrayList<>(Collections.nCopies(32, null));

    private void setResolutionFBO(byte[] payload){
        if (payload.length != 4){
            System.out.println("Size payload for update size FBO != 4 bytes: PayLoad Bin");
            for (int i = 0; i < payload.length; i++){
                System.out.printf("%02X ", payload[i]);
            }
            return;
        }

        int[] newSizeFBO = new int[]{
                this.byteEncode.BE16_encode(payload[0], payload[1]),
                this.byteEncode.BE16_encode(payload[2], payload[3])
        };

        if (newSizeFBO[0] <= 0 || newSizeFBO[1] <= 0){
            System.out.println("Width or Height new size FBO is lower 0");
            return;
        }

        this.gl.resize_FBO(newSizeFBO[0], newSizeFBO[1]);
        System.out.println("Set size FBO done successful " + newSizeFBO[0] + "X" + newSizeFBO[1]);

    }

    private void setTickRate(byte[] payload){
        int newTickRate = this.byteEncode.BE16_encode(payload[0], payload[1]);
        this.setHandlerSetTickRate.accept(newTickRate);
    }
    public void setHandlerSetTickRate(Consumer<Integer> handler){
        this.setHandlerSetTickRate = handler;
    }

    private void getByteCodeVM(byte[] payload){
        this.socket.sendTypedBytePkg((byte) 0xFF, this.handlerGetByteCodeVM.get());
    }
    public void setHandlerGetByteCodeVM(Supplier<byte[]> handler){
        this.handlerGetByteCodeVM = handler;
    }

    private void resetVM(byte[] payload){
        this.handlerResetVM.accept(true);
    }
    public void setHandlerResetVM(Consumer<Boolean> handler){
        this.handlerResetVM = handler;
    }

    private void LoadByteCode(byte[] payload){
        this.handlerLoadByteCodeVM.accept(payload);
        System.out.println("Load new code VM");
        this.getByteCodeVM(new byte[0]);
    }
    public void setHandlerLoadByteCode(Consumer<byte[]> handler){
        this.handlerLoadByteCodeVM = handler;
    }

    public WS_EMU(WS_Main socket, GEngine_Functions gl){
        this.socket = socket;
        this.gl = gl;
        END_POINTS.set(0x00, this::setResolutionFBO);
        END_POINTS.set(0x01, this::setTickRate);
        END_POINTS.set(0x02, this::getByteCodeVM);
        END_POINTS.set(0x03, this::LoadByteCode);
        END_POINTS.set(0x04, this::resetVM);
    }

    public void sendDebugData(String ver, int[] screen_size, int scale, int opcode_exec, int cursor, int tick){
        if (!socket.isConnected()) return;
        this.socket.sendString("{" +
                "\"ver\":\"" + ver + "\"," +
                "\"size\":[" + screen_size[0] + ", " + screen_size[1] + "]," +
                "\"tick_update\":\"" + tick + " ms\"," +
                "\"opcode\":" + opcode_exec + "," +
                "\"cursor\":" + cursor +
                "}");
    }

    public void sendByteCode(byte[] code){
        this.socket.sendByteArr(code);
    }

    public void checkEndPoint(byte[] code){
        int cursor = 0;

        int command = code[cursor];
        cursor += 1;

        int size_payload = this.byteEncode.BE16_encode(code[cursor], code[cursor + 1]);
        cursor += 2;
        byte[] payload = new byte[size_payload];

        int counter = 0;
        for (int i = cursor; i < code.length; i++){
            payload[counter] = code[i];
            counter++;
        }

        this.END_POINTS.get(command).accept(payload);
    }
}
