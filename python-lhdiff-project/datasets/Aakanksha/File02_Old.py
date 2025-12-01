# File02_Old.py

class File02:
    def process_data(self, data):
        print("Starting data processing...")

        for item in data:
            print("Processing:", item)
            # Intermediate step 1
            if "error" in item:
                print("Error detected for:", item, file=sys.stderr)
            # Intermediate step 2

        print("Data processing finished.")