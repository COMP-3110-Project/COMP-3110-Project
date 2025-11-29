import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class JavaLineNormalizer {
    public static class StatefulNormalizer {
        private boolean inBlockComment = false;
        private boolean inTextBlock = false;

        public void reset() {
            inBlockComment = false;
            inTextBlock = false;
        }

        public String normalizeLine(String line) {
            if (line == null) return "";
            StringBuilder out = new StringBuilder();
            int len = line.length();
            int i = 0;
            while (i < len) {
                char c = line.charAt(i);

                if (inBlockComment) {
                    if (c == '*' && i + 1 < len && line.charAt(i + 1) == '/') {
                        inBlockComment = false;
                        i += 2;
                        continue;
                    }
                    i++;
                    continue;
                }
                if (inTextBlock) {
                    if (c == '"' && i + 2 < len && line.charAt(i + 1) == '"' && line.charAt(i + 2) == '"') {
                        inTextBlock = false;
                        i += 3;
                        continue;
                    } else {
                        out.append(c);
                        i++;
                        continue;
                    }
                }
                if (c == '/' && i + 1 < len && line.charAt(i + 1) == '/') break;
                if (c == '/' && i + 1 < len && line.charAt(i + 1) == '*') {
                    inBlockComment = true;
                    i += 2;
                    continue;
                }
                if (c == '"' && i + 2 < len && line.charAt(i + 1) == '"' && line.charAt(i + 2) == '"') {
                    inTextBlock = true;
                    i += 3;
                    continue;
                }
                if (c == '"') {
                    out.append(c);
                    i++;
                    while (i < len) {
                        char ch = line.charAt(i);
                        out.append(ch);
                        if (ch == '\\') {
                            if (i + 1 < len) { i++; out.append(line.charAt(i)); }
                            i++; continue;
                        }
                        if (ch == '"') { i++; break; }
                        i++;
                    }
                    continue;
                }
                if (c == '\'') {
                    out.append(c);
                    i++;
                    while (i < len) {
                        char ch = line.charAt(i);
                        out.append(ch);
                        if (ch == '\\') {
                            if (i + 1 < len) { i++; out.append(line.charAt(i)); }
                            i++; continue;
                        }
                        if (ch == '\'') { i++; break; }
                        i++;
                    }
                    continue;
                }
                out.append(c);
                i++;
            }
            String result = out.toString();
            result = result.replace(";", "");
            // Corrected Regex for function identifiers
            result = result.replaceAll("\\b[a-zA-Z_][a-zA-Z0-9_]*\\s*\\(", "func_id(");
            result = result.trim().replaceAll("\\s+", " ");
            result = result.toLowerCase();
            if (result.isEmpty()) return "BLANK_TOKEN";
            return result;
        }
    }

    public static String normalizeFile(Path input) throws IOException {
        StatefulNormalizer norm = new StatefulNormalizer();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = Files.newBufferedReader(input, StandardCharsets.UTF_8)) {
            String line;
            boolean first = true;
            while ((line = r.readLine()) != null) {
                String n = norm.normalizeLine(line);
                if (!first) sb.append(System.lineSeparator());
                sb.append(n);
                first = false;
            }
        }
        return sb.toString();
    }
    
    public static void normalizeFileTo(Path input, Path output) throws IOException {
        String normalized = normalizeFile(input);
        try (BufferedWriter w = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) { w.write(normalized); }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) System.exit(2);
        Path in = Path.of(args[0]);
        if (args.length >= 2) normalizeFileTo(in, Path.of(args[1]));
        else System.out.println(normalizeFile(in));
    }
}