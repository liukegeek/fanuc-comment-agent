package com.byd.tools.api;

import com.byd.tools.api.dto.*;
import com.byd.tools.exceptions.ConnectFailedException;
import com.byd.tools.exceptions.InvalidParaException;
import com.byd.tools.exceptions.JsonFileIOException;
import com.byd.tools.pojo.Comment;
import com.byd.tools.pojo.CommentType;
import com.byd.tools.service.CommentRepository;
import com.byd.tools.service.CommentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 对外暴露的注释管理接口，负责前端与后端服务之间的交互。
 */
@RestController
@RequestMapping("/api")
public class CommentController {

    private static final Map<String, CommentType> TYPE_MAPPINGS = Map.ofEntries(
            Map.entry("R_COMMENT", CommentType.NUM_REGISTER_COMMENT),
            Map.entry("R_VALUE", CommentType.NUM_REGISTER_VALUE),
            Map.entry("PR", CommentType.POSITION_REGISTER),
            Map.entry("SR_COMMENT", CommentType.STRING_REGISTER_COMMENT),
            Map.entry("SR_VALUE", CommentType.STRING_REGISTER_VALUE),
            Map.entry("RI", CommentType.RI),
            Map.entry("RO", CommentType.RO),
            Map.entry("DI", CommentType.DI),
            Map.entry("DO", CommentType.DO),
            Map.entry("GI", CommentType.GI),
            Map.entry("GO", CommentType.GO),
            Map.entry("AI", CommentType.AI),
            Map.entry("AO", CommentType.AO),
            Map.entry("FLAG", CommentType.FLAG)
    );

    private final CommentRepository commentRepository;
    private final ObjectProvider<CommentService> commentServiceProvider;

    public CommentController(CommentRepository commentRepository,
                             ObjectProvider<CommentService> commentServiceProvider) {
        this.commentRepository = commentRepository;
        this.commentServiceProvider = commentServiceProvider;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("ok", true);
    }

    @GetMapping("/comments/queryById")
    public ResponseEntity<List<Comment>> queryById(@RequestParam("type") @NotBlank String type,
                                                   @RequestParam("id") @NotBlank String id) {
        CommentType commentType = resolveCommentType(type);
        Optional<CommentService> service = commentService();
        if (service.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        try {
            Comment comment = service.get().queryByID(Integer.parseInt(id), commentType);
            if (comment == null) {
                return ResponseEntity.ok(Collections.emptyList());
            }
            return ResponseEntity.ok(List.of(comment));
        } catch (ConnectFailedException | InvalidParaException ex) {
            throw translateException(ex);
        }
    }

    @GetMapping("/comments/queryByKeyWord")
    public ResponseEntity<List<Comment>> queryByKeyword(@RequestParam("type") @NotBlank String type,
                                                        @RequestParam("keyword") @NotBlank String keyword) {
        CommentType commentType = resolveCommentType(type);
        Optional<CommentService> service = commentService();
        if (service.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        try {
            List<Comment> commentList = service.get().queryByKeyword(keyword, commentType);
            return ResponseEntity.ok(commentList);
        } catch (ConnectFailedException | InvalidParaException ex) {
            throw translateException(ex);
        }
    }

    @GetMapping("/comments/queryByIdRange")
    public ResponseEntity<List<Comment>> queryByRange(@RequestParam("type") @NotBlank String type,
                                                      @RequestParam("start") int start,
                                                      @RequestParam("end") int end) {
        CommentType commentType = resolveCommentType(type);
        Optional<CommentService> service = commentService();
        if (service.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        try {
            List<Comment> commentList = service.get().queryByIdRange(start, end, commentType);
            return ResponseEntity.ok(commentList);
        } catch (ConnectFailedException | InvalidParaException ex) {
            throw translateException(ex);
        }
    }

    @GetMapping("/comments/queryAll")
    public ResponseEntity<List<Comment>> queryAll(@RequestParam("type") @NotBlank String type) {
        CommentType commentType = resolveCommentType(type);
        Optional<CommentService> service = commentService();
        if (service.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        try {
            List<Comment> commentList = service.get().queryAllFromServer(commentType);
            return ResponseEntity.ok(commentList);
        } catch (ConnectFailedException | InvalidParaException ex) {
            throw translateException(ex);
        }
    }


    @PostMapping("/comments/update")
    public ResponseEntity<OperationStatusResponse> update(@Valid @RequestBody CommentUpdateRequest request) {
        Optional<CommentService> service = commentService();
        if (service.isEmpty()) {
            return ResponseEntity.ok(OperationStatusResponse.ok("更新接口已就绪，待接入实际业务逻辑。"));
        }
        boolean updated = service.get().updateComment(toComment(request.comment()));
        return ResponseEntity.ok(updated
                ? OperationStatusResponse.ok()
                : OperationStatusResponse.failed("更新失败"));
    }

    @PostMapping("/comments/batchUpdate")
    public ResponseEntity<OperationStatusResponse> batchUpdate(
            @Valid @RequestBody CommentBatchRequest request) {
        Optional<CommentService> service = commentService();
        if (service.isEmpty()) {
            return ResponseEntity.ok(OperationStatusResponse.ok("批量更新接口已就绪，待接入实际业务逻辑。"));
        }
        try {
            boolean updated = service.get().uploadAllToServer(request.commentList().stream().map(this::toComment).toList());
            return ResponseEntity.ok(updated
                    ? OperationStatusResponse.ok()
                    : OperationStatusResponse.failed("更新失败"));
        } catch (ConnectFailedException | InvalidParaException ex) {
            throw translateException(ex);
        }
    }

    /**
     * 从本地文件系统加载注释。
     * 由于浏览器前端沙盒机制，不能直接访问本地文件系统，因此这里只能通过传入 要加载的文件名 + 当前目录 构成完整路径
     * 统一从桌面目录 保存/加载文件
     * 比如 加载桌面 AAA文件夹的bbb.json文件，则接受参数request.path() = "AAA/bbb.json"，返回桌面AAA文件夹的bbb.json文件内容
     *
     * @param request 包含要加载的文件名的请求体
     * @return 加载的注释列表
     */
    @PostMapping("/comments/local/load")
    public ResponseEntity<List<Comment>> loadFromLocal(@Valid @RequestBody LocalLoadRequest request) {


        // 尝试构建桌面路径
        // 在 Windows/macOS/大多数 Linux 上，通常是 "Desktop"
        String userHomeDir = System.getProperty("user.home");
        File desktopDir = new File(userHomeDir, "Desktop");

        //检查这个路径是否存在并且确实是一个目录
        if (!desktopDir.exists() || !desktopDir.isDirectory()) {

            // 标准桌面路径不存在，尝试使用用户主目录作为备用路径。
            desktopDir = new File(userHomeDir);

            // 对于非标准系统（如某些Linux发行版桌面目录名可能不同），
            // 这可能需要更复杂的逻辑，或者干脆回退到用户主目录
        }

        String path = desktopDir.getAbsolutePath() + File.separator + request.path();

        try {
            List<Comment> commentList = commentRepository.loadFromLocalFile(path);
            if (commentList == null) {
                commentList = Collections.emptyList();
            }
            return ResponseEntity.ok(commentList);
        } catch (JsonFileIOException ex) {
            throw translateException(ex);
        }
    }

    @PostMapping("/comments/local/save")
    public ResponseEntity<OperationStatusResponse> saveToLocal(@Valid @RequestBody LocalSaveRequest request) {


        List<Comment> commentList = request.commentList().stream().map(this::toComment).toList();
        if (commentList.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "没有可供保存的注释内容");
        }
        try {
            if (CollectionUtils.isEmpty(request.commentList())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "没有可供保存的注释内容");
            }
            // 尝试构建桌面路径
            // 在 Windows/macOS/大多数 Linux 上，通常是 "Desktop"
            String userHomeDir = System.getProperty("user.home");
            File desktopDir = new File(userHomeDir, "Desktop");

            //检查这个路径是否存在并且确实是一个目录
            if (!desktopDir.exists() || !desktopDir.isDirectory()) {

                // 标准桌面路径不存在，尝试使用用户主目录作为备用路径。
                desktopDir = new File(userHomeDir);

                // 对于非标准系统（如某些Linux发行版桌面目录名可能不同），
                // 这可能需要更复杂的逻辑，或者干脆回退到用户主目录
            }

            String path = desktopDir.getAbsolutePath() + File.separator + request.path();

            commentRepository.saveToJson(commentList, path);
            return ResponseEntity.ok(OperationStatusResponse.ok());
        } catch (JsonFileIOException | InvalidParaException ex) {
            throw translateException(ex);
        }
    }

    private Optional<CommentService> commentService() {
        return Optional.ofNullable(commentServiceProvider.getIfAvailable());
    }

    private CommentType resolveCommentType(String type) {
        if (!StringUtils.hasText(type)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "注释类型不能为空");
        }
        String normalized = type.replace('-', '_').toUpperCase(Locale.ROOT);
        CommentType commentType = TYPE_MAPPINGS.get(normalized);
        if (commentType != null) {
            return commentType;
        }
        // 兼容服务端自身的枚举命名
        Set<CommentType> matching = EnumSet.noneOf(CommentType.class);
        for (CommentType candidate : CommentType.values()) {
            if (candidate.name().equalsIgnoreCase(normalized)) {
                matching.add(candidate);
            }
        }
        if (!matching.isEmpty()) {
            return matching.iterator().next();
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "暂不支持的注释类型: " + type);
    }

    private Comment toComment(CommentPayLoad payload) {
        return new Comment(
                Integer.parseInt(payload.id()),
                payload.content(),
                resolveCommentType(payload.type()));
    }

    private ResponseStatusException translateException(Exception ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        if (ex instanceof InvalidParaException) {
            status = HttpStatus.BAD_REQUEST;
        } else if (ex instanceof ConnectFailedException) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
        } else if (ex instanceof JsonFileIOException) {
            status = HttpStatus.BAD_REQUEST;
        }
        String message = StringUtils.hasText(ex.getMessage()) ? ex.getMessage() : status.getReasonPhrase();
        return new ResponseStatusException(status, message, ex);
    }
}
