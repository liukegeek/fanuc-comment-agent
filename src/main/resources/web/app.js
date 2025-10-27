const { createApp } = Vue;

function createDemoBridge() {
    const seed = (prefix) => Array.from({ length: 8 }, (_, idx) => ({
        id: idx + 1,
        content: `${prefix} 示例注释 ${idx + 1}`,
    }));

    const demoTypes = [
        "R_Comment",
        "R_Value",
        "PR",
        "SR_Comment",
        "SR_Value",
        "RI",
        "RO",
        "DI",
        "DO",
        "GI",
        "GO",
        "AI",
        "AO",
        "FLAG",
    ];

    const store = Vue.reactive(
        demoTypes.reduce((acc, type) => {
            acc[type] = seed(type);
            return acc;
        }, {})
    );

    const clone = (data) => JSON.parse(JSON.stringify(data));
    const delay = (result, timeout = 320) => new Promise((resolve) => setTimeout(() => resolve(clone(result)), timeout));

    const ensureList = (value) => (Array.isArray(value) ? value : value ? [value] : []);

    return {
        __isDemo: true,
        async queryById(type, id) {
            const list = store[type] || [];
            const match = list.find((item) => Number(item.id) === Number(id));
            return delay(ensureList(match));
        },
        async queryByKeyword(type, keyword) {
            const list = store[type] || [];
            const lower = keyword.toLowerCase();
            const matched = list.filter((item) => String(item.content).toLowerCase().includes(lower));
            return delay(matched);
        },
        async queryByIdRange(type, start, end) {
            const list = store[type] || [];
            const [s, e] = [Number(start), Number(end)];
            const matched = list.filter((item) => Number(item.id) >= s && Number(item.id) <= e);
            return delay(matched);
        },
        async queryAll(type) {
            return delay(store[type] || []);
        },
        async updateComment(type, comment) {
            const list = store[type] || [];
            const index = list.findIndex((item) => Number(item.id) === Number(comment.id));
            if (index >= 0) {
                list[index] = clone(comment);
            } else {
                list.push(clone(comment));
            }
            return delay({ success: true });
        },
        async updateAll(type, comments) {
            store[type] = ensureList(comments).map((item) => clone(item));
            return delay({ success: true }, 400);
        },
    };
}

function formatTime(date) {
    if (!date) return "";
    return new Intl.DateTimeFormat("zh-CN", {
        hour: "2-digit",
        minute: "2-digit",
        second: "2-digit",
    }).format(date);
}

createApp({
    data() {
        const typeButtons = [
            { label: "R_Comment", value: "R_Comment", tooltip: "数值寄存器的注释" },
            { label: "R_Value", value: "R_Value", tooltip: "数值寄存器的值" },
            { label: "PR", value: "PR", tooltip: "位置寄存器的注释" },
            { label: "SR_Comment", value: "SR_Comment", tooltip: "字符串寄存器的注释" },
            { label: "SR_Value", value: "SR_Value", tooltip: "字符串寄存器的值" },
            { label: "RI", value: "RI", tooltip: "机器人输入信号" },
            { label: "RO", value: "RO", tooltip: "机器人输出信号" },
            { label: "DI", value: "DI", tooltip: "数字输入信号" },
            { label: "DO", value: "DO", tooltip: "数字输出信号" },
            { label: "GI", value: "GI", tooltip: "组输入信号" },
            { label: "GO", value: "GO", tooltip: "组输出信号" },
            { label: "AI", value: "AI", tooltip: "模拟输入信号" },
            { label: "AO", value: "AO", tooltip: "模拟输出信号" },
            { label: "FLAG", value: "FLAG", tooltip: "标签" },
        ];

        const records = {};
        const typeStates = {};
        typeButtons.forEach((btn) => {
            records[btn.value] = { items: [], original: [], source: "none" };
            typeStates[btn.value] = "idle";
        });

        return {
            typeButtons,
            selectedType: typeButtons[0].value,
            typeStates,
            records,
            displayPanels: [
                { type: null },
                { type: null },
            ],
            search: {
                singleId: "",
                keyword: "",
                rangeStart: "",
                rangeEnd: "",
            },
            selectedRowIndex: null,
            loading: false,
            statusMessage: "请选择操作以加载或更新数据。",
            statusLevel: "info",
            statusUpdatedAt: null,
            demoMode: false,
            commentApi: null,
        };
    },
    computed: {
        currentItems() {
            return this.records[this.selectedType].items;
        },
        hasItems() {
            return this.currentItems.length > 0;
        },
        hasSelection() {
            return this.selectedRowIndex !== null && this.currentItems[this.selectedRowIndex];
        },
        canQueryRange() {
            const start = Number(this.search.rangeStart);
            const end = Number(this.search.rangeEnd);
            return !Number.isNaN(start) && !Number.isNaN(end) && end >= start;
        },
        statusTimestamp() {
            return formatTime(this.statusUpdatedAt);
        },
        displayedPanels() {
            return this.displayPanels.map((panel) => ({
                type: panel.type,
                items: panel.type ? this.records[panel.type].items : [],
            }));
        },
        totalDisplayedCount() {
            return this.displayedPanels.reduce((sum, panel) => sum + (panel.items?.length || 0), 0);
        },
    },
    created() {
        this.commentApi = window.CommentUIBridge ?? createDemoBridge();
        if (this.commentApi.__isDemo) {
            this.demoMode = true;
            this.setStatus("当前展示为示例数据，可随时替换为实际接口。", "info");
            this.queryAll();
        }
    },
    methods: {
        setType(type) {
            this.selectedType = type;
            this.selectedRowIndex = null;
        },
        typeButtonClass(type) {
            return [
                "type-button",
                this.selectedType === type ? "is-selected" : null,
                this.typeStates[type] === "queried" ? "state-queried" : null,
                this.typeStates[type] === "modified" ? "state-modified" : null,
            ].filter(Boolean);
        },
        stateDescription(type) {
            const state = this.typeStates[type];
            if (state === "queried") return "数据已与服务器同步";
            if (state === "modified") return "存在待上传的本地修改";
            return "尚未加载数据";
        },
        selectRow(type, index) {
            this.selectedType = type;
            this.selectedRowIndex = index;
        },
        cloneItems(items) {
            return JSON.parse(JSON.stringify(items || []));
        },
        applyServerRecords(type, items) {
            const normalized = this.normalizeItems(items);
            this.records[type].items = normalized;
            this.records[type].original = this.cloneItems(normalized);
            this.records[type].source = "server";
            this.typeStates[type] = normalized.length ? "queried" : "idle";
            if (type === this.selectedType) {
                this.selectedRowIndex = null;
            }
            this.updateDisplayPanels(type);
        },
        applyLocalRecords(type, items) {
            const normalized = this.normalizeItems(items);
            this.records[type].items = normalized;
            this.records[type].original = this.cloneItems(normalized);
            this.records[type].source = "local";
            this.typeStates[type] = normalized.length ? "modified" : "idle";
            if (type === this.selectedType) {
                this.selectedRowIndex = null;
            }
            this.updateDisplayPanels(type);
        },
        normalizeItems(items) {
            if (!items) return [];
            return items
                .filter((item) => item !== null && item !== undefined)
                .map((item) => ({
                    id: item.id ?? "",
                    content: item.content ?? "",
                }));
        },
        handleContentChange(type) {
            const record = this.records[type];
            if (!record) return;
            if (record.source === "local") {
                this.typeStates[type] = record.items.length ? "modified" : "idle";
                return;
            }
            const { items, original } = record;
            const hasDiff =
                items.length !== original.length ||
                items.some((item, index) => item.content !== (original[index] ? original[index].content : undefined));
            this.typeStates[type] = hasDiff ? "modified" : record.items.length ? "queried" : "idle";
        },
        panelState(type) {
            if (!type) return "idle";
            return this.typeStates[type] ?? "idle";
        },
        isPanelEditable(type) {
            return type && type === this.selectedType;
        },
        updateDisplayPanels(type) {
            if (!type) return;
            const currentPrimary = this.displayPanels[0];
            if (currentPrimary?.type === type) {
                this.displayPanels.splice(0, 1, { type });
                return;
            }
            const newPrimary = { type };
            const fallbackSecondary = this.displayPanels[0]?.type ? { type: this.displayPanels[0].type } : { type: null };
            if (this.displayPanels[1]?.type === type) {
                // move secondary to primary and keep the previous primary as secondary
                const previousPrimary = this.displayPanels[0]?.type ? { type: this.displayPanels[0].type } : { type: null };
                this.displayPanels.splice(0, 1, newPrimary);
                this.displayPanels.splice(1, 1, previousPrimary);
                return;
            }
            this.displayPanels.splice(1, 1, fallbackSecondary);
            this.displayPanels.splice(0, 1, newPrimary);
        },
        clearDisplay() {
            this.displayPanels.forEach((panel) => {
                if (panel.type && this.records[panel.type]) {
                    this.records[panel.type].items = [];
                    this.records[panel.type].original = [];
                    this.records[panel.type].source = "none";
                    this.typeStates[panel.type] = "idle";
                }
            });
            this.displayPanels.splice(0, 2, { type: null }, { type: null });
            this.selectedRowIndex = null;
            this.setStatus("已清空展示区域内容。", "info");
        },
        async queryById() {
            const id = this.search.singleId.trim();
            if (!id) {
                this.setStatus("请输入要查询的 ID。", "error");
                return;
            }
            await this.runAsync(async () => {
                const response = await this.commentApi.queryById(this.selectedType, id);
                this.applyServerRecords(this.selectedType, response);
                this.setStatus(`已获取 ID ${id} 的注释信息。`, "success");
            });
        },
        async queryByKeyword() {
            const keyword = this.search.keyword.trim();
            if (!keyword) {
                this.setStatus("请输入要查询的关键字。", "error");
                return;
            }
            await this.runAsync(async () => {
                const response = await this.commentApi.queryByKeyword(this.selectedType, keyword);
                this.applyServerRecords(this.selectedType, response);
                this.setStatus(`已获取包含 “${keyword}” 的结果，共 ${this.currentItems.length} 条。`, "success");
            });
        },
        async queryByRange() {
            if (!this.canQueryRange) {
                this.setStatus("请正确填写 ID 范围。", "error");
                return;
            }
            const { rangeStart, rangeEnd } = this.search;
            await this.runAsync(async () => {
                const response = await this.commentApi.queryByIdRange(this.selectedType, rangeStart, rangeEnd);
                this.applyServerRecords(this.selectedType, response);
                this.setStatus(`已获取 ID ${rangeStart} - ${rangeEnd} 区间的结果。`, "success");
            });
        },
        async queryAll() {
            await this.runAsync(async () => {
                const response = await this.commentApi.queryAll(this.selectedType);
                this.applyServerRecords(this.selectedType, response);
                this.setStatus(`已加载 ${this.selectedType} 类型的全部数据。`, "success");
            });
        },
        async updateSingle() {
            if (!this.hasSelection) {
                this.setStatus("请先选择一条记录。", "error");
                return;
            }
            const record = this.records[this.selectedType];
            const payload = record.items[this.selectedRowIndex];
            await this.runAsync(async () => {
                await this.commentApi.updateComment(this.selectedType, payload);
                record.original[this.selectedRowIndex] = this.cloneItems([payload])[0];
                record.source = "server";
                this.handleContentChange(this.selectedType);
                this.setStatus(`已更新 ID ${payload.id} 的注释至服务器。`, "success");
            });
        },
        async updateAll() {
            if (!this.hasItems) {
                this.setStatus("暂无数据可上传。", "error");
                return;
            }
            const record = this.records[this.selectedType];
            await this.runAsync(async () => {
                await this.commentApi.updateAll(this.selectedType, record.items);
                record.original = this.cloneItems(record.items);
                record.source = "server";
                this.typeStates[this.selectedType] = record.items.length ? "queried" : "idle";
                this.setStatus("已将当前列表全部上传至服务器。", "success");
            });
        },
        saveToLocal() {
            if (!this.hasItems) {
                this.setStatus("暂无数据可保存。", "error");
                return;
            }
            const data = {
                type: this.selectedType,
                exportedAt: new Date().toISOString(),
                records: this.currentItems,
            };
            const blob = new Blob([JSON.stringify(data, null, 2)], { type: "application/json" });
            const url = URL.createObjectURL(blob);
            const a = document.createElement("a");
            a.href = url;
            a.download = `${this.selectedType.toLowerCase()}-comments.json`;
            a.click();
            URL.revokeObjectURL(url);
            this.setStatus("已保存至本地 JSON 文件。", "success");
        },
        loadFromLocal() {
            const input = this.$refs.fileInput;
            if (input) {
                input.value = "";
                input.click();
            }
        },
        handleLocalFile(event) {
            const [file] = event.target.files || [];
            if (!file) {
                return;
            }
            const reader = new FileReader();
            reader.onload = () => {
                try {
                    const parsed = JSON.parse(reader.result);
                    const records = Array.isArray(parsed) ? parsed : parsed.records;
                    this.applyLocalRecords(this.selectedType, records);
                    this.setStatus(`已从本地文件加载 ${records?.length ?? 0} 条数据。`, "success");
                } catch (error) {
                    console.error(error);
                    this.setStatus("文件解析失败，请检查 JSON 格式。", "error");
                }
            };
            reader.onerror = () => {
                this.setStatus("文件读取失败，请重试。", "error");
            };
            reader.readAsText(file, "utf-8");
        },
        setStatus(message, level = "info") {
            this.statusMessage = message;
            this.statusLevel = level;
            this.statusUpdatedAt = new Date();
        },
        async runAsync(task) {
            try {
                this.loading = true;
                await task();
            } catch (error) {
                console.error(error);
                this.setStatus(error?.message ?? "操作执行失败，请检查控制台日志。", "error");
            } finally {
                this.loading = false;
            }
        },
    },
}).mount("#app");
