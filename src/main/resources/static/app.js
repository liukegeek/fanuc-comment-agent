const { createApp } = Vue;

// -----------------------------------------------------------------------------
// 演示桥接：当 Java 侧尚未注入真实的 CommentUIBridge 时，前端依然可以
// 通过该模拟对象完成所有数据交互，方便我们专注在界面与交互逻辑上。
// -----------------------------------------------------------------------------
function createDemoBridge() {
    // 为每一种类型生成 8 条示例数据，确保展示区域看上去足够丰富。
    const seed = (prefix) =>
        Array.from({ length: 8 }, (_, idx) => ({
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
    const delay = (result, timeout = 320) =>
        new Promise((resolve) => setTimeout(() => resolve(clone(result)), timeout));

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
            const matched = list.filter((item) =>
                String(item.content).toLowerCase().includes(lower)
            );
            return delay(matched);
        },
        async queryByIdRange(type, start, end) {
            const list = store[type] || [];
            const [s, e] = [Number(start), Number(end)];
            const matched = list.filter(
                (item) => Number(item.id) >= s && Number(item.id) <= e
            );
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
            records[btn.value] = {
                items: [],
                original: [],
                source: "none",
                selected: [],
                flashActive: false,
                flashTimer: null,
            };
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
            loading: false,
            statusMessage: "请选择操作以加载或更新数据。",
            statusLevel: "info",
            statusUpdatedAt: null,
            demoMode: false,
            commentApi: null,
        };
    },
    computed: {
        // 当前选中类型对应的数据列表。
        currentItems() {
            return this.records[this.selectedType].items;
        },
        hasItems() {
            return this.currentItems.length > 0;
        },
        // 只有当范围输入合法时才允许查询。
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
            return this.displayedPanels.reduce(
                (sum, panel) => sum + (panel.items?.length || 0),
                0
            );
        },
        // 记录用户勾选的总数量，便于控制批量操作按钮状态。
        totalSelectedCount() {
            return Object.values(this.records).reduce(
                (sum, record) => sum + (record.selected?.length || 0),
                0
            );
        },
        hasSelection() {
            return this.totalSelectedCount > 0;
        },
    },
    created() {
        // 优先使用 Java 注入的真实桥接对象，若不存在则降级到演示数据。
        this.commentApi = window.CommentUIBridge ?? createDemoBridge();
        if (this.commentApi.__isDemo) {
            this.demoMode = true;
            this.setStatus("当前展示为示例数据，可随时替换为实际接口。", "info");
            this.queryAll();
        }
    },
    beforeUnmount() {
        // 组件卸载前清理仍在等待的动画计时器，避免潜在的内存泄露。
        Object.values(this.records).forEach((record) => {
            if (record.flashTimer) {
                clearTimeout(record.flashTimer);
                record.flashTimer = null;
            }
        });
    },
    methods: {
        // ------------------------------------------------------------------
        // 通用工具方法
        // ------------------------------------------------------------------
        cloneItems(items) {
            return JSON.parse(JSON.stringify(items || []));
        },
        hasRecordDiff(record) {
            if (!record) return false;
            if (record.items.length !== record.original.length) {
                return true;
            }
            return record.items.some(
                (item, index) => item.content !== (record.original[index]?.content ?? "")
            );
        },
        determineState(type) {
            const record = this.records[type];
            if (!record || !record.items.length) return "idle";
            if (record.source !== "server" || this.hasRecordDiff(record)) {
                return "modified";
            }
            return "displayed";
        },
        refreshTypeStates() {
            const visibleTypes = new Set(
                this.displayPanels.map((panel) => panel.type).filter(Boolean)
            );
            this.typeButtons.forEach(({ value: type }) => {
                if (visibleTypes.has(type)) {
                    this.typeStates[type] = this.determineState(type);
                } else {
                    this.typeStates[type] = "idle";
                    this.clearSelection(type);
                }
            });
        },
        clearSelection(type) {
            if (type && this.records[type]) {
                this.records[type].selected = [];
            }
        },
        collectSelectedPayloads() {
            const payload = {};
            let total = 0;
            Object.entries(this.records).forEach(([type, record]) => {
                if (!record.selected.length) return;
                const selectedItems = record.selected
                    .map((key) => record.items.find((item) => String(item.id) === key))
                    .filter(Boolean);
                if (selectedItems.length) {
                    payload[type] = this.cloneItems(selectedItems);
                    total += selectedItems.length;
                }
            });
            return { payload, total };
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
        applyServerRecords(type, items) {
            const normalized = this.normalizeItems(items);
            const record = this.records[type];
            record.items = normalized;
            record.original = this.cloneItems(normalized);
            record.source = "server";
            record.selected = [];
            this.selectedType = type;
            this.updateDisplayPanels(type);
            this.triggerFlash(type);
        },
        applyLocalRecords(type, items) {
            const normalized = this.normalizeItems(items);
            const record = this.records[type];
            record.items = normalized;
            record.original = this.cloneItems(normalized);
            record.source = "local";
            record.selected = [];
            this.selectedType = type;
            this.updateDisplayPanels(type);
            this.triggerFlash(type);
        },
        handleContentChange(type) {
            const record = this.records[type];
            if (!record) return;
            if (record.source === "server" && this.hasRecordDiff(record)) {
                record.source = "edited";
            }
            if (!this.hasRecordDiff(record)) {
                record.source = record.source === "local" ? "local" : "server";
            }
            this.triggerFlash(type);
            this.refreshTypeStates();
        },
        // ------------------------------------------------------------------
        // 类型选择与展示区逻辑
        // ------------------------------------------------------------------
        setType(type) {
            this.selectedType = type;
            const record = this.records[type];
            if (record?.items?.length) {
                this.updateDisplayPanels(type);
            }
        },
        typeButtonClass(type) {
            return [
                "type-button",
                this.selectedType === type ? "is-selected" : null,
                this.typeStates[type] === "displayed" ? "state-displayed" : null,
                this.typeStates[type] === "modified" ? "state-modified" : null,
            ].filter(Boolean);
        },
        stateDescription(type) {
            const state = this.typeStates[type];
            if (state === "displayed") return "数据已显示在面板中";
            if (state === "modified") return "存在待上传的本地修改";
            return "尚未加载数据";
        },
        // 供模板判断某一行是否处于选中状态，决定视觉高亮。
        isRowSelected(type, itemId) {
            const record = this.records[type];
            if (!record) return false;
            return record.selected.includes(String(itemId));
        },
        // 允许用户点击行来切换选中状态，方便批量操作。
        toggleRowSelection(type, itemId) {
            if (!type) return;
            this.selectedType = type;
            const record = this.records[type];
            if (!record) return;
            const key = String(itemId);
            const index = record.selected.indexOf(key);
            if (index >= 0) {
                record.selected.splice(index, 1);
            } else {
                record.selected.push(key);
            }
        },
        // 在用户开始编辑时，自动将该行加入选中集合，保持高亮一致。
        ensureRowSelection(type, itemId) {
            if (!type) return;
            this.selectedType = type;
            const record = this.records[type];
            if (!record) return;
            const key = String(itemId);
            if (!record.selected.includes(key)) {
                record.selected.push(key);
            }
        },
        togglePanelSelection(type) {
            if (!type) return;
            const record = this.records[type];
            if (!record) return;
            if (!record.items.length) {
                record.selected = [];
                return;
            }
            const allSelected = record.selected.length === record.items.length;
            record.selected = allSelected
                ? []
                : record.items.map((item) => String(item.id));
        },
        isPanelEditable(type) {
            return type && type === this.selectedType;
        },
        // 行样式统一封装，便于集中控制选中态、编辑态等视觉效果。
        rowClasses(type, itemId) {
            return {
                selected: this.isRowSelected(type, itemId),
                editable: this.isPanelEditable(type),
            };
        },
        updateDisplayPanels(type) {
            if (!type) return;
            const currentPrimary = this.displayPanels[0];
            if (currentPrimary?.type === type) {
                this.displayPanels.splice(0, 1, { type });
                this.refreshTypeStates();
                return;
            }
            const newPrimary = { type };
            const fallbackSecondary = this.displayPanels[0]?.type
                ? { type: this.displayPanels[0].type }
                : { type: null };
            if (this.displayPanels[1]?.type === type) {
                const previousPrimary = this.displayPanels[0]?.type
                    ? { type: this.displayPanels[0].type }
                    : { type: null };
                this.displayPanels.splice(0, 1, newPrimary);
                this.displayPanels.splice(1, 1, previousPrimary);
                this.refreshTypeStates();
                return;
            }
            this.displayPanels.splice(1, 1, fallbackSecondary);
            this.displayPanels.splice(0, 1, newPrimary);
            this.refreshTypeStates();
        },
        panelState(type) {
            if (!type) return "idle";
            return this.typeStates[type] ?? "idle";
        },
        // 判断某个类型是否处于动画状态，用于触发 CSS 闪烁效果。
        isFlashing(type) {
            return Boolean(type && this.records[type]?.flashActive);
        },
        // 为指定类型的数据面板触发一次约 2 秒的闪烁动画，提示用户数据发生了变化。
        triggerFlash(type) {
            const record = this.records[type];
            if (!record) return;
            if (record.flashTimer) {
                clearTimeout(record.flashTimer);
            }
            record.flashActive = true;
            record.flashTimer = setTimeout(() => {
                record.flashActive = false;
                record.flashTimer = null;
            }, 2000);
        },
        clearDisplay() {
            this.displayPanels.forEach((panel) => {
                if (panel.type && this.records[panel.type]) {
                    const record = this.records[panel.type];
                    record.items = [];
                    record.original = [];
                    record.source = "none";
                    record.selected = [];
                    record.flashActive = false;
                    if (record.flashTimer) {
                        clearTimeout(record.flashTimer);
                        record.flashTimer = null;
                    }
                }
            });
            this.displayPanels.splice(0, 2, { type: null }, { type: null });
            this.refreshTypeStates();
            this.setStatus("已清空展示区域内容。", "info");
        },
        // ------------------------------------------------------------------
        // 查询相关操作
        // ------------------------------------------------------------------
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
                const response = await this.commentApi.queryByKeyword(
                    this.selectedType,
                    keyword
                );
                this.applyServerRecords(this.selectedType, response);
                this.setStatus(
                    `已获取包含 “${keyword}” 的结果，共 ${this.currentItems.length} 条。`,
                    "success"
                );
            });
        },
        async queryByRange() {
            if (!this.canQueryRange) {
                this.setStatus("请正确填写 ID 范围。", "error");
                return;
            }
            const { rangeStart, rangeEnd } = this.search;
            await this.runAsync(async () => {
                const response = await this.commentApi.queryByIdRange(
                    this.selectedType,
                    rangeStart,
                    rangeEnd
                );
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
        // ------------------------------------------------------------------
        // 上传与本地文件操作
        // ------------------------------------------------------------------
        async uploadSelected() {
            const { payload, total } = this.collectSelectedPayloads();
            if (!total) {
                this.setStatus("请至少勾选一条需要上传的记录。", "error");
                return;
            }
            await this.runAsync(async () => {
                for (const [type, items] of Object.entries(payload)) {
                    await this.commentApi.updateAll(type, items);
                    const record = this.records[type];
                    const uploadedIds = new Set(items.map((item) => String(item.id)));
                    items.forEach((item) => {
                        const index = record.items.findIndex(
                            (candidate) => String(candidate.id) === String(item.id)
                        );
                        if (index >= 0) {
                            record.original[index] = this.cloneItems([record.items[index]])[0];
                        }
                    });
                    if (record.source === "local") {
                        const uploadedAll =
                            record.items.length > 0 && uploadedIds.size === record.items.length;
                        record.source = uploadedAll ? "server" : "local";
                    } else {
                        record.source = this.hasRecordDiff(record) ? "edited" : "server";
                    }
                }
                this.refreshTypeStates();
                this.setStatus(`已将 ${total} 条记录上传到机器人。`, "success");
            });
        },
        saveToLocal() {
            const { payload, total } = this.collectSelectedPayloads();
            if (!total) {
                this.setStatus("请至少勾选一条需要保存的记录。", "error");
                return;
            }
            const data = {
                exportedAt: new Date().toISOString(),
                types: payload,
            };
            const blob = new Blob([JSON.stringify(data, null, 2)], {
                type: "application/json",
            });
            const url = URL.createObjectURL(blob);
            const a = document.createElement("a");
            a.href = url;
            a.download = "fanuc-comments-selection.json";
            a.click();
            URL.revokeObjectURL(url);
            this.setStatus(`已保存 ${total} 条记录到本地文件。`, "success");
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
            if (!file) return;
            const reader = new FileReader();
            reader.onload = () => {
                try {
                    const parsed = JSON.parse(reader.result);
                    let loaded = 0;
                    if (Array.isArray(parsed)) {
                        this.applyLocalRecords(this.selectedType, parsed);
                        loaded = parsed.length;
                    } else if (parsed?.types && typeof parsed.types === "object") {
                        Object.entries(parsed.types).forEach(([type, list]) => {
                            if (this.records[type]) {
                                this.applyLocalRecords(type, list);
                                loaded += Array.isArray(list) ? list.length : 0;
                            }
                        });
                    } else {
                        const targetType =
                            parsed?.type && this.records[parsed.type]
                                ? parsed.type
                                : this.selectedType;
                        const list = Array.isArray(parsed?.records) ? parsed.records : [];
                        this.applyLocalRecords(targetType, list);
                        loaded = list.length;
                    }
                    this.setStatus(`已从本地文件加载 ${loaded} 条数据。`, "success");
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
        // ------------------------------------------------------------------
        // 统一的状态显示与异步执行封装
        // ------------------------------------------------------------------
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
                this.setStatus(
                    error?.message ?? "操作执行失败，请检查控制台日志。",
                    "error"
                );
            } finally {
                this.loading = false;
            }
        },
    },
}).mount("#app");
