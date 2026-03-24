package WS_Server;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.WebSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

public class WS_Main extends WebSocketServer{
    private WebSocket client;
    private byte[] byteData;
    private Consumer<byte[]> handlerProcessPKG;
    public WS_Main(int port){
        super(new InetSocketAddress("127.0.0.1", port));
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        System.out.println("New Connection");
        this.client = webSocket;
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        System.out.println("Closed");
        this.client = webSocket;
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Text message: " + message);
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {

        this.byteData = new byte[message.remaining()];
        message.get(byteData);
        this.handlerProcessPKG.accept(this.byteData);

        System.out.println("Binary message: " + byteData.length + " bytes");
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        System.out.println(e.getMessage());
    }

    @Override
    public void onStart() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Server shut down...");
            this.sendString("Server shut down");
            try {
                this.stop(1000); // корректно закрываем WebSocket
                System.out.println("Server shut downed");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
    }

    public byte[] getByteMSG(){
        return this.byteData;
    }

    public void sendString(String s) {
        this.broadcast(s);
    }

    public void sendByteArr(byte[] data){
        this.broadcast(data);
    }

    public void sendTypedBytePkg(byte typePKG, byte[] data){
        byte[] pkg = new byte[data.length + 1];
        pkg[0x00] = typePKG;
        System.arraycopy(data, 0, pkg, 1, data.length);
        this.broadcast(pkg);
    }

    public boolean isConnected(){
        if (this.client == null) return false;
        return this.client.isOpen();
    }

    public void setHandlerProcess(Consumer<byte[]> handler){
        this.handlerProcessPKG = handler;
    }

}
