function encodeInt32BE(velue) {
    return [
        (velue >> 24) & 0xFF,
        (velue >> 16) & 0xFF,
        (velue >> 8) & 0xFF,
        (velue & 0xFF)
    ];
}

function encodeInt16BE(velue) {
    return [
        (velue >> 8) & 0xFF,
        (velue & 0xFF)
    ];
}

function encodeColorRGBA8888(velue_str_hex_color){
    const color_ch = velue_str_hex_color.match(/.{1,2}/g);
    const r = parseInt(color_ch[0], 16);
    const g = parseInt(color_ch[1], 16);
    const b = parseInt(color_ch[2], 16);
    const a = parseInt(color_ch[3], 16);
    const value = ((r & 0xFF) << 24 | 
               (g & 0xFF) << 16 | 
               (b & 0xFF) << 8  | 
               (a & 0xFF)) >>> 0;
    return value;
}

function buildStartCommand(outArr, id, size, pointer) {
    outArr[pointer] = id;
    outArr[pointer + 1] = size[0];
    outArr[pointer + 2] = size[1];
}

function buildPayload(outArr, offset, payload, pointer) {
    for (let j = 0; j < payload.length; j++) {
        outArr[pointer + offset + j] = payload[j];
    }
}

class Compiler_ASM {
    constructor() {
        this.instcractions = {
            'LOAD': {
                opcode: 0xF9,
                payloadSize: encodeInt16BE(5),
                totalSizeLineOpcodeBytes: 8,
                build: function (ctx, args) {

                    buildStartCommand(ctx.outCode, this.opcode, this.payloadSize, ctx.pointer);
                    ctx.outCode[ctx.pointer + 3] = parseInt(args[0].replace('R', ''));
                    buildPayload(ctx.outCode, 4, encodeInt32BE(parseInt(args[1])), ctx.pointer);

                }
            },
            'LOAD_CZ': {
                opcode: -1,
                payloadSize: encodeInt16BE(5),
                totalSizeLineOpcodeBytes: 8,
                build: function (ctx, args) {
                    args[1] = encodeColorRGBA8888(args[1]);
                    ctx.instcractions['LOAD'].build(ctx, args);
                }
            },
            'JMP': {
                opcode: 0xFB,
                payloadSize: encodeInt16BE(2),
                totalSizeLineOpcodeBytes: 5,
                build: function (ctx, args) {

                    buildStartCommand(ctx.outCode, this.opcode, this.payloadSize, ctx.pointer);
                    if (typeof args[0] === "string") buildPayload(ctx.outCode, 3, encodeInt16BE(ctx.lables[args[0]]), ctx.pointer);
                    else buildPayload(ctx.outCode, 3, encodeInt16BE(parseInt(args[0])), ctx.pointer);

                }
            },
            'CMP': {
                opcode: 0xF8,
                payloadSize: encodeInt16BE(5),
                totalSizeLineOpcodeBytes: 8,
                build: function (ctx, args) {

                    let idCompair = 0;

                    switch (args[1]) {
                        case "==":
                            idCompair = 0;
                            break;
                        case "<":
                            idCompair = 1;
                            break;
                        case ">":
                            idCompair = 2;
                            break;
                        case "<=":
                            idCompair = 3;
                            break;
                        case ">=":
                            idCompair = 4;
                            break;
                        case "!=":
                            idCompair = 5;
                            break;
                    }

                    buildStartCommand(ctx.outCode, this.opcode, this.payloadSize, ctx.pointer);
                    ctx.outCode[ctx.pointer + 3] = parseInt(args[0].replace('R', ''));
                    ctx.outCode[ctx.pointer + 4] = idCompair;
                    ctx.outCode[ctx.pointer + 5] = parseInt(args[2].replace('R', ''));
                    if (typeof args[0] === "string") buildPayload(ctx.outCode, 6, encodeInt16BE(ctx.lables[args[3]]), ctx.pointer);
                    else buildPayload(ctx.outCode, 6, encodeInt16BE(parseInt(args[0])), ctx.pointer);

                }
            },
            'INC': {
                opcode: 0xF7,
                payloadSize: encodeInt16BE(5),
                totalSizeLineOpcodeBytes: 8,
                build: function (ctx, args) {

                    buildStartCommand(ctx.outCode, this.opcode, this.payloadSize, ctx.pointer);
                    ctx.outCode[ctx.pointer + 3] = parseInt(args[0].replace('R', ''));
                    buildPayload(ctx.outCode, 4, encodeInt32BE(parseInt(args[1])), ctx.pointer);

                }
            },
            '@lable': {
                opcode: -1,
                payloadSize: [],
                payloadSizeBytes: 0,
                totalSizeLineOpcodeBytes: 0,
                build: function (ctx, args) {
                }
            },

            'RS_FBO': {
                opcode: 0x00,
                payloadSize: encodeInt16BE(4),
                totalSizeLineOpcodeBytes: 7,
                build: function (ctx, args) {
                    buildStartCommand(ctx.outCode, this.opcode, this.payloadSize, ctx.pointer);
                    buildPayload(ctx.outCode, 3, encodeInt16BE(parseInt(args[0])), ctx.pointer);
                    buildPayload(ctx.outCode, 5, encodeInt16BE(parseInt(args[1])), ctx.pointer);
                }
            },

            'DRW_PU': {
                opcode: 0x04,
                payloadSize: encodeInt16BE(9),
                totalSizeLineOpcodeBytes: 12,
                build: function (ctx, args) {
                    buildStartCommand(ctx.outCode, this.opcode, this.payloadSize, ctx.pointer);
                    buildPayload(ctx.outCode, 3, encodeInt32BE(encodeColorRGBA8888(args[0])), ctx.pointer);
                    ctx.outCode[ctx.pointer + 7] = 0x00;
                    buildPayload(ctx.outCode, 8, encodeInt16BE(parseInt(args[1])), ctx.pointer);
                    buildPayload(ctx.outCode, 10, encodeInt16BE(parseInt(args[2])), ctx.pointer);
                }
            },

            'DRW_PR': {
                opcode: 0x04,
                payloadSize: encodeInt16BE(7),
                totalSizeLineOpcodeBytes: 10,
                build: function (ctx, args) {
                    buildStartCommand(ctx.outCode, this.opcode, this.payloadSize, ctx.pointer);
                    buildPayload(ctx.outCode, 3, encodeInt32BE(encodeColorRGBA8888(args[0])), ctx.pointer);
                    ctx.outCode[ctx.pointer + 7] = 0x01;
                    ctx.outCode[ctx.pointer + 8] = parseInt(args[1].replace('R', ''));
                    ctx.outCode[ctx.pointer + 9] = parseInt(args[2].replace('R', ''));
                }
            }, 

            'CL': {
                opcode: 0x02,
                payloadSize: encodeInt16BE(0),
                totalSizeLineOpcodeBytes: 3,
                build: function (ctx, args) {
                    buildStartCommand(ctx.outCode, this.opcode, this.payloadSize, ctx.pointer);
                }
            },

            'FILL_C': {
                opcode: 0x01,
                payloadSize: encodeInt16BE(4),
                totalSizeLineOpcodeBytes: 7,
                build: function (ctx, args) {
                    buildStartCommand(ctx.outCode, this.opcode, this.payloadSize, ctx.pointer);
                    buildPayload(ctx.outCode, 3, encodeInt32BE(encodeColorRGBA8888(args[0])), ctx.pointer);
                }
            },
            'CGL_RGB': {
                opcode: 0x03,
                payloadSize: encodeInt16BE(18),
                totalSizeLineOpcodeBytes: 21,
                build: function (ctx, args) {
                    buildStartCommand(ctx.outCode, this.opcode, this.payloadSize, ctx.pointer);
                    buildPayload(ctx.outCode, 3, encodeInt32BE(encodeColorRGBA8888(args[0])), ctx.pointer);
                    buildPayload(ctx.outCode, 7, encodeInt32BE(encodeColorRGBA8888(args[1])), ctx.pointer);
                    buildPayload(ctx.outCode, 11, encodeInt16BE(parseInt(args[2])), ctx.pointer);
                    buildPayload(ctx.outCode, 13, encodeInt16BE(parseInt(args[3])), ctx.pointer);
                    buildPayload(ctx.outCode, 15, encodeInt16BE(parseInt(args[4])), ctx.pointer);
                    buildPayload(ctx.outCode, 17, encodeInt16BE(parseInt(args[5])), ctx.pointer);
                    buildPayload(ctx.outCode, 19, encodeInt16BE(parseInt(args[6])), ctx.pointer);
                }
            }, 
            'SQR_DRW': {
                opcode: 0x05,
                payloadSize: encodeInt16BE(12),
                totalSizeLineOpcodeBytes: 15,
                build: function (ctx, args) {
                    buildStartCommand(ctx.outCode, this.opcode, this.payloadSize, ctx.pointer);
                    buildPayload(ctx.outCode, 3, encodeInt32BE(encodeColorRGBA8888(args[0])), ctx.pointer);
                    buildPayload(ctx.outCode, 7, encodeInt16BE(parseInt(args[1])), ctx.pointer);
                    buildPayload(ctx.outCode, 9, encodeInt16BE(parseInt(args[2])), ctx.pointer);
                    buildPayload(ctx.outCode, 11, encodeInt16BE(parseInt(args[3])), ctx.pointer);
                    buildPayload(ctx.outCode, 13, encodeInt16BE(parseInt(args[4])), ctx.pointer);
                }
            }
        };

        this.lables = {};
        this.pointer = 0;
        this.outCode = new Uint8Array();
    }

    compile_asm(code) {
        code = code.replace(/;.*$/gm, "");
        let code_pars = code.split("\n");
        code_pars = code_pars.filter(item => item !== "");
        let bytecode_size = 0;

        for (let i = 0; i < code_pars.length; i++) {
            const tokens = code_pars[i].trim().split(/\s+/);
            const cmdName = tokens[0];

            if (cmdName == '@lable') {
                const args = tokens.slice(1);
                this.lables[args[0]] = bytecode_size;
                console.log(this.lables);
            }

            bytecode_size += this.instcractions[cmdName].totalSizeLineOpcodeBytes;
        }

        this.outCode = new Uint8Array(bytecode_size);
        console.log("bytecode_size = ", bytecode_size);

        for (let i = 0; i < code_pars.length; i++) {
            const tokens = code_pars[i].trim().split(/\s+/);
            const cmdName = tokens[0];

            const args = tokens.slice(1);
            this.instcractions[cmdName].build(this, args);
            console.log(code_pars[i].trim().split(/\s+/));
            this.pointer += this.instcractions[cmdName].totalSizeLineOpcodeBytes;
        }

        console.log(this.outCode);
        this.pointer = 0;

        return this.outCode;
    }
}