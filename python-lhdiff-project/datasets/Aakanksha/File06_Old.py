# File06_Old.py

class File06:
    def __init__(self, name):
        self.user_name = name  # Variable to be renamed

    def get_user_name(self):  # Method to be renamed
        return self.user_name

    def display_user_info(self):
        print("User:", self.get_user_name())