package org.example;

import GL_Engine.GEngine_Functions;
import GL_Engine.GEngine_Main;
import Logger.Logger;
import VM.VM_Main;
import WS_Server.WS_EMU;
import WS_Server.WS_Main;
import java.awt.Desktop;
import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        byte[] bytecode = {
                (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0x00, (byte) 0x10, (byte) 0x00,(byte) 0x10,
                (byte) 0xFA, (byte) 0x00, (byte) 0x00,
                (byte) 0x03, (byte) 0x00, (byte) 0x10,
                (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0xFF,
                (byte) 0x80, (byte) 0x00, (byte) 0x80,(byte) 0xFF,
                (byte) 0x00,(byte) 0x01,
                (byte) 0x00,(byte) 0x01,
                (byte) 0x00,(byte) 0x10,
                (byte) 0x00,(byte) 0x10,
                (byte) 0xFB, (byte) 0x00, (byte) 0x00,
        };
        Logger logger = new Logger();
        GEngine_Main glContext = new GEngine_Main(1, logger);
        GEngine_Functions gl = new GEngine_Functions(glContext);


        WS_Main serverDebug = new WS_Main(1204);
        WS_EMU ws_emu = new WS_EMU(serverDebug, gl, logger);
        serverDebug.setHandlerProcess(ws_emu::checkEndPoint);
        serverDebug.start();

        VM_Main vm = new VM_Main(bytecode, gl, logger);
        vm.setTick(1000);
        ws_emu.setHandlerSetTickRate(vm::setTick);
        ws_emu.setHandlerGetByteCodeVM(vm::getByteCodeVM_Debug);
        ws_emu.setHandlerLoadByteCode(vm::loadByteCode);
        ws_emu.setHandlerResetVM(vm::resetVM);

        try {
            String currentDir = System.getProperty("user.dir");
            File html = new File(currentDir + File.separator + "WebUI" + File.separator + "index.html");
            Desktop.getDesktop().browse(html.toURI());

        } catch (Exception e) {
            e.printStackTrace();
        }


        while(true){
            while (vm.getCursorPos() < vm.getSizeByteCode()){
                vm.execute();
                serverDebug.sendTypedBytePkg((byte)0xFE, glContext.render_ByteArr());

                ws_emu.sendDebugData(
                        "1.4.4 Alpha (2603.0226)",
                        glContext.getSizeFBO(),
                        10, vm.getOpcodeNowExec(),
                        vm.getCursorOpcodePoss(),
                        vm.getValueTick()
                );

                Thread.sleep(vm.getValueTick());
            }

            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //vm.returnCursorZero();
        }

    }
}