package com.calsignlabs.apde.ai;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.calsignlabs.apde.R;
import com.calsignlabs.apde.ai.model.AIProvider;
import com.calsignlabs.apde.ai.model.ChatMessage;
import com.calsignlabs.apde.ai.service.AIService;
import com.calsignlabs.apde.ai.service.AnthropicAIService;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.ArrayList;
import java.util.List;

public class AIChatPanelActivity extends AppCompatActivity {
    private RecyclerView chatRecyclerView;
    private TextInputLayout messageInputLayout;
    private TextInputEditText messageInput;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messages;
    private AIService aiService;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ai_chat_panel);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        // Initialize views
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageInputLayout = findViewById(R.id.messageInputLayout);
        messageInput = findViewById(R.id.messageInput);

        // Setup RecyclerView
        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter(messages);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        // Initialize AI service and command help
        setupAIService();
        setupCommandHelp();

        // Setup message sending
        messageInputLayout.setEndIconOnClickListener(v -> sendMessage());
        
        messageInput.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    private void setupCommandHelp() {
        View commandHelpCard = findViewById(R.id.commandHelpCard);
        commandHelpCard.setVisibility(View.GONE);

        messageInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && messageInput.getText().toString().isEmpty()) {
                commandHelpCard.setVisibility(View.VISIBLE);
            } else {
                commandHelpCard.setVisibility(View.GONE);
            }
        });

        messageInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().startsWith("/")) {
                    commandHelpCard.setVisibility(View.VISIBLE);
                } else {
                    commandHelpCard.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void setupAIService() {
        String provider = prefs.getString("ai_provider", AIProvider.PROVIDER_ANTHROPIC);
        String apiKey = prefs.getString("ai_api_key", "");
        String model = prefs.getString("ai_model", "claude-2");

        switch (provider) {
            case AIProvider.PROVIDER_ANTHROPIC:
                aiService = new AnthropicAIService(apiKey, model);
                break;
            // Add other providers here
            default:
                aiService = new AnthropicAIService(apiKey, model);
        }
    }

    private void sendMessage() {
        String message = messageInput.getText().toString().trim();
        if (message.isEmpty()) return;

        // Add user message to chat
        messages.add(new ChatMessage(message, ChatMessage.TYPE_USER));
        chatAdapter.notifyItemInserted(messages.size() - 1);
        chatRecyclerView.scrollToPosition(messages.size() - 1);

        // Clear input
        messageInput.setText("");

        // Check if it's a command
        if (message.startsWith("/")) {
            String command = message.substring(1).trim();
            CodeExecutor executor = new CodeExecutor(this);
            
            executor.executeCommand(command, new CodeExecutor.ExecutionCallback() {
                @Override
                public void onSuccess(String result) {
                    runOnUiThread(() -> {
                        messages.add(new ChatMessage(result, ChatMessage.TYPE_AI));
                        chatAdapter.notifyItemInserted(messages.size() - 1);
                        chatRecyclerView.scrollToPosition(messages.size() - 1);
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        new MaterialAlertDialogBuilder(AIChatPanelActivity.this)
                                .setTitle("Command Error")
                                .setMessage(error)
                                .setPositiveButton("OK", null)
                                .show();
                    });
                }
            });
            return;
        }

        // Send to AI service
        aiService.sendMessage(message, new AIService.MessageCallback() {
            @Override
            public void onResponse(ChatMessage response) {
                runOnUiThread(() -> {
                    messages.add(response);
                    chatAdapter.notifyItemInserted(messages.size() - 1);
                    chatRecyclerView.scrollToPosition(messages.size() - 1);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    new MaterialAlertDialogBuilder(AIChatPanelActivity.this)
                            .setTitle("Error")
                            .setMessage(error)
                            .setPositiveButton("OK", null)
                            .show();
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ai_chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            showSettingsDialog();
            return true;
        } else if (item.getItemId() == R.id.action_clear) {
            messages.clear();
            chatAdapter.notifyDataSetChanged();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSettingsDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_ai_settings, null);
        TextInputEditText apiKeyInput = dialogView.findViewById(R.id.apiKeyInput);
        
        new MaterialAlertDialogBuilder(this)
                .setTitle("AI Settings")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String apiKey = apiKeyInput.getText().toString();
                    if (!apiKey.isEmpty()) {
                        prefs.edit()
                                .putString("ai_api_key", apiKey)
                                .apply();
                        setupAIService();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}