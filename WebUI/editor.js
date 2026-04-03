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
                        [/\b(LOAD|LOAD_CZ|ADD|INC|JMP|CMP|RS_FBO|DRW_PU|DRW_PR|CL)\b/, 'keyword'],
                        [/\bR[0-9]+\b/, 'variable'],
                        [/[0-9]+/, 'number'],
                        [/;.*$/, 'comment'],
                        [/[A-Z_]+:/, 'type.identifier'],
                    ]
                }
            });


            self.editor = monaco.editor.create(document.getElementById('container'), {
                value: [
                    'LOAD R1 5',
                    '@lable MainLoop',
                    'JMP MainLoop',
                    'CMP R0 <= R1 MainLoop'
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