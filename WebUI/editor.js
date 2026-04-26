class Editor {
    constructor() {
        const self = this;
        self.editor;
        self.init();
    }

    init() {
        require.config({ paths: { 'vs': 'https://cdnjs.cloudflare.com/ajax/libs/monaco-editor/0.34.1/min/vs' } });
        require(['vs/editor/editor.main'], function () {
            monaco.languages.register({ id: 'myAsm' });
            monaco.languages.setMonarchTokensProvider('myAsm', {
                tokenizer: {
                    root: [
                        [/\b(LOAD|LOAD_TIME_NOW|LOAD_TICK|ADD|SUB|MUL|DIV|INC|DEC|JMP|CMP|RS_FBO|DRW_PX|CL|FILL_C|CGL_RGB|SQR_DRW|LR_BRS|C_LOAD_HLS|COLOR_MODIFY|HLS_TO_RGB|RENDER)\b/, 'keyword'],
                        [/\b(R[0-9]+|CH[0-3]+)\b/, 'variable'],
                        [/[0-9]+/, 'number'],
                        [/;.*$/, 'comment'],
                        [/[A-Z_]+:/, 'type.identifier'],
                    ]
                }
            });


            self.editor = monaco.editor.create(document.getElementById('container'), {
                value: [
                    "RS_FBO 16 4 ; Set FBO",
"C_LOAD_HLS R1 255 0 0 255 ;Load in register R1 RGBA color",
"LOAD R2 19 ;pos x target",
"LOAD R3 1500 ;duration ms",
"LOAD_TIME_NOW R4 ;time start",
"",
";(Ease-in) Animation",
"@lable MainLoop",
"CL ; Clear FBO",
"LOAD_TIME_NOW R0 ;time now",
"; 1. progress time (0..255)",
"SUB R5 R0 R4 ; t",
"MUL R5 R5 255 ; t * 255",
"DIV R5 R5 R3 ; R6 = normal time X (0..255)",
"",
"; 2. Squer (Ease-in)",
"MUL R5 R5 R5 ; R6 = X*X (max. 65025 - unsigned 16-bit)",
"DIV R5 R5 256 ; R6 = (X*X) / 256. now R6 in range 0..255",
"",
"; 3. Scale to int 0..15",
"MUL R7 R5 R2 ; 255 * 15 pixel renger",
"DIV R7 R7 256 ; div in 256. res: 0..15",
"MUL R6 R5 255 ; color range 0..255 Hue",
"DIV R6 R6 255",
"",
"RGB_TO_HLS R1 R1 ; convert RGB to HLS (read reg1 & save reg1)",
"COLOR_MODIFY R1 CH0 R6 ; modify color in reg1, channel 0 (hue), read from reg6",
"HLS_TO_RGB R1 R1 ; convert HLS to RGB (read reg1 & save reg1)",
"DRW_PX R1 R7 1",
"; call draw pixe (arg1 -> reg1 color), (arg2 -> reg7 pos X)",
"; (arg3 -> const value pos Y)",
"INC R7 1",
"DRW_PX R1 R7 2",
"INC R7 1",
"DRW_PX R1 R7 3",
"INC R7 1",
"DRW_PX R1 R7 4",
"",
"CMP R7 > R2 Continue",
"; CMP = IF (jamp in lable Continue if the conditions are not true)",
"LOAD R7 1",
"LOAD_TIME_NOW R4 ; load time now in reg4",
"RENDER ; call Render in device",
"JMP MainLoop",
"",
"@lable Continue",
"RENDER ; call Render in device",
"JMP MainLoop"

                ].join('\n'),
                language: 'myAsm',
                theme: 'vs-dark',
                glyphMargin: true,
                automaticLayout: true,
                fontSize: 14
            });
        });
    }

    getCode() {
        if (self.editor) {
            return self.editor.getValue();
        } else {
            console.error("Editor not inited");
            return "";
        }
    }


}

