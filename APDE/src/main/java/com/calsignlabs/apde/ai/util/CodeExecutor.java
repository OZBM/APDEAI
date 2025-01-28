package com.calsignlabs.apde.ai.util;

import android.content.Context;
import com.calsignlabs.apde.APDE;
import com.calsignlabs.apde.EditorActivity;
import java.io.File;

public class CodeExecutor {
    private final Context context;
    private final APDE apde;

    public CodeExecutor(Context context) {
        this.context = context;
        this.apde = (APDE) context.getApplicationContext();
    }

    public interface ExecutionCallback {
        void onSuccess(String result);
        void onError(String error);
    }

    public void executeCommand(String command, ExecutionCallback callback) {
        try {
            // Handle different types of commands
            if (command.startsWith("run")) {
                runSketch(callback);
            } else if (command.startsWith("build")) {
                buildSketch(callback);
            } else if (command.startsWith("format")) {
                formatCode(callback);
            } else if (command.startsWith("save")) {
                saveSketch(callback);
            } else {
                callback.onError("Unknown command: " + command);
            }
        } catch (Exception e) {
            callback.onError("Failed to execute command: " + e.getMessage());
        }
    }

    private void runSketch(ExecutionCallback callback) {
        try {
            if (context instanceof EditorActivity) {
                ((EditorActivity) context).runSketch();
                callback.onSuccess("Sketch is running");
            } else {
                callback.onError("Not in editor context");
            }
        } catch (Exception e) {
            callback.onError("Failed to run sketch: " + e.getMessage());
        }
    }

    private void buildSketch(ExecutionCallback callback) {
        try {
            if (context instanceof EditorActivity) {
                ((EditorActivity) context).buildSketch();
                callback.onSuccess("Sketch built successfully");
            } else {
                callback.onError("Not in editor context");
            }
        } catch (Exception e) {
            callback.onError("Failed to build sketch: " + e.getMessage());
        }
    }

    private void formatCode(ExecutionCallback callback) {
        try {
            if (context instanceof EditorActivity) {
                ((EditorActivity) context).autoFormat();
                callback.onSuccess("Code formatted");
            } else {
                callback.onError("Not in editor context");
            }
        } catch (Exception e) {
            callback.onError("Failed to format code: " + e.getMessage());
        }
    }

    private void saveSketch(ExecutionCallback callback) {
        try {
            if (context instanceof EditorActivity) {
                ((EditorActivity) context).saveSketch();
                callback.onSuccess("Sketch saved");
            } else {
                callback.onError("Not in editor context");
            }
        } catch (Exception e) {
            callback.onError("Failed to save sketch: " + e.getMessage());
        }
    }

    public void editCode(String filePath, String newCode, ExecutionCallback callback) {
        try {
            if (context instanceof EditorActivity) {
                EditorActivity editor = (EditorActivity) context;
                // Get the current tab index for the file
                int tabIndex = editor.getSketchFile().getTabIndex();
                
                // Update the code in the editor
                editor.getCodeEditText().setText(newCode);
                
                callback.onSuccess("Code updated");
            } else {
                callback.onError("Not in editor context");
            }
        } catch (Exception e) {
            callback.onError("Failed to edit code: " + e.getMessage());
        }
    }
}