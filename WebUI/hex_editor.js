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
        this.container.style.gridTemplateColumns = `repeat(${this.bytesPerLine}, 40px)`;
        this.container.style.gap = '2px';
        let counter_wrap = 0;

        for (let i = 0; i < this.data.length; i++) {
            const cell = document.createElement('div');
            cell.className = 'hex-cell';
            cell.textContent = this.formatHex(this.data[i]);
            cell.dataset.offset = i;

            if (i == this.indexWrap[counter_wrap]) {
                cell.style.gridColumn = 1;
                counter_wrap++;
            }

            cell.contentEditable = true;
            cell.oninput = (e) => this.handleInput(i, e.target.textContent);

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
        const size_block_del = ((this.data[index + 1] & 0xFF) << 8 ) | (this.data[index + 2] & 0xFF);
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
