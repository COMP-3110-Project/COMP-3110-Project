# File08_Old.py

class File08:
    def __init__(self):
        self.counter = 0

    def increment(self):
        self.counter += 1
        print("Counter:", self.counter)  # This line

    def reset_and_log(self, initial_value):
        self.counter = initial_value
        log_message = f"Counter reset to {initial_value}."
        print(log_message)