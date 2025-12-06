# File02_New.py

class File02:
    def process_data(self, data):
        print("Initializing data processing...")  # Modified

        # New line: Validate data first
        if data is None or len(data) == 0:
            print("No data to process.")
            return  # Early exit

        for item in data:
            # Processing message removed
            if "invalid" in item:  # Modified condition
                print("Invalid item:", item, file=sys.stderr)

        print("Data processing completed.")  # Modified