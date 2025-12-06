class File6:


    def farewell():     # NEW: this function now appears first
        return "Goodbye!"

    def greet():        # NEW: greet moved below farewell
        return "Hello!"

    # SAME LOGIC — ordering changed
    message1 = greet()
    message2 = farewell()

    print(message1)
    print(message2)