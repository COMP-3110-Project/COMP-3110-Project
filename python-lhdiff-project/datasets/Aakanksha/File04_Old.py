# File04_Old.py

class File04:
    def process_configuration(self, host, port, username, password):
        # A very long initialization line
        connection_string = (
            f"jdbc:mysql://{host}:{port}/{username}?password={password}&useSSL=false"
        )
        print("Connecting to:", connection_string)
        # More code...