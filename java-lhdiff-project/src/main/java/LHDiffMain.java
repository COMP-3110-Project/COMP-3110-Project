import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.*;

public class LHDiffMain {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java LHDiffMain <OldFile> <NewFile>");
            return;
        }

        Path oldFile = Paths.get(args[0]);
        Path newFile = Paths.get(args[1]);

        try {
            // --- Initialization ---
            LinesMapping linesMapping = new LinesMapping();
            SimhashGenerator simhashGenerator = new SimhashGenerator();
            MappingResolver mappingResolver = new MappingResolver();
            Step5LineSplitDetector splitDetector = new Step5LineSplitDetector();

            // 1. Run Step 1 & 2 (Diff)
            Step2Result step2 = linesMapping.run(oldFile, newFile);

            // Re-read/Normalize
            List<String> oldNormStrings = new ArrayList<>();
            List<LinesMapping.SettingLineRecord> oldRecords = readAndGetRecords(oldFile, oldNormStrings);

            List<String> newNormStrings = new ArrayList<>();
            List<LinesMapping.SettingLineRecord> newRecords = readAndGetRecords(newFile, newNormStrings);

            // Read RAW lines
            List<String> oldRawLines = Files.readAllLines(oldFile);
            List<String> newRawLines = Files.readAllLines(newFile);

            // 2. Run Step 3 (Candidate Generation)
            Set<Integer> mappedOld = new HashSet<>();
            Set<Integer> mappedNew = new HashSet<>();

            for (int[] pair : step2.anchors) {
                mappedOld.add(pair[0]); 
                mappedNew.add(pair[1]);
            }

            Map<Integer, List<SimhashGenerator.LineSimhash.Candidate>> candidates = SimhashGenerator.LineSimhash
                    .generateCandidates(
                            oldNormStrings,
                            newNormStrings,
                            mappedOld,
                            mappedNew,
                            4, 
                            15 
                    );

            // 3. Run Step 4 (Resolve Conflicts)
            List<MappingResolver.SettingLineRecord> oldRecs4 = convertToResolverRecords(oldRecords);
            List<MappingResolver.SettingLineRecord> newRecs4 = convertToResolverRecords(newRecords);
            Map<Integer, List<MappingResolver.Candidate>> resolverCandidates = convertToResolverCandidates(candidates);

            Map<Integer, Integer> step4Matches = mappingResolver.resolveCandidates(
                    oldRecs4, newRecs4,
                    oldRawLines, 
                    newRawLines,
                    resolverCandidates,
                    step2.unmappedOld,
                    step2.unmappedNew);

            // 4. Run Step 5 (Detect Line Splits)
            Map<Integer, List<Integer>> step5Matches = splitDetector.detectSplits(
                    oldRecords,
                    newRecords,
                    step2.unmappedOld, 
                    step2.unmappedNew);

            // --- Aggregate Matches ---
            TreeMap<Integer, String> finalOutput = new TreeMap<>();

            for (int[] pair : step2.anchors) {
                finalOutput.put(pair[0] + 1, String.valueOf(pair[1] + 1));
            }
            for (Map.Entry<Integer, Integer> entry : step4Matches.entrySet()) {
                finalOutput.put(entry.getKey() + 1, String.valueOf(entry.getValue() + 1));
            }
            for (Map.Entry<Integer, List<Integer>> entry : step5Matches.entrySet()) {
                List<Integer> targets = entry.getValue();
                Collections.sort(targets);
                String val = (targets.size() == 1) ? 
                    String.valueOf(targets.get(0) + 1) : 
                    (targets.get(0) + 1) + "-" + (targets.get(targets.size() - 1) + 1);
                finalOutput.put(entry.getKey() + 1, val);
            }

            // 5. Run Step 6 (Zipper)
            runZipperPass(finalOutput, oldRecords.size(), newRecords.size());

            // --- FINAL OUTPUT GENERATION (Format: "1 -> 1") ---
            
            // Print Mappings and Deletions
            for (int i = 1; i <= oldRecords.size(); i++) {
                String oldRaw = oldRawLines.get(i - 1);
                if (oldRaw.trim().isEmpty()) continue; 

                if (finalOutput.containsKey(i)) {
                    // CHANGE: Output format changed to " -> "
                    System.out.println(i + " -> " + finalOutput.get(i));
                } else {
                    // CHANGE: Output format changed to " -> "
                    System.out.println(i + " -> -1"); 
                }
            }

            // Calculate mapped new indices
            Set<Integer> mappedNewIndices = new HashSet<>();
            for (String val : finalOutput.values()) {
                if (val.contains("-")) {
                    String[] parts = val.split("-");
                    int start = Integer.parseInt(parts[0]);
                    int end = Integer.parseInt(parts[1]);
                    for (int k = start; k <= end; k++)
                        mappedNewIndices.add(k);
                } else {
                    mappedNewIndices.add(Integer.parseInt(val));
                }
            }

            // Print Added lines
            for (int j = 1; j <= newRecords.size(); j++) {
                String newRaw = newRawLines.get(j - 1);
                if (newRaw.trim().isEmpty()) continue;

                if (!mappedNewIndices.contains(j)) {
                    // CHANGE: Output format changed to " -> "
                    System.out.println("-1 -> " + j);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void runZipperPass(TreeMap<Integer, String> mapping, int maxOld, int maxNew) {
        List<int[]> anchors = new ArrayList<>();
        anchors.add(new int[]{0, 0}); 

        for (Map.Entry<Integer, String> entry : mapping.entrySet()) {
            String val = entry.getValue();
            if (!val.contains("-")) {
                anchors.add(new int[]{entry.getKey(), Integer.parseInt(val)});
            }
        }
        anchors.add(new int[]{maxOld + 1, maxNew + 1});
        anchors.sort(Comparator.comparingInt(a -> a[0]));

        for (int i = 0; i < anchors.size() - 1; i++) {
            int[] curr = anchors.get(i);
            int[] next = anchors.get(i+1);

            int oldGapStart = curr[0] + 1;
            int oldGapEnd = next[0] - 1;
            int newGapStart = curr[1] + 1;
            int newGapEnd = next[1] - 1;

            int oldGapSize = Math.max(0, oldGapEnd - oldGapStart + 1);
            int newGapSize = Math.max(0, newGapEnd - newGapStart + 1);

            if (oldGapSize > 0 && oldGapSize == newGapSize) {
                for (int k = 0; k < oldGapSize; k++) {
                    mapping.putIfAbsent(oldGapStart + k, String.valueOf(newGapStart + k));
                }
            }
        }
    }

    // --- Helpers (Same as before) ---
    private static List<LinesMapping.SettingLineRecord> readAndGetRecords(Path p, List<String> rawList) throws Exception {
        JavaLineNormalizer.StatefulNormalizer norm = new JavaLineNormalizer.StatefulNormalizer();
        List<LinesMapping.SettingLineRecord> recs = new ArrayList<>();
        List<String> lines = java.nio.file.Files.readAllLines(p);
        for (int i = 0; i < lines.size(); i++) {
            String n = norm.normalizeLine(lines.get(i));
            rawList.add(n);
            recs.add(new LinesMapping.SettingLineRecord(i + 1, n));
        }
        return recs;
    }

    private static List<MappingResolver.SettingLineRecord> convertToResolverRecords(List<LinesMapping.SettingLineRecord> src) {
        List<MappingResolver.SettingLineRecord> out = new ArrayList<>();
        for (LinesMapping.SettingLineRecord s : src) {
            out.add(new MappingResolver.SettingLineRecord(s.originalLineNumber - 1, s.normalized, ""));
        }
        return out;
    }

    private static Map<Integer, List<MappingResolver.Candidate>> convertToResolverCandidates(Map<Integer, List<SimhashGenerator.LineSimhash.Candidate>> src) {
        Map<Integer, List<MappingResolver.Candidate>> out = new HashMap<>();
        for (Map.Entry<Integer, List<SimhashGenerator.LineSimhash.Candidate>> e : src.entrySet()) {
            List<MappingResolver.Candidate> list = new ArrayList<>();
            for (SimhashGenerator.LineSimhash.Candidate c : e.getValue()) {
                list.add(new MappingResolver.Candidate(c.newIndex, c.hammingDistance));
            }
            out.put(e.getKey(), list);
        }
        return out;
    }
}