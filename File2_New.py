def example_three():
    data = [3,1,4,1,5]
    max_val = data[0]
    min_val = data[0]
    total = 0
    for value in data:
        if value > max_val:
            max_val = value
        if value < min_val:
            min_val = value
        total += value
    average = total / float(len(data))
    print("Max:", max_val)
    print("Min:", min_val)
    print("Average:", average)
def example_one():
    a = 5
    b = 10
    s = a + b
    print("Sum:", s)
def example_two():
    sb = ""
    for _ in range(3):
        sb += "*"
        print("Stars:", sb)
def example_four():
    print("This is the new method added to File2.")
def main():
    print("File2 demo starting.")
    example_three()
    example_one()
    example_two()
    example_four()
    print("File2 demo finished.")
if __name__ == "__main__":
    main()
    
