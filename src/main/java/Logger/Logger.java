package Logger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private LocalDateTime now;
    private String log = "";

    private boolean log_render_calls = false;
    private boolean log_vm_bs_opcodes = false;
    private boolean log_events = true;

    private void handlerLog(String msg){
        System.out.print(msg + "\n");
        this.log = msg + "\n";
    }

    private String getFormatedTime(){
        this.now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
        return this.now.format(formatter);
    }

    public void Log(String msg, String Layer){
        String Log = "[ " + this.getFormatedTime() + " ]" +
                "[ " + Layer +" ] " + msg;
        this.handlerLog(Log);
    }

    public void LOGI(String msg){
        this.Log(msg, "INFO");
    }

    public void LOGDB(String msg){
        this.Log(msg, "DEBUG");
    }

    public void LOGE(String msg){
        this.Log(msg, "ERROR");
    }

    public String getLog(){
        return this.log;
    }

    public boolean state_log_render_calls(){
        return this.log_render_calls;
    }

    public boolean state_log_evets(){
        return this.log_events;
    }

    public boolean state_log_vm_bs_opcode(){
        return this.log_vm_bs_opcodes;
    }
}
