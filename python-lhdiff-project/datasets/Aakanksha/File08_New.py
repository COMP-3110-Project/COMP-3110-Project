# File08_New.py - Mixed Complex Changes

from time import time

class File08:
    def __init__(self):
        self.counter = 0

    def reset_and_log(self, initial_value):  # Moved method
        self.counter = initial_value
        # Line split
        log_message = "Counter reset to "
        log_message += f"{initial_value}. Logged at {int(time.time() * 1000)}"
        print(log_message)

    def increment(self):  # Moved method
        self.counter += 1
        current_count = f"Current count: {self.counter}"  # Line merged
        print(current_count)  # Line merged

    def decrement(self):  # Added method
        self.counter -= 1
        current_count = f"Current count: {self.counter}"
        print(current_count) 