# File06_New.py

class File06:
    def __init__(self, name):
        self.account_holder_name = name  # Renamed variable

    def get_account_holder_name(self):  # Renamed method
        return self.account_holder_name

    def display_user_info(self):
        print("Account Holder:", self.get_account_holder_name())  # Usage updated