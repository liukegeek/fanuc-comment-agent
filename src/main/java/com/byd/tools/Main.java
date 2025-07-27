package com.byd.tools;

import com.byd.tools.view.FanucEditor;
import javafx.application.Application;

/**
 * ClassName: Main
 * Package: com.byd.tools
 * Description:
 * Author: LiuKe
 * Create: 2025/4/20 23:22
 * Version 1.0
 */
public class Main {
    public static void main(String[] args) {
        FanucEditor fanucEditor = new FanucEditor();
        Application.launch(fanucEditor.getClass());
    }
}
