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

function encodeColorRGBA8888(velue_str_hex_color) {
    const color_ch = velue_str_hex_color.match(/.{1,2}/g);
    const r = parseInt(color_ch[0], 16);
    const g = parseInt(color_ch[1], 16);
    const b = parseInt(color_ch[2], 16);
    const a = parseInt(color_ch[3], 16);
    const value = ((r & 0xFF) << 24 |
        (g & 0xFF) << 16 |
        (b & 0xFF) << 8 |
        (a & 0xFF)) >>> 0;
    return value;
}

function isConst(arg) {
    return !isNaN(parseFloat(arg)) && isFinite(arg);
}

function buildStartCommand(outArr, id, size, maskSizeBytes, pointer) {
    outArr[pointer] = id;
    outArr[pointer + 1] = (maskSizeBytes << 6) | (size[0] & 0x3F);
}

function maskMaker(args, size) {
    let mask = 0;
    const isOp = (arg) => /^(>=|<=|==|!=|>|<)$/.test(arg);
    const isReg = (arg) => /^R\d{0,7}$/i.test(arg);
    const isChannel = (arg) => /^CH\d{0,3}$/i.test(arg);
    const isConstant = (arg) => /^-?\d+$/.test(arg) || /^[0-9A-F]+$/i.test(arg);
    for (let i = 0; i < args.length; i++) {
        if (isOp(args[i])) mask |= (1 << (size - 1 - i));
        if (isChannel(args[i])) mask |= (0 << (size - 1 - i));
        if (isReg(args[i])) mask |= (0 << (size - 1 - i));
        if (isConstant(args[i])) mask |= (1 << (size - 1 - i))
    }

    return mask;
}

function buildMaskArgs(maskSizeBytes, args) {
    let mask;

    switch (maskSizeBytes) {
        case 1:
            mask = new Uint8Array(1);
            mask[0] = maskMaker(args, 8);
            console.log(mask[0].toString(2).padStart(8, '0'));
            return mask[0];
    }
}

function buildPayload(outArr, offset, payload, pointer) {
    for (let j = 0; j < payload.length; j++) {
        outArr[pointer + offset + j] = payload[j];
    }
}

function toUint8(value) {
    const arr = new Uint8Array(1);
    arr[0] = value;
    return arr;
}

class Compiler_ASM {
    constructor() {
        this.instcractions = {
            'LOAD': {
                opcode: 0xF9,
                payloadSize: toUint8(5),
                maskSizeBytes: 0,
                totalSizeLineOpcodeBytes: 7,
                build: function (ctx, args) {

                    buildStartCommand(ctx.outCode, this.opcode, this.payloadSize, this.maskSizeBytes, ctx.pointer);
                    ctx.outCode[ctx.pointer + 2] = parseInt(args[0].replace('R', ''));
                    buildPayload(ctx.outCode, 3, encodeInt32BE(parseInt(args[1])), ctx.pointer);

                }
            },
            'LOAD_CZ': {
                opcode: -1,
                payloadSize: toUint8(5),
                totalSizeLineOpcodeBytes: 7,
                build: function (ctx, args) {
                    args[1] = encodeColorRGBA8888(args[1]);
                    ctx.instcractions['LOAD'].build(ctx, args);
                }
            },
            'JMP': {
                opcode: 0xFB,
                payloadSize: toUint8(2),
                maskSizeBytes: 0,
                totalSizeLineOpcodeBytes: 4,
                build: function (ctx, args) {

                    buildStartCommand(ctx.outCode, this.opcode, this.payloadSize, this.maskSizeBytes, ctx.pointer);
                    if (typeof args[0] === "string") buildPayload(ctx.outCode, 2, encodeInt16BE(ctx.lables[args[0]]), ctx.pointer);
                    else buildPayload(ctx.outCode, 2, encodeInt16BE(parseInt(args[0])), ctx.pointer);

                }
            },
            'CMP': {
                opcode: 0xF8,
                payloadSize: toUint8(12),
                maskSizeBytes: 1,
                totalSizeLineOpcodeBytes: 14,
                build: function (ctx, args) {
                    buildMaskArgs(this.maskSizeBytes, args);
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

                    buildStartCommand(ctx.outCode, this.opcode, this.payloadSize, this.maskSizeBytes, ctx.pointer);
                    ctx.outCode[ctx.pointer + 2] = buildMaskArgs(this.maskSizeBytes, args);
                    buildPayload(ctx.outCode, 3, encodeInt32BE(parseInt(args[0].replace('R', ''))), ctx.pointer)
                    ctx.outCode[ctx.pointer + 7] = idCompair;
                    buildPayload(ctx.outCode, 8, encodeInt32BE(parseInt(args[2].replace('R', ''))), ctx.pointer)
                    if (typeof args[0] === "string") buildPayload(ctx.outCode, 12, encodeInt16BE(ctx.lables[args[3]]), ctx.pointer);
                    else buildPayload(ctx.outCode, 12, encodeInt16BE(parseInt(args[0])), ctx.pointer);

                }
            },
            'INC': {
                opcode: 0xF7,
                payloadSize: toUint8(5),
                maskSizeBytes: 0,
                totalSizeLineOpcodeBytes: 7,
                build: function (ctx, args) {

                    buildStartCommand(ctx.outCode, this.opcode, this.payloadSize, this.maskSizeBytes, ctx.pointer);
                    ctx.outCode[ctx.pointer + 2] = parseInt(args[0].replace('R', ''));
                    buildPayload(ctx.outCode, 3, encodeInt32BE(parseInt(args[1])), ctx.pointer);

                }
            },
            'DEC': {
                opcode: 0xF6,
                payloadSize: toUint8(5),
                maskSizeBytes: 0,
                totalSizeLineOpcodeBytes: 7,
                build: function (ctx, args) {

                    buildStartCommand(ctx.outCode, this.opcode, this.payloadSize, this.maskSizeBytes, ctx.pointer);
                    ctx.outCode[ctx.pointer + 2] = parseInt(args[0].replace('R', ''));
                    buildPayload(ctx.outCode, 3, encodeInt32BE(parseInt(args[1])), ctx.pointer);

                }
            },
            'ADD': {
                opcode: 0xF3,
                payloadSize: toUint8(10),
                maskSizeBytes: 1,
                totalSizeLineOpcodeBytes: 12,
                build: function (ctx, args) {
                    buildStartCommand(ctx.outCode, this.opcode, this.payloadSize, this.maskSizeBytes, ctx.pointer);
                    ctx.outCode[ctx.pointer + 2] = buildMaskArgs(this.maskSizeBytes, args);
                    ctx.outCode[ctx.pointer + 3] = parseInt(args[0].replace('R', ''));
                    buildPayload(ctx.outCode, 4, encodeInt32BE(parseInt(args[1].replace('R', ''))), ctx.pointer);
                    buildPayload(ctx.outCode, 8, encodeInt32BE(parseInt(args[2].replace('R', ''))), ctx.pointer);
                }
            },
            'SUB': {
                opcode: 0xF2,
                payloadSize: toUint8(10),
                maskSizeBytes: 1,
                totalSizeLineOpcodeBytes: 12,
                build: function (ctx, args) {
                    buildStartCommand(ctx.outCode, this.opcode, this.payloadSize, this.maskSizeBytes, ctx.pointer);
                    ctx.outCode[ctx.pointer + 2] = buildMaskArgs(this.maskSizeBytes, args);
                    ctx.outCode[ctx.pointer + 3] = parseInt(args[0].replace('R', ''));
                    buildPayload(ctx.outCode, 4, encodeInt32BE(parseInt(args[1].replace('R', ''))), ctx.pointer);
                    buildPayload(ctx.outCode, 8, encodeInt32BE(parseInt(args[2].replace('R', ''))), ctx.pointer);
                }
            },
            'MUL': {
                opcode: 0xF1,
                payloadSize: toUint8(10),
                maskSizeBytes: 1,
                totalSizeLineOpcodeBytes: 12,
                build: function (ctx, args) {
                    buildStartCommand(ctx.outCode, this.opcode, this.payloadSize, this.maskSizeBytes, ctx.pointer);
                    ctx.outCode[ctx.pointer + 2] = buildMaskArgs(this.maskSizeBytes, args);
                    ctx.outCode[ctx.pointer + 3] = parseInt(args[0].replace('R', ''));
                    buildPayload(ctx.outCode, 4, encodeInt32BE(parseInt(args[1].replace('R', ''))), ctx.pointer);
                    buildPayload(ctx.outCode, 8, encodeInt32BE(parseInt(args[2].replace('R', ''))), ctx.pointer);
                }
            },
            'DIV': {
                opcode: 0xF0,
                payloadSize: toUint8(10),
                maskSizeBytes: 1,
                totalSizeLineOpcodeBytes: 12,
                build: function (ctx, args) {
                    buildStartCommand(ctx.outCode, this.opcode, this.payloadSize, this.maskSizeBytes, ctx.pointer);
                    ctx.outCode[ctx.pointer + 2] = buildMaskArgs(this.maskSizeBytes, args);
                    ctx.outCode[ctx.pointer + 3] = parseInt(args[0].replace('R', ''));
                    buildPayload(ctx.outCode, 4, encodeInt32BE(parseInt(args[1].replace('R', ''))), ctx.pointer);
                    buildPayload(ctx.outCode, 8, encodeInt32BE(parseInt(args[2].replace('R', ''))), ctx.pointer);
                }
            },
            'LOAD_TIME_NOW':{
                opcode: 0xF5,
                payloadSize: toUint8(1),
                maskSizeBytes: 0,
                totalSizeLineOpcodeBytes: 3,
                build: function (ctx, args) {
                    buildStartCommand(ctx.outCode, this.opcode, this.payloadSize, this.maskSizeBytes, ctx.pointer);
                    ctx.outCode[ctx.pointer + 2] = parseInt(args[0].replace('R', ''));
                }
            },
            'LOAD_TICK':{
                opcode: 0xF4,
                payloadSize: toUint8(1),
                maskSizeBytes: 0,
                totalSizeLineOpcodeBytes: 3,
                build: function (ctx, args) {
                    buildStartCommand(ctx.outCode, this.opcode, this.payloadSize, this.maskSizeBytes, ctx.pointer);
                    ctx.outCode[ctx.pointer + 2] = parseInt(args[0].replace('R', ''));
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
                payloadSize: toUint8(4),
                maskSizeBytes: 0,
                totalSizeLineOpcodeBytes: 6,
                build: function (ctx, args) {
                    buildStartCommand(ctx.outCode, this.opcode, this.payloadSize, this.maskSizeBytes, ctx.pointer);
                    buildPayload(ctx.outCode, 2, encodeInt16BE(parseInt(args[0])), ctx.pointer);
                    buildPayload(ctx.outCode, 4, encodeInt16BE(parseInt(args[1])), ctx.pointer);
                }
            },

            'DRW_PX': {
                opcode: 0x04,
                payloadSize: toUint8(9),
                maskSizeBytes: 1,
                totalSizeLineOpcodeBytes: 11,
                build: function (ctx, args) {
                    buildStartCommand(ctx.outCode, this.opcode, this.payloadSize, this.maskSizeBytes, ctx.pointer);
                    ctx.outCode[ctx.pointer + 2] = buildMaskArgs(this.maskSizeBytes, args);
                    if ((ctx.outCode[ctx.pointer + 2] & 0xFF ) >> 7 == 1){
                        buildPayload(ctx.outCode, 3, encodeInt32BE(encodeColorRGBA8888( args[0])), ctx.pointer);
                    }else{
                        buildPayload(ctx.outCode, 3, encodeInt32BE(parseInt(args[0].replace('R', ''))), ctx.pointer);
                    }
                    buildPayload(ctx.outCode, 7, encodeInt16BE(parseInt(args[1].replace('R', ''))), ctx.pointer);
                    buildPayload(ctx.outCode, 9, encodeInt16BE(parseInt(args[2].replace('R', ''))), ctx.pointer);
                }
            },
            'CL': {
                opcode: 0x02,
                payloadSize: toUint8(0),
                maskSizeBytes: 0,
                totalSizeLineOpcodeBytes: 2,
                build: function (ctx, args) {
                    buildStartCommand(ctx.outCode, this.opcode, this.payloadSize, this.maskSizeBytes, ctx.pointer);
                }
            },

            'FILL_C': {
                opcode: 0x01,
                payloadSize: toUint8(5),
                maskSizeBytes: 1,
                totalSizeLineOpcodeBytes: 7,
                build: function (ctx, args) {
                    buildStartCommand(ctx.outCode, this.opcode, this.payloadSize, this.maskSizeBytes, ctx.pointer);
                    ctx.outCode[ctx.pointer + 2] = buildMaskArgs(this.maskSizeBytes, args);
                    if ((ctx.outCode[ctx.pointer + 2] & 0xFF ) >> 7 == 1){
                        buildPayload(ctx.outCode, 3, encodeInt32BE(encodeColorRGBA8888( args[0])), ctx.pointer);
                    }else{
                        buildPayload(ctx.outCode, 3, encodeInt32BE(parseInt(args[0].replace('R', ''))), ctx.pointer);
                    }
                    
                }
            },
            'CGL_RGB': {
                opcode: 0x03,
                payloadSize: toUint8(19),
                maskSizeBytes: 1,
                totalSizeLineOpcodeBytes: 21,
                build: function (ctx, args) {
                    buildStartCommand(ctx.outCode, this.opcode, this.payloadSize, this.maskSizeBytes, ctx.pointer);
                    ctx.outCode[ctx.pointer + 2] = buildMaskArgs(this.maskSizeBytes, args);
                    if ((ctx.outCode[ctx.pointer + 2] & 0xFF ) >> 7 == 1){
                        buildPayload(ctx.outCode, 3, encodeInt32BE(encodeColorRGBA8888( args[0])), ctx.pointer);
                    }else{
                        buildPayload(ctx.outCode, 3, encodeInt32BE(parseInt(args[0].replace('R', ''))), ctx.pointer);
                    }

                    if ((ctx.outCode[ctx.pointer + 2] & 0xFF ) >> 6 == 1){
                        buildPayload(ctx.outCode, 7, encodeInt32BE(encodeColorRGBA8888( args[1])), ctx.pointer);
                    }else{
                        buildPayload(ctx.outCode, 7, encodeInt32BE(parseInt(args[1].replace('R', ''))), ctx.pointer);
                    }
                    
                    buildPayload(ctx.outCode, 11, encodeInt16BE(parseInt(args[2].replace('R', ''))), ctx.pointer);
                    buildPayload(ctx.outCode, 13, encodeInt16BE(parseInt(args[3].replace('R', ''))), ctx.pointer);
                    buildPayload(ctx.outCode, 15, encodeInt16BE(parseInt(args[4].replace('R', ''))), ctx.pointer);
                    buildPayload(ctx.outCode, 17, encodeInt16BE(parseInt(args[5].replace('R', ''))), ctx.pointer);
                    buildPayload(ctx.outCode, 19, encodeInt16BE(parseInt(args[6].replace('R', ''))), ctx.pointer);
                }
            },
            'SQR_DRW': {
                opcode: 0x05,
                payloadSize: toUint8(13),
                maskSizeBytes: 1,
                totalSizeLineOpcodeBytes: 15,
                build: function (ctx, args) {
                    buildStartCommand(ctx.outCode, this.opcode, this.payloadSize, this.maskSizeBytes, ctx.pointer);
                    ctx.outCode[ctx.pointer + 2] = buildMaskArgs(this.maskSizeBytes, args);
                    if ((ctx.outCode[ctx.pointer + 2] & 0xFF ) >> 7 == 1){
                        buildPayload(ctx.outCode, 3, encodeInt32BE(encodeColorRGBA8888( args[0])), ctx.pointer);
                    }else{
                        buildPayload(ctx.outCode, 3, encodeInt32BE(parseInt(args[0].replace('R', ''))), ctx.pointer);
                    }
                    buildPayload(ctx.outCode, 7, encodeInt16BE(parseInt(args[1].replace('R', ''))), ctx.pointer);
                    buildPayload(ctx.outCode, 9, encodeInt16BE(parseInt(args[2].replace('R', ''))), ctx.pointer);
                    buildPayload(ctx.outCode, 11, encodeInt16BE(parseInt(args[3].replace('R', ''))), ctx.pointer);
                    buildPayload(ctx.outCode, 13, encodeInt16BE(parseInt(args[4].replace('R', ''))), ctx.pointer);
                }
            },
            'LR_BRS': {
                opcode: 0x06,
                payloadSize: toUint8(1),
                maskSizeBytes: 0,
                totalSizeLineOpcodeBytes: 3,
                build: function (ctx, args) {
                    buildStartCommand(ctx.outCode, this.opcode, this.payloadSize, this.maskSizeBytes, ctx.pointer);
                    const arr = new Uint8Array(1);
                    arr[0] = parseInt(args[0]) & 0xFF;
                    buildPayload(ctx.outCode, 2, arr, ctx.pointer);
                }
            },
            'C_LOAD_HLS': {
                opcode: 0x07,
                payloadSize: toUint8(6),
                maskSizeBytes: 1,
                totalSizeLineOpcodeBytes: 8,
                build: function (ctx, args) {
                    buildStartCommand(ctx.outCode, this.opcode, this.payloadSize, this.maskSizeBytes, ctx.pointer);
                    ctx.outCode[ctx.pointer + 2] = buildMaskArgs(this.maskSizeBytes, args);
                    ctx.outCode[ctx.pointer + 3] = parseInt(args[0].replace('R', ''));
                    ctx.outCode[ctx.pointer + 4] = parseInt(args[1].replace('R', ''));
                    ctx.outCode[ctx.pointer + 5] = parseInt(args[2].replace('R', ''));
                    ctx.outCode[ctx.pointer + 6] = parseInt(args[3].replace('R', ''));
                    ctx.outCode[ctx.pointer + 7] = parseInt(args[4].replace('R', ''));
                }
            },
            'COLOR_MODIFY': {
                opcode: 0x08,
                payloadSize: toUint8(4),
                maskSizeBytes: 1,
                totalSizeLineOpcodeBytes: 6,
                build: function (ctx, args) {
                    buildStartCommand(ctx.outCode, this.opcode, this.payloadSize, this.maskSizeBytes, ctx.pointer);
                    ctx.outCode[ctx.pointer + 2] = buildMaskArgs(this.maskSizeBytes, args);
                    ctx.outCode[ctx.pointer + 3] = parseInt(args[0].replace('R', ''));
                    ctx.outCode[ctx.pointer + 4] = parseInt(args[1].replace('CH', ''));
                    ctx.outCode[ctx.pointer + 5] = parseInt(args[2].replace('R', ''));
                }
            },
            'HLS_TO_RGB': {
                opcode: 0x09,
                payloadSize: toUint8(2),
                maskSizeBytes: 0,
                totalSizeLineOpcodeBytes: 4,
                build: function (ctx, args) {
                    buildStartCommand(ctx.outCode, this.opcode, this.payloadSize, this.maskSizeBytes, ctx.pointer);
                    ctx.outCode[ctx.pointer + 2] = parseInt(args[0].replace('R', ''));
                    ctx.outCode[ctx.pointer + 3] = parseInt(args[1].replace('R', ''));
                }
            },
            'RGB_TO_HLS': {
                opcode: 0x0B,
                payloadSize: toUint8(2),
                maskSizeBytes: 0,
                totalSizeLineOpcodeBytes: 4,
                build: function (ctx, args) {
                    buildStartCommand(ctx.outCode, this.opcode, this.payloadSize, this.maskSizeBytes, ctx.pointer);
                    ctx.outCode[ctx.pointer + 2] = parseInt(args[0].replace('R', ''));
                    ctx.outCode[ctx.pointer + 3] = parseInt(args[1].replace('R', ''));
                }
            },
            'RENDER': {
                opcode: 0x0A,
                payloadSize: toUint8(0),
                maskSizeBytes: 0,
                totalSizeLineOpcodeBytes: 2,
                build: function (ctx, args) {
                    buildStartCommand(ctx.outCode, this.opcode, this.payloadSize, this.maskSizeBytes, ctx.pointer);
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