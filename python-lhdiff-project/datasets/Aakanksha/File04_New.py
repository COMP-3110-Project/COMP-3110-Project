# File04_New.py - Line Splitting

class File04:
    def process_configuration(self, host, port, username, password):
        # The long initialization line is now split
        connection_string = f"jdbc:mysql://{host}:{port}/"
        connection_string += f"{username}?password={password}&useSSL=false"

        print("Connecting to:", connection_string)
        print("Connection established.")
        # More code...