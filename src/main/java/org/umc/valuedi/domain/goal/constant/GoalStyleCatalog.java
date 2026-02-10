package org.umc.valuedi.domain.goal.constant;

import java.util.List;
import java.util.Set;

public final class GoalStyleCatalog {

    private GoalStyleCatalog() {}

    public record ColorOption(String code, String name) {}
    public record IconOption(int id, String name) {}

    public static final List<ColorOption> COLORS = List.of(
            new ColorOption("FF6363", "red"),
            new ColorOption("FF7B2E", "red_orange"),
            new ColorOption("FFA938", "orange"),
            new ColorOption("FA73E3", "pink"),
            new ColorOption("D478FF", "purple"),
            new ColorOption("7D5EF7", "violet"),
            new ColorOption("3385FF", "blue"),
            new ColorOption("3DC2FF", "lightblue"),
            new ColorOption("28D0ED", "cyan"),
            new ColorOption("1ED45A", "green"),
            new ColorOption("6BE016", "lime"),
            new ColorOption("FFEB38", "yellow")
    );

    public static final List<IconOption> ICONS = List.of(
            new IconOption(1, "데이트"),
            new IconOption(2, "관심1"),
            new IconOption(3, "관심2"),
            new IconOption(4, "독서"),
            new IconOption(5, "목표"),
            new IconOption(6, "오락"),
            new IconOption(7, "여행"),
            new IconOption(8, "식사"),
            new IconOption(9, "쇼핑"),
            new IconOption(10, "주택"),
            new IconOption(11, "저축")
    );

    private static final Set<String> COLOR_SET = Set.copyOf(
            COLORS.stream().map(ColorOption::code).toList()
    );

    private static final Set<Integer> ICON_SET = ICONS.stream()
            .mapToInt(IconOption::id)
            .boxed()
            .collect(java.util.stream.Collectors.toSet());

    public static boolean isValidColor(String code) {
        if (code == null) return false;
        // 혹시 #FF6363 같이 들어오면 방어
        String normalized = code.startsWith("#") ? code.substring(1) : code;
        return COLOR_SET.contains(normalized);
    }

    public static String normalizeColor(String code) {
        if (code == null) return null;
        return code.startsWith("#") ? code.substring(1) : code;
    }

    public static boolean isValidIcon(Integer iconId) {
        if (iconId == null) return false;
        return ICON_SET.contains(iconId);
    }
}
