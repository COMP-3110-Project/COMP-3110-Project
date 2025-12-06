class File8:
    def __init__(self):
        self.seconds = 0

    # tick
    def tick(self):
        self.seconds += self.seconds

    def get_time(self):
        return self.seconds