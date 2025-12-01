import unittest
import os
import glob
import xml.etree.ElementTree as ET
from lhdiff import LHDiff  # Imports your LHDiff class from lhdiff.py

class TestLHDiffDataset(unittest.TestCase):
    
    # Configuration: Path to your dataset folder
    DATASET_DIR = "Datasets/Aakanksha"

    def setUp(self):
        """Checks if dataset directory exists before running tests."""
        if not os.path.exists(self.DATASET_DIR):
            self.skipTest(f"Dataset directory '{self.DATASET_DIR}' not found.")

    def parse_xml_ground_truth(self, xml_path):
        """
        Parses the XML file and returns a dictionary: 
        { old_line_num: new_line_num_string }
        Example: { 1: "1", 5: "7", 8: "-1" }
        """
        mapping = {}
        try:
            tree = ET.parse(xml_path)
            root = tree.getroot()
            
            # Navigate to VERSION -> LOCATION tags
            version = root.find("VERSION")
            if version is None: 
                return mapping

            for loc in version.findall("LOCATION"):
                orig = loc.get("ORIG")
                new = loc.get("NEW")
                
                # We store ORIG as int for sorting/lookup, NEW as str (to handle splits "1,2")
                try:
                    orig_int = int(orig)
                    mapping[orig_int] = new
                except ValueError:
                    continue # Skip invalid integers
                    
        except Exception as e:
            print(f"Error parsing XML {xml_path}: {e}")
            
        return mapping

    def run_lhdiff_and_get_mapping(self, old_file, new_file):
        """
        Runs the LHDiff algorithm and captures the internal mapping state 
        into a format comparable to the XML ground truth.
        """
        tracker = LHDiff(old_file, new_file)
        tracker.run()
        
        # Convert internal mapping (0-based) to output format (1-based)
        # matches: { old_line_int: "new_line_str" }
        result_mapping = {}
        
        # 1. Add mapped lines
        for i, line in enumerate(tracker.old_lines):
            if line['is_empty']: continue
            
            old_num = i + 1
            if i in tracker.mapping:
                target = tracker.mapping[i]
                if isinstance(target, list):
                    # Sort and join for consistent comparison (e.g. "20,21")
                    target_sorted = sorted([t + 1 for t in target])
                    new_str = ",".join(map(str, target_sorted))
                    result_mapping[old_num] = new_str
                else:
                    result_mapping[old_num] = str(target + 1)
            else:
                result_mapping[old_num] = "-1"

        # 2. Add inserted lines (Old line is -1)
        # The XML usually represents insertions as ORIG="-1" NEW="X"
        for j, line in enumerate(tracker.new_lines):
            if line['is_empty']: continue
            
            if j not in tracker.matched_new:
                new_num = str(j + 1)
                # In the XML provided in your examples, insertions are often listed
                # as LOCATION ORIG="-1" NEW="7". However, dictionary keys must be unique.
                # To support multiple insertions, we might handle them differently,
                # but standard practice for line mapping tests often keys by Old Line.
                # 
                # If your XML includes ORIG="-1", we need a way to store them.
                # We will use negative keys starting from -1, -2 etc for collision avoidance
                # or simply verify them if they exist in the XML.
                
                # Check if XML expects this insertion
                # For this simple map, we will store them with a special key convention
                # or skip if your XML doesn't strictly test -1 origins.
                
                # Let's map them to -1 key? No, keys must be unique.
                # We will check insertions separately if needed, but usually 
                # correctness is determined by:
                # 1. Did Old Line X go to Correct New Line Y?
                # 2. Did Old Line Z go to -1 (Deleted)?
                pass

        return result_mapping

    def test_all_xml_cases(self):
        """Iterates over all XML files in the dataset and runs the comparison."""
        
        xml_files = glob.glob(os.path.join(self.DATASET_DIR, "*.xml"))
        
        if not xml_files:
            print("No XML files found in dataset.")
            return

        print(f"\nFound {len(xml_files)} test cases.")
        print("-" * 60)
        print(f"{'Test File':<30} | {'Status':<10} | {'Accuracy'}")
        print("-" * 60)

        total_files = 0
        passed_files = 0

        for xml_file in xml_files:
            total_files += 1
            base_name = os.path.splitext(os.path.basename(xml_file))[0]
            
            # Construct filenames based on standard naming convention
            # Assumes: FileX.xml -> FileX_Old.py and FileX_New.py
            # If your naming is different (e.g., FileX.py), adjust here.
            
            # Try specific pattern first: File01.xml -> File01_Old.py / File01_New.py
            old_source = os.path.join(self.DATASET_DIR, f"{base_name}_Old.py")
            new_source = os.path.join(self.DATASET_DIR, f"{base_name}_New.py")
            
            # Fallback patterns if needed
            if not os.path.exists(old_source):
                 # Try pattern: File01.py (old) / File01_New.py (new)
                 old_source = os.path.join(self.DATASET_DIR, f"{base_name}.py")
            
            if not os.path.exists(old_source) or not os.path.exists(new_source):
                print(f"{base_name:<30} | SKIPPED    | Missing Source Files")
                continue

            # 1. Get Expected Mapping
            expected_map = self.parse_xml_ground_truth(xml_file)
            
            # Filter expected map to only include positive ORIG lines for direct comparison
            # (ignoring insertion checks ORIG="-1" for simpler map comparison unless critical)
            expected_map_clean = {k: v for k, v in expected_map.items() if k > 0}

            # 2. Get Actual Mapping
            actual_map = self.run_lhdiff_and_get_mapping(old_source, new_source)

            # 3. Compare
            match_count = 0
            total_lines_checked = 0
            failures = []

            for old_line, expected_new in expected_map_clean.items():
                total_lines_checked += 1
                
                # Check if our tool even considered this line (might be empty/skipped)
                if old_line not in actual_map:
                    # If XML says map line 5, but our tool skipped it (empty), 
                    # check if XML target was valid. 
                    # If XML expected a mapping, this is a fail.
                    failures.append(f"Line {old_line}: Expected {expected_new}, Got None (Skipped)")
                    continue

                actual_new = actual_map[old_line]
                
                if actual_new == expected_new:
                    match_count += 1
                else:
                    failures.append(f"Line {old_line}: Expected {expected_new}, Got {actual_new}")

            # Calculate score
            if total_lines_checked == 0:
                accuracy = 100.0
            else:
                accuracy = (match_count / total_lines_checked) * 100.0

            status = "PASS" if accuracy == 100.0 else "FAIL"
            if status == "PASS":
                passed_files += 1

            print(f"{base_name:<30} | {status:<10} | {accuracy:.1f}%")
            
            # Optional: Print detailed failures for debugging
            if status == "FAIL":
                for f in failures:
                    print(f"   [x] {f}")
                print("")

        print("-" * 60)
        print(f"Total: {total_files}, Passed: {passed_files}, Failed: {total_files - passed_files}")
        
        # Fail the unittest if any file failed
        self.assertEqual(passed_files, total_files, f"Only {passed_files}/{total_files} tests passed.")

if __name__ == "__main__":
    unittest.main()