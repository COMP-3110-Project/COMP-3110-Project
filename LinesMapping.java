

import com.github.difflib.DiffUtils;// for using the diff method 
import com.github.difflib.patch.AbstractDelta;//
import com.github.difflib.patch.Chunk;//Comes from a delta inside a patch Holds: starting position,list of lines changed,old vs new text
import com.github.difflib.patch.Patch;//  created by diffs to Holds all diffs and Can be looped, applied, reversed
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LinesMapping {
    /** One normalized line with its original line number. */
    public static class SettingLineRecord{
        public final int originalLineNumber;// the real number line in the file
        public final String normalized;// holds the clean text from Step
       
      /*---Constructor: saves the line number and it normalized text-----*/
        public SettingLineRecord(int originalLineNumber, String normalized) {
            this.originalLineNumber = originalLineNumber;
            this.normalized = normalized;
            } 
    /*------------DIFF ALGORITHM---------------*/
  
  //  Reads a source file and normalizes every line. and after this method the record has  the line number + normalized text cant go back if we have error 
    /*List<SettingLineRecord>  is the return type
     * readAndNormalize(Path file, List<String> outNormalizedStrings) :input parameters  */
    private List<SettingLineRecord> readAndNormalize(Path file, List<String> outNormalizedStrings) throws Exception {// 
        JavaLineNormalizer.StatefulNormalizer normalizer = new JavaLineNormalizer.StatefulNormalizer();//dedicated normalizer for different file so we can normalize it 
        List<SettingLineRecord> records = new ArrayList<>();// creating a empty list to hold the lines after normalization result 
        List<String> lines;//list of strings that holds raw lines(temporary) of the file after reading for the for loop
        // helps to normalize  each line easily and index postion is easier to find 
        try {
            // Try to read the file
            lines = Files.readAllLines(file);
        } catch (IOException e) {
            // If something goes wrong, print the error because algorithm needs files to continue
            System.err.println("Error reading file: " + file);
            throw e;
        }
        for (int i = 0; i < lines.size(); i++) {// going through each line in the file
        	 String rs = lines.get(i);
        	    String r = normalizer.normalizeLine(rs);//      // Normalization of lines happening
            outNormalizedStrings.add(r);// Saved  normalized text into a string list 
            records.add(new SettingLineRecord(i+1, r));//SAVE THE NORMALIZED TEXT ALONG  ITS ORIGINAL LINE INDEX 
        }return records;
    }/**
     * Run Step 2 using java-diff-utils:
     *  - compute a diff on normalized lines
     *  - derive anchor pairs for unchanged segments
     *  - everything else becomes unmapped.
     */
    public Step2Result run(Path oldFile, Path newFile) throws Exception {
        // 1) Normalize both files (Step 1)
        List<String> oldNormStrings = new ArrayList<>();
        List<String> newNormStrings = new ArrayList<>();
        
        List<SettingLineRecord> oldLines = readAndNormalize(oldFile, oldNormStrings);
        List<SettingLineRecord> newLines = readAndNormalize(newFile, newNormStrings);
        
        Step2Result result = new Step2Result(oldLines.size(), newLines.size());

 // patch holds result of diff (AbstractDelta<String> delta object that is each line that is(inserted/removed/changed)
        Patch<String> patch = DiffUtils.diff(oldNormStrings, newNormStrings);// computing  the differences between the old and new normalized line sequences. 
        // CURRENT LOACATION -Cursors for the tracking where you currently are in the old and new line lists as you walk through the diff.
        //They help find the “unchanged” regions before each delta and after the last one.
        int oldIndex = 0;
        int newIndex = 0;
        
     //   We are looping over every change that the diff tool found, one by one, in order from top to bottom.
        for (AbstractDelta<String> delta : patch.getDeltas()) {
    //AFFECTED BY DELTA LINES 
            Chunk<String> src = delta.getSource(); // old side
            Chunk<String> tgt = delta.getTarget(); // new side
          /*  src: The block of lines in the old list affected by this Line that is 
            tgt: The corresponding block in the new list.  */
            
         //STARTING INDEX OF LINE THAT ARE (inserted/removed/changed)
            int srcPos = src.getPosition();
            int tgtPos = tgt.getPosition();
            /* srcPos: starting index in oldNormStrings where this changed block begins.
			tgtPos: starting index in newNormStrings where the corresponding changed block begins.*/
            
            
        //    How many unchanged lines are there between where we are now and where the next change begins?
            int unchangedCount = srcPos - oldIndex; // should equal (tgtPos - newIndex)
      
            
            /* Loops through each unchanged line in this region.
            i: index in old file for this unchanged line.
            j: index in new file for the corresponding unchanged line.*/        
            //k is just a counter that moves through the unchanged lines.
            
            for (int k = 0; k < unchangedCount; k++) {
            	
            	//we want to detect and store matching lines (anchors).
                int i = oldIndex + k;
                int j = newIndex + k;
                String text = oldLines.get(i).normalized;//Fetches the normalized text for this old line index i.
              /*  Condition:
                	!text.isEmpty():Skip anchors for lines that are empty after normalization 
                	text.equals(newLines.get(j).normalized): Double-check that the normalized texts truly match between old and new.
                	IF BOTH CONDITIONS ARE TRUE: 
                	Adds an “anchor” pair {i, j} to result.anchors.//Line i in the old file and line j in the new file are considered the “same” logical line.
					So this loop is effectively building a mapping:
                	This means line i in the old file corresponds to line j in the new file and is unchanged.
                	for each unchanged line that you want to treat as a stable reference point (anchor).*/
                 if (!text.isEmpty() && text.equals(newLines.get(j).normalized)) {
                    result.anchors.add(new int[]{i, j});
                    
                } }
            
            // Skip over the changed block on both sides
            oldIndex = srcPos + src.size();
            newIndex = tgtPos + tgt.size();}
       
        
        /*Moves oldIndex and newIndex past the changed block:
src.size() = number of lines changed/removed in old.
tgt.size() = number of lines changed/inserted in new.
After this, oldIndex and newIndex point to the first line after the changed block.*/
        
        
        // 4) Handle any unchanged  after the last delta
        int remainingOld = oldLines.size() - oldIndex;
        int remainingNew = newLines.size() - newIndex;
        int tail = Math.min(remainingOld, remainingNew);
        for (int k = 0; k < tail; k++) {
            int i = oldIndex + k;
            int j = newIndex + k;
            String text = oldLines.get(i).normalized;
            if (!text.isEmpty() && text.equals(newLines.get(j).normalized)) {
                result.anchors.add(new int[]{i, j});
            }  }
        Set<Integer> mappedOld = new HashSet<>();
        Set<Integer> mappedNew = new HashSet<>();
        for (int[] pair : result.anchors) {
            mappedOld.add(pair[0]);
            mappedNew.add(pair[1]);}
        for (int i = 0; i < oldLines.size(); i++) {
            if (!mappedOld.contains(i)) {
                result.unmappedOld.add(i); }}
        for (int j = 0; j < newLines.size(); j++) {
            if (!mappedNew.contains(j)) {
                result.unmappedNew.add(j);
            } } return result; }
    
    }}	

/*All the normalized lines of the old file.
All the normalized lines of the new file.
For each unchanged line: the pair (oldIndex, newIndex) → anchors.
For old lines that don’t have a match: their indices → unmappedOld.
For new lines that don’t have a match: their indices → unmappedNew.
*/
