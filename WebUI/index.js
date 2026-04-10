
function toHex(num) {
    let id = "0x" + num.toString(16).padStart(2, "0").toUpperCase();
    switch (id) {
        case "0x00":
            return id + " | ReSizeFBO";
        case "0x01":
            return id + " | FillColor";
        case "0x02":
            return id + " | Clear";
        case "0x03":
            return id + " | CrateGradientRGBA";
        case "0x04":
            return id + " | DRAW";
        case "0xF7":
            return id + " | INC";
        case "0xF8":
            return id + " | CMP";
        case "0xF9":
            return id + " | LOAD";
        case "0xFB":
            return id + " | JMP";
    }
}

function buildPKG(type, size, data) {
    let pkg = new Uint8Array(size + 3);
    pkg[0] = type & 0xFF;
    pkg[1] = (size >> 8) & 0xFF;
    pkg[2] = size & 0xFF;
    pkg.set(data, 3);
    return pkg;
}


document.addEventListener("DOMContentLoaded", () => {
    let socket = new WebSocket("ws://127.0.0.1:1205");

    let scaleValue = 16;
    const canvas = document.getElementById("screen");
    const ctx = canvas.getContext("2d");
    let code = new Uint8Array();

    const ver = document.getElementById("ver");
    const scale_row = document.getElementById("fbo_scale");
    const resolution = document.getElementById("fbo_size");
    const opcode = document.getElementById("opcode");
    const cursor = document.getElementById("cursor");
    const tick_update = document.getElementById("tick_update");
    const logger = document.getElementById("LOG");
    const regs = document.getElementById("REGS");
    const regs_u = document.getElementById("REGS_U");
    const flags_vm = document.getElementById("FLAGS");
    const server_status = document.getElementById("server_status");

    const btn_scale_fbo = document.getElementById("setScaleFBO");
    const btn_res_fbo = document.getElementById("setResFBO");
    const setTickRate = document.getElementById("setTickRate");
    const updateByteCode = document.getElementById("updateByteCode");
    const reset_btn = document.getElementById("reset");
    const refreshServer = document.getElementById("refreshServer");

    const editor = new Editor();
    const compiler_asm = new Compiler_ASM();

    btn_scale_fbo.onclick = () => {
        scaleValue = prompt("Input new value scale");
        scale_row.textContent = scaleValue + "x";
        canvas.style.width = (width * scaleValue) + "px";
        canvas.style.height = (height * scaleValue) + "px";
    };

    btn_res_fbo.onclick = () => {
        let size_x = prompt("Input new width: ");
        let size_y = prompt("Input new height: ");
        const d = new Uint8Array([(size_x >> 8) & 0xFF, (size_x) & 0xFF, (size_y >> 8) & 0xFF, (size_y) & 0xFF]);
        socket.send(buildPKG(0x00, 4, d));
    };

    setTickRate.onclick = () => {
        let tickrate = prompt("Input new tickrate in ms: ");
        const d = new Uint8Array([(tickrate >> 8) & 0xFF, (tickrate) & 0xFF]);
        socket.send(buildPKG(0x01, 2, d));
    }

    updateByteCode.onclick = () => {
        code = editor.getCode();
        console.log(editor.getCode());
        const out = compiler_asm.compile_asm(editor.getCode());
        socket.send(buildPKG(0x03, out.length, out));
    }

    reset_btn.onclick = () => {
        socket.send(new Uint8Array([0x04, 0x00, 0x00]));
    }

    let width = 32;
    let height = 32;
    let imageData = ctx.createImageData(width, height);

    socket.binaryType = "arraybuffer";
    socket.onopen = () => {
        console.log("Connected");
        socket.send(new Uint8Array([0x02, 0x00, 0x00]));
    };

    function fnContect(event) {
        const buffer = event.data;
        const bytes = new Uint8Array(buffer);

        if (event.data.length == undefined) {
            //console.log("Server Responce Binary:", bytes.length);
        } else {
            //console.log("Server Responce Text:", event.data.length);
            const json_debug = JSON.parse(event.data);
            scale_row.textContent = scaleValue + "x";
            ver.textContent = json_debug.ver;
            resolution.textContent = json_debug.size[0] + "X" + json_debug.size[1];
            opcode.textContent = toHex(json_debug.opcode & 0xFF);
            cursor.textContent = json_debug.cursor;
            tick_update.textContent = json_debug.tick_update;
            logger.textContent = json_debug.log_cli;
            regs.textContent = json_debug.regs_vm;
            regs_u.textContent = json_debug.regs_u_vm;
            flags_vm.textContent = json_debug.flags_vm;
            server_status.textContent = socket.readyState;
            console.log(json_debug.log_cli);



            if (width != json_debug.size[0] || height != json_debug.size[1]) {
                width = json_debug.size[0];
                height = json_debug.size[1];

                canvas.width = width;
                canvas.height = height;

                canvas.style.width = (width * scaleValue) + "px";
                canvas.style.height = (height * scaleValue) + "px";
                imageData = ctx.createImageData(width, height);
            }
        }

        switch (bytes[0]) {
            case 0xFF:
                code = bytes.subarray(1);
                break;
            case 0xFE:
                const img = bytes.subarray(1);
                for (let i = 0; i < img.length; i++) {
                    imageData.data[i] = img[i];
                }
                ctx.putImageData(imageData, 0, 0);
                break;

        }
    };

    refreshServer.onclick = () => {
        console.log(123);
        socket = new WebSocket("ws://127.0.0.1:1205");
        socket.binaryType = "arraybuffer";
        socket.onopen = () => {
            console.log("Connected");
            socket.send(new Uint8Array([0x02, 0x00, 0x00]));
        };
        socket.onmessage = fnContect;
    };

    socket.onmessage = (event) => {fnContect(event);}
});