import time
class File2:
    def countdown(self, n):
        print("Starting countdown...")
        while n > 0:
            print(n)
            n -= 1
        time.sleep(1)
    print("Done!")

countdown(5)