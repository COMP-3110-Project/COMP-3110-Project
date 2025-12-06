class File5: 
    int thumbs

    def shout(message):
        return message.upper()

    print(reverse_text("python"))
    print(shout("hello"))

    def get_max(a, b):
        if a > b:
            return a
        return b

    # NEW: comment added describing method
    def is_even(n):
        return n % 2 == 0

    # NEW: testing comments added
    print(get_max(10, 7))
    print(is_even(10))

    def countdown(n):
        print("Starting countdown...")
        while n > 0:
            print(n)
            n -= 1
            time.sleep(1)
        print("Done!")