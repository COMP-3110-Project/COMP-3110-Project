import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Chunk;
import com.github.difflib.patch.Patch;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LinesMapping {
    public static class SettingLineRecord {
        public final int originalLineNumber;
        public final String normalized;
        public SettingLineRecord(int originalLineNumber, String normalized) {
            this.originalLineNumber = originalLineNumber;
            this.normalized = normalized;
        }
    }

    private List<SettingLineRecord> readAndNormalize(Path file, List<String> outNormalizedStrings) throws Exception {
        JavaLineNormalizer.StatefulNormalizer normalizer = new JavaLineNormalizer.StatefulNormalizer();
        List<SettingLineRecord> records = new ArrayList<>();
        List<String> lines = Files.readAllLines(file);
        for (int i = 0; i < lines.size(); i++) {
            String r = normalizer.normalizeLine(lines.get(i));
            outNormalizedStrings.add(r);
            records.add(new SettingLineRecord(i + 1, r));
        }
        return records;
    }

    public Step2Result run(Path oldFile, Path newFile) throws Exception {
        List<String> oldNormStrings = new ArrayList<>();
        List<String> newNormStrings = new ArrayList<>();
        List<SettingLineRecord> oldLines = readAndNormalize(oldFile, oldNormStrings);
        List<SettingLineRecord> newLines = readAndNormalize(newFile, newNormStrings);

        Step2Result result = new Step2Result(oldLines.size(), newLines.size());
        Patch<String> patch = DiffUtils.diff(oldNormStrings, newNormStrings);

        int oldIndex = 0;
        int newIndex = 0;

        for (AbstractDelta<String> delta : patch.getDeltas()) {
            Chunk<String> src = delta.getSource();
            Chunk<String> tgt = delta.getTarget();
            int unchangedCount = src.getPosition() - oldIndex;

            for (int k = 0; k < unchangedCount; k++) {
                String text = oldLines.get(oldIndex + k).normalized;
                // FIX: Do not use BLANK_TOKEN as an anchor
                if (text.equals(newLines.get(newIndex + k).normalized) && !text.equals("BLANK_TOKEN")) {
                    result.anchors.add(new int[] { oldIndex + k, newIndex + k });
                }
            }
            oldIndex = src.getPosition() + src.size();
            newIndex = tgt.getPosition() + tgt.size();
        }

        int tail = Math.min(oldLines.size() - oldIndex, newLines.size() - newIndex);
        for (int k = 0; k < tail; k++) {
            String text = oldLines.get(oldIndex + k).normalized;
            // FIX: Do not use BLANK_TOKEN as an anchor
            if (text.equals(newLines.get(newIndex + k).normalized) && !text.equals("BLANK_TOKEN")) {
                result.anchors.add(new int[] { oldIndex + k, newIndex + k });
            }
        }

        Set<Integer> mappedOld = new HashSet<>();
        Set<Integer> mappedNew = new HashSet<>();
        for (int[] pair : result.anchors) {
            mappedOld.add(pair[0]);
            mappedNew.add(pair[1]);
        }
        for (int i = 0; i < oldLines.size(); i++) if (!mappedOld.contains(i)) result.unmappedOld.add(i);
        for (int j = 0; j < newLines.size(); j++) if (!mappedNew.contains(j)) result.unmappedNew.add(j);
        
        return result;
    }
}