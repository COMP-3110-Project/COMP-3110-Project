import unittest
import os
import glob
import xml.etree.ElementTree as ET
from lhdiff import LHDiff  # Imports your LHDiff class from lhdiff.py

class TestLHDiffDataset(unittest.TestCase):

    def setUp(self):
        """No dataset check needed now because multiple folders are tested."""
        pass

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
            
            version = root.find("VERSION")
            if version is None: 
                return mapping

            for loc in version.findall("LOCATION"):
                orig = loc.get("ORIG")
                new = loc.get("NEW")

                try:
                    orig_int = int(orig)
                    mapping[orig_int] = new
                except ValueError:
                    continue
                    
        except Exception as e:
            print(f"Error parsing XML {xml_path}: {e}")
            
        return mapping

    def run_lhdiff_and_get_mapping(self, old_file, new_file):
        """
        Runs the LHDiff algorithm and captures the internal mapping.
        """
        tracker = LHDiff(old_file, new_file)
        tracker.run()
        
        result_mapping = {}

        for i, line in enumerate(tracker.old_lines):
            if line['is_empty']:
                continue

            old_num = i + 1

            if i in tracker.mapping:
                target = tracker.mapping[i]

                if isinstance(target, list):
                    target_sorted = sorted([t + 1 for t in target])
                    new_str = ",".join(map(str, target_sorted))
                    result_mapping[old_num] = new_str
                else:
                    result_mapping[old_num] = str(target + 1)
            else:
                result_mapping[old_num] = "-1"

        return result_mapping

    def test_all_xml_cases(self):
        """Iterate over all 4 dataset folders and run tests for each."""
        
        dataset_folders = ["Aakanksha", "Ifechi", "Sargun", "Atefeh"]

        all_files = 0
        all_passed_files = 0
        total_files = 0
        passed_files = 0

        for folder in dataset_folders:
            folder_path = os.path.join("Datasets", folder)
            print(f"\n=== Testing folder: {folder} ===")

            xml_files = glob.glob(os.path.join(folder_path, "*.xml"))
            if not xml_files:
                print(f"No XML files found in {folder_path}, skipping.")
                continue

            print(f"\nFound {len(xml_files)} test cases in {folder}.")
            print("-" * 60)
            print(f"{'Test File':<30} | {'Status':<10} | {'Accuracy'}")
            print("-" * 60)

            total_files = 0
            passed_files = 0

            for xml_file in xml_files:
                total_files += 1
                base_name = os.path.splitext(os.path.basename(xml_file))[0]

                old_source = os.path.join(folder_path, f"{base_name}_Old.py")
                new_source = os.path.join(folder_path, f"{base_name}_New.py")

                # fallback if old is FileX.py
                if not os.path.exists(old_source):
                    old_source = os.path.join(folder_path, f"{base_name}.py")

                if not os.path.exists(old_source) or not os.path.exists(new_source):
                    print(f"{base_name:<30} | SKIPPED    | Missing Source Files")
                    continue

                expected_map = self.parse_xml_ground_truth(xml_file)
                expected_clean = {k: v for k, v in expected_map.items() if k > 0}

                actual_map = self.run_lhdiff_and_get_mapping(old_source, new_source)

                match_count = 0
                total_checked = 0
                failures = []

                for old_line, expected_new in expected_clean.items():
                    total_checked += 1
                    
                    if old_line not in actual_map:
                        failures.append(f"Line {old_line}: Expected {expected_new}, Got None")
                        continue

                    actual_new = actual_map[old_line]
                    if actual_new == expected_new:
                        match_count += 1
                    else:
                        failures.append(
                            f"Line {old_line}: Expected {expected_new}, Got {actual_new}"
                        )

                accuracy = (match_count / total_checked) * 100 if total_checked else 100
                status = "PASS" if accuracy == 100 else "FAIL"
                
                if status == "PASS":
                    passed_files += 1

                print(f"{base_name:<30} | {status:<10} | {accuracy:.1f}%")

                if status == "FAIL":
                    for f in failures:
                        print(f"   [x] {f}")
                    print("")

            print("-" * 60)
            print(f"{folder}: Passed {passed_files}/{total_files} tests")

            all_files += total_files
            all_passed_files += passed_files

        print(f"Total: Passed {all_passed_files}/{all_files} tests\n")


if __name__ == "__main__":
    unittest.main()