package com.calsignlabs.apde.ai.util;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.MonospaceSpan;
import android.text.style.TypefaceSpan;
import androidx.core.content.ContextCompat;
import com.calsignlabs.apde.R;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownRenderer {
    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("```(\\w*)\\n([\\s\\S]*?)```");
    private static final Pattern INLINE_CODE_PATTERN = Pattern.compile("`([^`]+)`");

    public static SpannableStringBuilder render(Context context, String markdown) {
        SpannableStringBuilder builder = new SpannableStringBuilder(markdown);
        
        // Replace code blocks
        Matcher blockMatcher = CODE_BLOCK_PATTERN.matcher(markdown);
        while (blockMatcher.find()) {
            String language = blockMatcher.group(1);
            String code = blockMatcher.group(2);
            
            // Apply code block styling
            int start = blockMatcher.start();
            int end = blockMatcher.end();
            
            builder.setSpan(new TypefaceSpan("monospace"), start, end, 
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setSpan(new BackgroundColorSpan(
                    ContextCompat.getColor(context, R.color.code_background)), 
                    start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            
            // Apply syntax highlighting if it's a code block
            if (language != null && !language.isEmpty()) {
                applySyntaxHighlighting(context, builder, start, end, code);
            }
        }
        
        // Replace inline code
        Matcher inlineMatcher = INLINE_CODE_PATTERN.matcher(markdown);
        while (inlineMatcher.find()) {
            int start = inlineMatcher.start();
            int end = inlineMatcher.end();
            
            builder.setSpan(new MonospaceSpan(), start, end, 
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setSpan(new BackgroundColorSpan(
                    ContextCompat.getColor(context, R.color.inline_code_background)), 
                    start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        
        return builder;
    }
    
    private static void applySyntaxHighlighting(Context context, SpannableStringBuilder builder, 
            int blockStart, int blockEnd, String code) {
        // Keywords
        Pattern keywordPattern = Pattern.compile(
                "\\b(public|private|protected|class|interface|enum|extends|implements|" +
                "return|if|else|while|for|do|break|continue|new|try|catch|throw|throws|" +
                "final|static|void|int|boolean|String|long|float|double)\\b");
        
        Matcher matcher = keywordPattern.matcher(code);
        while (matcher.find()) {
            builder.setSpan(new ForegroundColorSpan(
                    ContextCompat.getColor(context, R.color.code_keyword)),
                    blockStart + matcher.start(),
                    blockStart + matcher.end(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        
        // Strings
        Pattern stringPattern = Pattern.compile("\"[^\"\\\\]*(\\\\.[^\"\\\\]*)*\"");
        matcher = stringPattern.matcher(code);
        while (matcher.find()) {
            builder.setSpan(new ForegroundColorSpan(
                    ContextCompat.getColor(context, R.color.code_string)),
                    blockStart + matcher.start(),
                    blockStart + matcher.end(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        
        // Numbers
        Pattern numberPattern = Pattern.compile("\\b\\d+\\b");
        matcher = numberPattern.matcher(code);
        while (matcher.find()) {
            builder.setSpan(new ForegroundColorSpan(
                    ContextCompat.getColor(context, R.color.code_number)),
                    blockStart + matcher.start(),
                    blockStart + matcher.end(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        
        // Comments
        Pattern commentPattern = Pattern.compile("//[^\\n]*|/\\*[^*]*\\*+(?:[^/*][^*]*\\*+)*/");
        matcher = commentPattern.matcher(code);
        while (matcher.find()) {
            builder.setSpan(new ForegroundColorSpan(
                    ContextCompat.getColor(context, R.color.code_comment)),
                    blockStart + matcher.start(),
                    blockStart + matcher.end(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
}