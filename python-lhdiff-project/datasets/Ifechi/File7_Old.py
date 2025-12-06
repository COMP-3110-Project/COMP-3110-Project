class File7 :
    def format_user(name, age):
    # Prepare a formatted user description
    line1 = "Name: " + name
    line2 = "Age: " + str(age)
    line3 = "Status: Active"
    line4 = "Location: Unknown"
    combined = line1 + ", " + line2
    final = combined + ", " + line3 + ", " + line4
    return final

    def print_user(info):
        print("User Information:")
        print(info)

    username = "Ifechi"
    user_age = 20
    description = format_user(username, user_age)

    print_user(description)
    # End of file