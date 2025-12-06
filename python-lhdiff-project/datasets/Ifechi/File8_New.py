class File8:
    def __init__(self):
        self.seconds = 0

    # advance one second
    def tick(self):
        self.seconds += self.seconds

    # return seconds
    def get_time(self):
        return self.seconds