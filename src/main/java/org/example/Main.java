package org.example;

import GL_Engine.GEngine_Functions;
import GL_Engine.GEngine_Main;
import Logger.Logger;
import VM.VM_Main;
import WS_Server.WS_EMU;
import WS_Server.WS_Main;

import java.io.File;
import java.util.Properties;

public class Main {
    public static void main(String[] args) throws Exception {
        byte[] bytecode = {};
        Logger logger = new Logger();
        GEngine_Main glContext = new GEngine_Main(1, logger);
        GEngine_Functions gl = new GEngine_Functions(glContext);


        WS_Main serverDebug = new WS_Main(1205);
        WS_EMU ws_emu = new WS_EMU(serverDebug, gl, logger);
        serverDebug.setHandlerProcess(ws_emu::checkEndPoint);
        serverDebug.start();

        VM_Main vm = new VM_Main(bytecode, gl, logger, serverDebug::sendTypedBytePkg);
        vm.setTick(1000);
        ws_emu.setHandlerSetTickRate(vm::setTick);
        ws_emu.setHandlerGetByteCodeVM(vm::getByteCodeVM_Debug);
        ws_emu.setHandlerLoadByteCode(vm::loadByteCode);
        ws_emu.setHandlerResetVM(vm::resetVM);

        try {
            String currentDir = System.getProperty("user.dir");
            File html = new File(currentDir + File.separator + "WebUI" + File.separator + "index.html");
            //Desktop.getDesktop().browse(html.toURI());

        } catch (Exception e) {
            e.printStackTrace();
        }

        Properties props = new Properties();
        props.load(Main.class.getResourceAsStream("/version.properties"));

        long lastDebug = 0;

        final long debugInterval = 200;

        while(true){
            long now = System.currentTimeMillis();
            try {
                vm.update();

                long frameTime = System.currentTimeMillis() - now;
                Thread.sleep(Math.max(0, vm.getValueTick() - frameTime));


                if (now - lastDebug >= debugInterval) {
                    ws_emu.sendDebugData(
                            props.getProperty("build.version") + " / build - " + props.getProperty("build.number"),
                            glContext.getSizeFBO(),
                            10, vm.getOpcodeNowExec(),
                            vm.getCursorOpcodePoss(),
                            vm.getValueTick(),
                            vm.getDebugRegsVM(),
                            vm.getDebugRegsU_VM(),
                            vm.getDebugRegsFlags(),
                            vm.getDebugRegsColor()
                    );
                    lastDebug = now;
                }

                serverDebug.sendTypedBytePkg((byte)0xFE, glContext.render_ByteArr());

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //vm.returnCursorZero();
        }

    }
}