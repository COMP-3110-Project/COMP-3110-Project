import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class LHDiffTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    // Use this path variable to point to your folder
    // Note: If running from IDE, ".." usually goes up from the Project Root.
    // Adjust as necessary for your folder structure.
    private static final String DATASET_PATH = "../datasets/Aakanksha";

    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
        outContent.reset(); // Clear buffer for next test
    }

    /**
     * This method automatically finds all XML files in the folder
     * and generates a specific test for each one.
     */
    @TestFactory
    Stream<DynamicTest> testAllFilesInDataset() throws Exception {
        Path dirPath = Paths.get(DATASET_PATH);

        if (!Files.exists(dirPath)) {
            fail("Dataset directory not found at: " + dirPath.toAbsolutePath());
        }

        // 1. Find all .xml files in the directory
        return Files.list(dirPath)
                .filter(path -> path.toString().endsWith(".xml"))
                .map(xmlPath -> {
                    String fileName = xmlPath.getFileName().toString();
                    String baseName = fileName.replace(".xml", ""); // e.g., "File01"

                    // 2. Derive the Old/New java filenames
                    Path oldFile = dirPath.resolve(baseName + "_Old.java");
                    Path newFile = dirPath.resolve(baseName + "_New.java");

                    // 3. Create a Dynamic Test for this specific file set
                    return DynamicTest.dynamicTest("Test: " + baseName, () -> {
                        runSingleTest(oldFile, newFile, xmlPath);
                    });
                });
    }

    /**
     * The logic to run one specific set of files
     */
    private void runSingleTest(Path oldFile, Path newFile, Path expectedXml) throws Exception {
        // Validation
        if (!Files.exists(oldFile)) fail("Missing file: " + oldFile);
        if (!Files.exists(newFile)) fail("Missing file: " + newFile);

        // Reset the output stream capture for this specific run
        outContent.reset();

        // Run Main
        LHDiffMain.main(new String[]{oldFile.toString(), newFile.toString()});

        // Get Output
        String actualConsoleOutput = outContent.toString();

        // Read XML
        String expectedXmlContent = Files.lines(expectedXml)
                .collect(Collectors.joining(System.lineSeparator()));

        // Compare
        compareResults(expectedXmlContent, actualConsoleOutput, oldFile.getFileName().toString());
    }

    // ==========================================
    // COMPARISON LOGIC
    // ==========================================

    private void compareResults(String xmlContent, String consoleOutput, String testName) {
        Set<String> expectedMappings = parseXmlMappings(xmlContent);
        Set<String> actualMappings = parseConsoleMappings(consoleOutput);

        List<String> sortedExpected = new ArrayList<>(expectedMappings);
        List<String> sortedActual = new ArrayList<>(actualMappings);
        Collections.sort(sortedExpected);
        Collections.sort(sortedActual);

        // Detailed error message if empty
        if (sortedActual.isEmpty()) {
            System.err.println("!!! FAILURE IN " + testName + " !!!");
            System.err.println("Program produced no output mappings. Raw output:");
            System.err.println(consoleOutput);
        }

        assertEquals(sortedExpected, sortedActual, "Mismatch in mappings for " + testName);
    }

    private Set<String> parseXmlMappings(String xml) {
        Set<String> mappings = new HashSet<>();
        Pattern pattern = Pattern.compile("ORIG=\"(-?\\d+)\"\\s+NEW=\"(-?\\d+)\"");
        Matcher matcher = pattern.matcher(xml);
        while (matcher.find()) {
            mappings.add(matcher.group(1) + ":" + matcher.group(2));
        }
        return mappings;
    }

    private Set<String> parseConsoleMappings(String output) {
        Set<String> mappings = new HashSet<>();
        String[] lines = output.split("\\R");

        // Relaxed Regex: Matches "1 -> 1" or "1->1"
        Pattern pattern = Pattern.compile("(-?\\d+)\\s*->\\s*(-?\\d+)");

        for (String line : lines) {
            if (line.trim().isEmpty() || line.contains("===") || line.contains("INFO")) continue;

            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                mappings.add(matcher.group(1) + ":" + matcher.group(2));
            }
        }
        return mappings;
    }
}