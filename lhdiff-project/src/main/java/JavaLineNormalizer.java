import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Clean Java file normalizer implementation (separate file: JavaLineNormalizer.java)
 */
public class JavaLineNormalizer {

    /**
     * Stateful normalizer that keeps minimal parsing state across lines.
     * Use one instance per file/stream to correctly handle multi-line block
     * comments and Java text blocks.
     */
    public static class StatefulNormalizer {
        // true when we're inside a /* ... */ block comment
        private boolean inBlockComment = false;
        // true when we're inside a Java text block (""" ... """)
        private boolean inTextBlock = false;

        /** Reset internal state to initial (not in comment/text-block). */
        public void reset() {
            inBlockComment = false;
            inTextBlock = false;
        }

        /**
         * Normalize a single input line.
         * Behavior summary:
         * - Removes // single-line comments (unless inside a string/char/text block).
         * - Skips content inside /* ... * / block comments across lines.
         * - Preserves string and char literals (so comment-like text inside them remains).
         * - Collapses consecutive whitespace into a single space and trims the line.
         * - Removes braces and semicolons (preserves other punctuation).
         * - Lowercases the result to remove capitalization differences.
         *
         * This is a lightweight scanner — not a full Java parser — but sufficient
         * for producing comparable normalized text for most source files.
         */
        public String normalizeLine(String line) {
            if (line == null) return "";

            StringBuilder out = new StringBuilder();
            int len = line.length();
            int i = 0;

            while (i < len) {
                char c = line.charAt(i);

                // If currently inside a block comment, skip until we find */
                if (inBlockComment) {
                    if (c == '*' && i + 1 < len && line.charAt(i + 1) == '/') {
                        inBlockComment = false; // close block comment
                        i += 2; // skip '*/'
                        continue;
                    }
                    i++; // keep skipping
                    continue;
                }

                // If inside a text block ("""), copy raw text until closing triple quotes
                if (inTextBlock) {
                    if (c == '"' && i + 2 < len && line.charAt(i + 1) == '"' && line.charAt(i + 2) == '"') {
                        inTextBlock = false; // close text block
                        i += 3; // skip the closing """
                        continue;
                    } else {
                        out.append(c); // keep text block content verbatim
                        i++;
                        continue;
                    }
                }

                // Detect start of single-line comment // (when not inside string/char)
                if (c == '/' && i + 1 < len && line.charAt(i + 1) == '/') {
                    break; // ignore the rest of the line
                }

                // Detect start of block comment /*
                if (c == '/' && i + 1 < len && line.charAt(i + 1) == '*') {
                    inBlockComment = true; // enter block comment state
                    i += 2; // skip '/*'
                    continue;
                }

                // Detect start of Java text block """
                if (c == '"' && i + 2 < len && line.charAt(i + 1) == '"' && line.charAt(i + 2) == '"') {
                    inTextBlock = true; // enter text block
                    i += 3; // skip opening """
                    continue;
                }

                // Detect string literal — copy verbatim and respect escapes
                if (c == '"') {
                    out.append(c);
                    i++;
                    while (i < len) {
                        char ch = line.charAt(i);
                        out.append(ch);
                        if (ch == '\\') {
                            // escaped character: include the next char too (if any)
                            if (i + 1 < len) {
                                i++;
                                out.append(line.charAt(i));
                            }
                            i++;
                            continue;
                        }
                        if (ch == '"') {
                            i++; // end of string literal
                            break;
                        }
                        i++;
                    }
                    continue;
                }

                // Detect char literal — copy verbatim and respect escapes
                if (c == '\'') {
                    out.append(c);
                    i++;
                    while (i < len) {
                        char ch = line.charAt(i);
                        out.append(ch);
                        if (ch == '\\') {
                            if (i + 1 < len) {
                                i++;
                                out.append(line.charAt(i));
                            }
                            i++;
                            continue;
                        }
                        if (ch == '\'') {
                            i++; // end of char literal
                            break;
                        }
                        i++;
                    }
                    continue;
                }

                // Default: copy the character
                out.append(c);
                i++;
            }

            // Post-process normalized line: remove braces/semicolons, collapse whitespace, lowercase
            String result = out.toString();
            // Keep braces ({ and }) but remove semicolons.
            result = result.replace(";", "");

            // ADDED Standardize method/function calls to force alignment
            // This replaces any valid Java identifier followed by '(' with 'func_id('.
            result = result.replaceAll("\\b[a-zA-Z_][a-zA-Z0-9_]*\\s*\\(", "func_id(");

            result = result.trim().replaceAll("\\s+", " ");
            result = result.toLowerCase();

            // Distinguish between code lines and empty/comment lines.
            // If the line is empty after cleaning, use a special token "BLANK" to ensure 
            // all comments/blank lines align with each other, rather than misaligning code.
            if (result.isEmpty()) {
                // Using a unique token ensures blank/comment lines map to each other, not code lines
                return "BLANK_TOKEN"; 
            }

            return result;
        }
    }

    /**
     * Normalize an entire Java source file read from the given path.
     * Returns the normalized content as a single string (lines separated by the
     * system line separator). This method does not modify the input file.
     */
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

    /**
     * Write the normalized content of `input` to `output` (overwrites output).
     */
    public static void normalizeFileTo(Path input, Path output) throws IOException {
        String normalized = normalizeFile(input);
        try (BufferedWriter w = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
            w.write(normalized);
        }
    }

    /**
     * CLI entry: java JavaLineNormalizer <input.java> [output.txt]
     * If output is omitted, normalized content is printed to stdout.
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: JavaLineNormalizer <input.java> [output.txt]");
            System.exit(2);
        }
        Path in = Path.of(args[0]);
        if (args.length >= 2) {
            Path out = Path.of(args[1]);
            normalizeFileTo(in, out);
        } else {
            System.out.println(normalizeFile(in));
        }
    }
}