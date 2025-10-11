>描述了项目中的异常树
```java

// 顶层
public class FcaException extends Exception {  } // 或 RuntimeException

// 连接/网关层（IO 故障）
public class ConnectFailedException extends FcaException {  }
public class RequestFailedException extends FcaException {  }
public class TimeoutException extends FcaException {  }

// 业务校验
public class InvalidParaException extends FcaException {  }  // 非法参数、格式问题（更偏编程错误时也可 Runtime）

// 资源状态
public class NotFoundException extends FcaException {  }    // 业务：不存在 → 也可不用异常，返回 Optional/Result
public class ConflictException extends FcaException {  }     // 版本冲突

// 解析/数据问题
public class ParseException extends FcaException {  }
```