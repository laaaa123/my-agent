package com.yupi.yuaiagent.app.formatter;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * AI 回答格式化服务
 */
@Service
public class AnswerFormattingService {

    private static final Pattern MARKDOWN_HEADING_PATTERN = Pattern.compile("^#{1,6}\\s*");

    private static final Pattern BULLET_PATTERN = Pattern.compile("^[-*+]\\s+");

    private static final Pattern ORDERED_LIST_PATTERN = Pattern.compile("^(\\d+)\\.\\s*");

    private static final Set<String> SECTION_TITLES = Set.of(
            "结论",
            "建议",
            "课程推荐",
            "原因分析",
            "下一步",
            "补充说明"
    );

    /**
     * 统一清洗回答格式，避免知识库文档原格式泄漏到最终答案中
     *
     * @param rawText 原始回答
     * @return 格式化后的回答
     */
    public String format(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return "";
        }
        String normalized = rawText
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replace("```", "")
                .replace("**", "")
                .replace("__", "")
                .replace("`", "");

        String[] lines = normalized.split("\n");
        List<String> resultLines = new ArrayList<>();
        for (String rawLine : lines) {
            String line = normalizeLine(rawLine);
            if (line.isBlank()) {
                if (!resultLines.isEmpty() && !resultLines.get(resultLines.size() - 1).isBlank()) {
                    resultLines.add("");
                }
                continue;
            }
            resultLines.add(line);
        }
        return String.join("\n", resultLines)
                .replaceAll("\n{3,}", "\n\n")
                .trim();
    }

    private String normalizeLine(String rawLine) {
        String line = rawLine == null ? "" : rawLine.trim();
        line = MARKDOWN_HEADING_PATTERN.matcher(line).replaceFirst("");
        line = line.replaceFirst("^>\\s*", "");
        line = BULLET_PATTERN.matcher(line).replaceFirst("");

        java.util.regex.Matcher orderedMatcher = ORDERED_LIST_PATTERN.matcher(line);
        if (orderedMatcher.find()) {
            String number = orderedMatcher.group(1);
            line = orderedMatcher.replaceFirst(number + ". ");
        }

        if (SECTION_TITLES.contains(line)) {
            return line + "：";
        }

        return line;
    }
}
