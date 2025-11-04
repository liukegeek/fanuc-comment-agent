const { createApp } = Vue;

const API_BASE_URL = "/api";

// -----------------------------------------------------------------------------
// HTTP 桥接：通过 REST 接口与后端 SpringBoot 服务通信。
// -----------------------------------------------------------------------------
/**
 * 创建一个面向 Comment REST 接口的 HTTP 桥接对象。
 *
 * @param {string} baseUrl - 后端服务提供的基础路径，例如 "/api"。
 * @returns {object} 对应前端可调用的方法集合。
 */
function createHttpBridge(baseUrl = API_BASE_URL) {
    const normalizedBase = baseUrl.endsWith("/")
        ? baseUrl.slice(0, -1)
        : baseUrl;

    /**
     * 构建请求地址，将路径与查询参数组合为合法的 URL 字符串。
     *
     * @param {string} path - 以斜杠开头的接口路径。
     * @param {Record<string, string | number | null | undefined>} [query] - 可选的查询参数。
     * @returns {string} 拼接后的完整请求地址。
     */
    const buildUrl = (path, query) => {
        const params = new URLSearchParams();
        if (query) {
            Object.entries(query).forEach(([key, value]) => {
                if (value === undefined || value === null || value === "") {
                    return;
                }
                params.append(key, String(value));
            });
        }
        const queryString = params.toString();
        return `${normalizedBase}${path}${queryString ? `?${queryString}` : ""}`;
    };

    /**
     * 向后端发起 JSON 请求并处理返回值。
     *
     * @param {string} method - HTTP 动作，例如 "GET"、"POST"。
     * @param {string} path - 请求路径。
     * @param {object} [options]
     * @param {Record<string, string | number | null | undefined>} [options.query] - 需要附带的查询参数。
     * @param {unknown} [options.body] - 需要序列化的请求体。
     * @returns {Promise<any>} 解析后的 JSON 对象，或在无内容时返回 null。
     */
    const request = async (method, path, { query, body } = {}) => {
        const fetchOptions = {
            method,
            headers: {},
        };
        if (body !== undefined) {
            fetchOptions.headers["Content-Type"] = "application/json";
            fetchOptions.body = JSON.stringify(body);
        }
        const url = buildUrl(path, query);
        let response;
        try {
            response = await fetch(url, fetchOptions);
        } catch (error) {
            throw new Error(`无法连接到后端服务: ${error.message}`);
        }
        const text = await response.text();
        if (!response.ok) {
            const message = text || response.statusText || "请求失败";
            throw new Error(message);
        }
        if (!text) {
            return null;
        }
        try {
            return JSON.parse(text);
        } catch (error) {
            throw new Error("后端返回的内容不是有效的 JSON 格式");
        }
    };

    /**
     * 提取 CommentType 的原始字符串值。
     *
     * @param {unknown} rawType - Comment.type 字段的原始值。
     * @returns {string | null} 解析后的字符串表示。
     */
    const extractTypeValue = (rawType) => {
        if (!rawType) return null;
        if (typeof rawType === "string") return rawType;
        if (typeof rawType === "object" && "name" in rawType) {
            return rawType.name;
        }
        return null;
    };

    /**
     * 将类型值规范化为服务端可识别的格式。
     *
     * @param {string} type - 前端内部使用的类型标识。
     * @returns {string | null} 处理后的类型字符串。
     */
    const normalizeTypeForServer = (type) => {
        if (!type) return null;
        return String(type).replace(/-/g, "_").toUpperCase();
    };

    /**
     * 将任意形式的编号转换为整数，便于提交给服务端。
     *
     * @param {unknown} value - 需要转换的编号。
     * @returns {number | null} 可被后端接受的整数，无法解析时返回 null。
     */
    const normalizeId = (value) => {
        if (value === null || value === undefined || value === "") return null;
        const parsed = Number(value);
        return Number.isFinite(parsed) ? parsed : null;
    };

    /**
     * 构造提交给后端的 Comment 载荷。
     *
     * @param {{ id: unknown; content: string }} item - 待上传的注释对象。
     * @param {string} type - 注释所属的类别。
     * @returns {{ id: number | null; content: string; type: string | null }} 格式化后的注释。
     */
    const formatPayload = (item, type) => ({
        id: normalizeId(item?.id),
        content: item?.content ?? "",
        type: normalizeTypeForServer(type),
    });

    /**
     * 统一解析后端返回的注释集合，兼容多种包裹结构。
     *
     * @param {any} data - 后端返回的原始数据。
     * @returns {Array<any>} 注释数组，无法解析时返回空数组。
     */
    const ensureCommentList = (data) => {
        if (!data) return [];
        if (Array.isArray(data)) return data;
        if (Array.isArray(data.comments)) return data.comments;
        if (Array.isArray(data.commentList)) return data.commentList;
        if (Array.isArray(data.data)) return data.data;
        return [];
    };

    return {
        /**
         * @returns {Promise<any>} 健康检查响应，后端存活时返回 { ok: true }。
         */
        async health() {
            return request("GET", "/health");
        },
        /**
         * 按照编号查询单条注释。
         *
         * @param {string} type - 注释类型，例如 "R_Comment"。
         * @param {string | number} id - 需要查询的编号。
         * @returns {Promise<Array<any>>} 匹配的注释列表（0 或 1 条）。
         */
        async queryById(type, id) {
            const data = await request("GET", "/comments/queryById", {
                query: { type, id },
            });
            return ensureCommentList(data);
        },
        /**
         * 根据关键字模糊查询注释。
         *
         * @param {string} type - 注释类型。
         * @param {string} keyword - 查询关键字。
         * @returns {Promise<Array<any>>} 查询到的注释集合。
         */
        async queryByKeyword(type, keyword) {
            const data = await request("GET", "/comments/queryByKeyWord", {
                query: { type, keyword },
            });
            return ensureCommentList(data);
        },
        /**
         * 按照编号范围批量查询注释。
         *
         * @param {string} type - 注释类型。
         * @param {string | number} start - 范围起始编号。
         * @param {string | number} end - 范围结束编号。
         * @returns {Promise<Array<any>>} 对应范围内的注释集合。
         */
        async queryByIdRange(type, start, end) {
            const data = await request("GET", "/comments/queryByIdRange", {
                query: { type, start, end },
            });
            return ensureCommentList(data);
        },
        /**
         * 查询指定类型的全部注释。
         *
         * @param {string} type - 注释类型。
         * @returns {Promise<Array<any>>} 后端返回的完整注释列表。
         */
        async queryAll(type) {
            const data = await request("GET", "/comments/queryAll", {
                query: { type },
            });
            return ensureCommentList(data);
        },
        /**
         * 更新单条注释内容。
         *
         * @param {string} type - 注释类型。
         * @param {{ id: number | string; content: string }} comment - 被修改的注释。
         * @returns {Promise<any>} 后端返回的操作状态对象。
         */
        async updateComment(type, comment) {
            const payload = { comment: formatPayload(comment, type) };
            return request("POST", "/comments/update", { body: payload });
        },
        /**
         * 批量上传同一类型的注释列表。
         *
         * @param {string} type - 注释类型。
         * @param {Array<{ id: number | string; content: string }>} comments - 待上传的注释集合。
         * @returns {Promise<any>} 后端返回的操作状态对象。
         */
        async updateAll(type, comments) {
            const commentList = Array.isArray(comments)
                ? comments.map((item) => formatPayload(item, type))
                : [];
            return request("POST", "/comments/batchUpdate", {
                body: { commentList },
            });
        },
        /**
         * 从本地 JSON 文件中读取注释数据。
         *
         * @param {string} path - 文件绝对路径或相对路径。
         * @param {string} [type] - 需要筛选的注释类型。
         * @returns {Promise<Array<any>>} 读取到的注释集合，若指定类型则已过滤。
         */
        async loadFromLocal(path, type) {
            const data = await request("POST", "/comments/local/load", {
                body: { path },
            });
            const list = ensureCommentList(data);
            if (!type) {
                return list;
            }
            const normalizedType = normalizeTypeForServer(type);
            return list.filter((item) => {
                const itemType = normalizeTypeForServer(
                    extractTypeValue(item?.type)
                );
                return !normalizedType || itemType === normalizedType;
            });
        },
        /**
         * 将注释集合保存到本地 JSON 文件。
         *
         * @param {string} path - 目标文件路径。
         * @param {Record<string, Array<{ id: number | string; content: string }>>} grouped - 按类型分组的注释集合。
         * @returns {Promise<any>} 后端返回的操作状态对象。
         */
        async saveToLocal(path, grouped) {
            const commentList = [];
            Object.entries(grouped || {}).forEach(([key, list]) => {
                (Array.isArray(list) ? list : []).forEach((item) => {
                    commentList.push(formatPayload(item, key));
                });
            });
            return request("POST", "/comments/local/save", {
                body: { path, commentList },
            });
        },
    };
}

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
        async loadFromLocal(path, type) {
            return delay(clone(store[type] || []));
        },
        async saveToLocal() {
            return delay({ success: true });
        },
        async health() {
            return delay({ ok: true, demo: true }, 120);
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
        this.bootstrapBridge();
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
        // 为指定类型的数据面板触发一次约 1.5 秒的闪烁动画，提示用户数据发生了变化。
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
            }, 1500);
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
            if (!this.ensureBridge()) return;
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
            if (!this.ensureBridge()) return;
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
            if (!this.ensureBridge()) return;
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
            if (!this.ensureBridge()) return;
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
            if (!this.ensureBridge()) return;
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
        async saveToLocal() {
            if (!this.ensureBridge()) return;
            const { payload, total } = this.collectSelectedPayloads();
            if (!total) {
                this.setStatus("请至少勾选一条需要保存的记录。", "error");
                return;
            }
            const defaultPath = `${this.selectedType.toLowerCase()}-comments.json`;
            const path = window.prompt("请输入要保存的文件相对于桌面的路径.\n保存到桌面法那科文件夹中则输入:\n 法那科/MB10R1.json", defaultPath);
            if (!path) {
                this.setStatus("已取消保存操作。", "info");
                return;
            }
            await this.runAsync(async () => {
                await this.commentApi.saveToLocal(path, payload);
                this.setStatus(`已请求保存 ${total} 条记录到 ${path}。`, "success");
            });
        },
        async loadFromLocal() {
            if (!this.ensureBridge()) return;
            const path = window.prompt("请输入要加载的文件相对于桌面的路径.\n加载桌面法那科文件夹中的MB10R1.json文件则输入:\n 法那科/MB10R1.json", "");
            if (!path) {
                this.setStatus("已取消本地加载。", "info");
                return;
            }
            await this.runAsync(async () => {
                const response = await this.commentApi.loadFromLocal(
                    path,
                    this.selectedType
                );
                const items = Array.isArray(response)
                    ? response
                    : response?.comments || [];
                this.applyLocalRecords(this.selectedType, items);
                this.setStatus(
                    `已从 ${path} 加载 ${items.length} 条数据。`,
                    "success"
                );
            });
        },
        // ------------------------------------------------------------------
        // 统一的状态显示与异步执行封装
        // ------------------------------------------------------------------
        setStatus(message, level = "info") {
            this.statusMessage = message;
            this.statusLevel = level;
            this.statusUpdatedAt = new Date();
        },
        async bootstrapBridge() {
            if (this.commentApi) {
                return;
            }
            this.loading = true;
            try {
                if (window.CommentUIBridge) {
                    this.commentApi = window.CommentUIBridge;
                    this.demoMode = !!this.commentApi.__isDemo;
                    if (this.demoMode) {
                        this.setStatus(
                            "当前展示为示例数据，可随时替换为实际接口。",
                            "info"
                        );
                        await this.queryAll();
                    } else {
                        this.setStatus("已接入宿主环境提供的注释接口。", "success");
                    }
                    return;
                }
                const api = createHttpBridge();
                await api.health();
                this.commentApi = api;
                this.demoMode = false;
                this.setStatus("已连接后端服务，可开始操作。", "success");
            } catch (error) {
                console.warn("后端接口不可用，使用演示数据。", error);
                this.commentApi = createDemoBridge();
                this.demoMode = true;
                this.setStatus(
                    "当前展示为示例数据，可随时替换为实际接口。",
                    "info"
                );
                await this.queryAll();
            } finally {
                this.loading = false;
            }
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
        ensureBridge() {
            if (!this.commentApi) {
                this.setStatus("接口尚未初始化，请稍后重试。", "error");
                return false;
            }
            return true;
        },
    },
}).mount("#app");
