package com.byd.tools.api;


import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * ClassName: CommentController
 * Package: com.byd.tools.api
 * Description:
 * Author: LiuKe
 * Create: 2025/10/28 14:45
 * Version 1.0
 */

@RestController
@RequestMapping("/api")
public class CommentController {
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("ok", true);
    }

    // 示例：查询某个信号的注释
    @GetMapping("/comments")
    public Map<String, Object> getComment(@RequestParam @NotBlank String type,
                                          @RequestParam int index) {
        // TODO: 调用你现有的 CommentService 查询 Fanuc
        return Map.of("type", type, "index", index, "comment", "DoorOpen");
    }

    // 示例：修改注释
    @PostMapping("/comments")
    public ResponseEntity<?> updateComment(@RequestParam @NotBlank String type,
                                           @RequestParam int index,
                                           @RequestParam @NotBlank String comment) {
        // TODO: 调用你现有的 CommentService 执行修改
        return ResponseEntity.ok(Map.of("updated", true));
    }
}
