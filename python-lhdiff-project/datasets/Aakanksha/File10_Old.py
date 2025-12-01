# File10_Old.py

class File10:
    @staticmethod
    def calculate_sum(limit):
        total = 0
        # The loop is exclusive of the limit
        for i in range(limit):
            total += i
        return total