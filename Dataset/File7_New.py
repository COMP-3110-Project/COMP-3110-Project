class File7:
    def format_user(name, age):
       # Prepare a formatted user description
        line1 = "Name: " + name
        line2 = "Age: " + str(age)
        final = line1 + ", " + line2
        return final

    # NEW: condensed print_user function
    def print_user(info):
        print("User Information:", info)   # MERGE: printing done in one line

    username = "Ifechi"
    user_age = 20
    description = format_user(username, user_age)

    print_user(description)   # unchanged
    # End of file