import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class LHDiffMain {

    public static void main(String[] args) {
        // Hardcoded paths for testing, or use args
        if(args.length < 2) {
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

            // We need to capture the raw Normalized Lists to pass to Step 3, 4, 5
            // LinesMapping.run does this internally, so we might need to modify LinesMapping
            // OR simply re-normalize here for the context of Main.
            // For simplicity, let's rely on LinesMapping to give us the anchors, 
            // and we re-read files for the raw strings needed for context.
            
            // 1. Run Step 1 & 2 (Diff)
            System.out.println("Running Step 1 & 2...");
            // NOTE: You still need to ensure LinesMapping.java defines a 'public Step2Result run(Path, Path)' method.
            Step2Result step2 = linesMapping.run(oldFile, newFile);

            // Re-read/Normalize to get the lists needed for subsequent steps
            List<String> oldNormStrings = new ArrayList<>();
            List<LinesMapping.SettingLineRecord> oldRecords = readAndGetRecords(oldFile, oldNormStrings);
            
            List<String> newNormStrings = new ArrayList<>();
            List<LinesMapping.SettingLineRecord> newRecords = readAndGetRecords(newFile, newNormStrings);

            // 2. Run Step 3 (Candidate Generation)
            System.out.println("Running Step 3...");
            Set<Integer> mappedOld = new HashSet<>();
            Set<Integer> mappedNew = new HashSet<>();
            
            // Fill mapped sets from Step 2 Anchors
            for(int[] pair : step2.anchors) {
                mappedOld.add(pair[0]); // Index in list (0-based)
                mappedNew.add(pair[1]);
            }

            Map<Integer, List<SimhashGenerator.LineSimhash.Candidate>> candidates = 
                SimhashGenerator.LineSimhash.generateCandidates(
                    oldNormStrings, 
                    newNormStrings, 
                    mappedOld, 
                    mappedNew, 
                    4, // Window Size
                    15 // K candidates
                );

            // 3. Run Step 4 (Resolve Conflicts)
            System.out.println("Running Step 4...");
            
            // Convert simple String lists to SettingLineRecord for MappingResolver
            // Note: MappingResolver expects its own inner class SettingLineRecord or the one from LinesMapping.
            // To make types compatible, let's adapt:
            List<MappingResolver.SettingLineRecord> oldRecs4 = convertToResolverRecords(oldRecords);
            List<MappingResolver.SettingLineRecord> newRecs4 = convertToResolverRecords(newRecords);
            
            // Convert SimHash candidates to Resolver candidates
            Map<Integer, List<MappingResolver.Candidate>> resolverCandidates = convertToResolverCandidates(candidates);

            Map<Integer, Integer> step4Matches = mappingResolver.resolveCandidates(
                oldRecs4, newRecs4, 
                oldNormStrings, // Raw lines (normalized) used for context in this implementation
                newNormStrings, 
                resolverCandidates, 
                step2.unmappedOld, 
                step2.unmappedNew
            );

            // 4. Run Step 5 (Detect Line Splits)
            System.out.println("Running Step 5...");
            Map<Integer, List<Integer>> step5Matches = splitDetector.detectSplits(
                oldRecords, 
                newRecords, 
                step2.unmappedOld, // This set is modified inside the method
                step2.unmappedNew
            );

            // --- Final Output Generation ---
            System.out.println("\n=== Final Mapping Results ===");
            
            // Combine all results into a sorted map for display
            // Key: Old Line Number (1-based), Value: String representation of New Line(s)
            TreeMap<Integer, String> finalOutput = new TreeMap<>();

            // Add Step 2 Anchors
            for(int[] pair : step2.anchors) {
                // +1 for 1-based line numbers in output
                finalOutput.put(pair[0] + 1, String.valueOf(pair[1] + 1));
            }

            // Add Step 4 Matches
            for(Map.Entry<Integer, Integer> entry : step4Matches.entrySet()) {
                finalOutput.put(entry.getKey() + 1, String.valueOf(entry.getValue() + 1));
            }

            // Add Step 5 Split Matches
            for(Map.Entry<Integer, List<Integer>> entry : step5Matches.entrySet()) {
                List<Integer> targets = entry.getValue();
                Collections.sort(targets);
                
                // Format as range (e.g., "20-22") or single number
                String val;
                if(targets.size() == 1) {
                    val = String.valueOf(targets.get(0) + 1);
                } else {
                    int start = targets.get(0) + 1;
                    int end = targets.get(targets.size() - 1) + 1;
                    val = start + "-" + end;
                }
                finalOutput.put(entry.getKey() + 1, val);
            }

            // Print
            for(Map.Entry<Integer, String> entry : finalOutput.entrySet()) {
                System.out.println(entry.getKey() + "-" + entry.getValue());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- Helpers to bridge the Gap between classes ---

    private static List<LinesMapping.SettingLineRecord> readAndGetRecords(Path p, List<String> rawList) throws Exception {
        JavaLineNormalizer.StatefulNormalizer norm = new JavaLineNormalizer.StatefulNormalizer();
        List<LinesMapping.SettingLineRecord> recs = new ArrayList<>();
        List<String> lines = java.nio.file.Files.readAllLines(p);
        
        for(int i=0; i<lines.size(); i++) {
            String n = norm.normalizeLine(lines.get(i));
            rawList.add(n); 
            recs.add(new LinesMapping.SettingLineRecord(i+1, n));
        }
        return recs;
    }

    private static List<MappingResolver.SettingLineRecord> convertToResolverRecords(List<LinesMapping.SettingLineRecord> src) {
        List<MappingResolver.SettingLineRecord> out = new ArrayList<>();
        for(LinesMapping.SettingLineRecord s : src) {
            // Passing empty simhash string as it's not strictly used in resolveCandidates (calculated internally or separate)
            // Or if MappingResolver needs it, we might need to compute it. 
            // Based on MappingResolver code, it calculates similarity on fly.
            out.add(new MappingResolver.SettingLineRecord(s.originalLineNumber - 1, s.normalized, ""));
        }
        return out;
    }

    private static Map<Integer, List<MappingResolver.Candidate>> convertToResolverCandidates(Map<Integer, List<SimhashGenerator.LineSimhash.Candidate>> src) {
        Map<Integer, List<MappingResolver.Candidate>> out = new HashMap<>();
        for(Map.Entry<Integer, List<SimhashGenerator.LineSimhash.Candidate>> e : src.entrySet()) {
            List<MappingResolver.Candidate> list = new ArrayList<>();
            for(SimhashGenerator.LineSimhash.Candidate c : e.getValue()) {
                list.add(new MappingResolver.Candidate(c.newIndex, c.hammingDistance));
            }
            out.put(e.getKey(), list);
        }
        return out;
    }
}