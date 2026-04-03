class HexEditor {
    constructor(containerId, data, bytesPerLine = 8) {
        this.container = document.getElementById(containerId);
        this.data = data;
        this.bytesPerLine = bytesPerLine;
        this.cells = [];
        this.lastIP = -1;
        this.indexWrap = [];
        this.init();
    }

    init() {
        this.render();
    }

    render() {
        this.container.style.display = 'grid';
        this.container.style.gridTemplateColumns = `60px 60px repeat(${this.bytesPerLine - 2}, 40px)`;
        let counter_wrap = 0;
        let hightlightSizePayload = 0;

        for (let i = 0; i < this.data.length; i++) {
            const cell = document.createElement('div');
            cell.className = 'hex-cell';
            cell.textContent = this.formatHex(this.data[i]);
            cell.dataset.offset = i;

            const idInset = document.createElement('div');
            idInset.className = 'hex-id';
            const idInset_hex = document.createElement('div');
            idInset_hex.classList = ['hex-id hex-id-addr-jmp'];

            if (i == this.indexWrap[counter_wrap]) {
                idInset.style.gridColumn = 2;
                idInset.textContent = this.indexWrap[counter_wrap];
                idInset.dataset.offset = -1;

                idInset_hex.style.gridColumn = 1;
                idInset_hex.textContent = this.formatHex16(this.indexWrap[counter_wrap]);
                idInset_hex.dataset.offset = -1;

                cell.style.gridColumn = 3;
                cell.classList.add('hex-cell-OPCODE');
                counter_wrap++;
                hightlightSizePayload = 2;

                this.container.appendChild(idInset_hex);
                this.container.appendChild(idInset);
                

            }else if (hightlightSizePayload > 0){
                cell.classList.add('hex-cell-OPCODE-SIZE');
                hightlightSizePayload--;
            }

            cell.contentEditable = true;
            cell.oninput = (e) => {
                let value = e.target.textContent.toUpperCase();
                value = value.replace(/[^0-9A-F]/g, '');

                if (value.length > 2) {
                    value = value.substring(0, 2);
                }

                if (e.target.textContent !== value) {
                    e.target.textContent = value;

                    const range = document.createRange();
                    const sel = window.getSelection();
                    range.setStart(e.target.childNodes[0] || e.target, value.length);
                    range.collapse(true);
                    sel.removeAllRanges();
                    sel.addRange(range);
                } 

                this.handleInput(i, value);
            };
            this.container.appendChild(cell);
            this.cells.push(cell);
        }

    }

    update(data) {
        this.cells = [];
        this.container.innerHTML = "";
        this.data = data;
        this.indexWrap = [];
        console.log(this.data);

        let cursor = 0;
        let payload_size = 0;
        this.indexWrap.push(cursor);

        while (cursor < data.length) {
            payload_size = ((data[cursor + 1] & 0xFF) << 8) | (data[cursor + 2] & 0xFF);
            cursor = cursor + 3 + payload_size;
            this.indexWrap.push(cursor);
        }

        this.render();
    }

    formatHex(val) {
        return val.toString(16).padStart(2, '0').toUpperCase();
    }

    formatHex16(val) {
        return val.toString(16).padStart(4, '0').toUpperCase();
    }

    handleInput(offset, value) {
        const parsed = parseInt(value, 16);
        if (!isNaN(parsed) && parsed >= 0 && parsed <= 255) {
            this.data[offset] = parsed;
        }
    }

    setIP(newIP) {
        if (this.lastIP !== -1) {
            this.cells[this.lastIP].classList.remove('active-ip');
        }
        if (this.cells[newIP]) {
            this.cells[newIP].classList.add('active-ip');
            this.cells[newIP].scrollIntoView({ block: 'nearest', behavior: 'smooth' });
            this.lastIP = newIP;
        }
    }

    insert_code(index_insert, opcode_id, payload_size) {
        let tempIndex = 0;
        let flag = false;
        for(let i = 0; i < this.indexWrap.length; i++){
            if (this.indexWrap[i] == index_insert){
                flag = true;
            }
        }

        if (!flag){
            alert("Incorect Index incert");
            return;
        }

        const cmd_size = 3 + payload_size;
        const newData = new Uint8Array(this.data.length + cmd_size);

        const insert_part = new Uint8Array(cmd_size);
        insert_part[0] = opcode_id & 0xFF;
        insert_part[1] = (payload_size >> 8) & 0xFF;
        insert_part[2] = payload_size & 0xFF;

        newData.set(this.data.subarray(0, index_insert), 0);
        newData.set(insert_part, index_insert);
        newData.set(this.data.subarray(index_insert), index_insert + cmd_size);

        this.update(newData);
    }

    delete_code(index) {
        const size_block_del = ((this.data[index + 1] & 0xFF) << 8) | (this.data[index + 2] & 0xFF);
        const newData = new Uint8Array(this.data.length - (size_block_del + 3));
        newData.set(this.data.subarray(0, index), 0);
        newData.set(this.data.subarray(index + (size_block_del + 3)), index);

        this.update(newData);
    }

    getCode() {
        return this.data;
    }

    getIndexesInsert() {
        return this.indexWrap;
    }
}
