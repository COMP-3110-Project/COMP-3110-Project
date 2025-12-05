# File3 helper demo
def print_array(values):
    for i, value in enumerate(values):
        print(f"Value {i}: {value}")
def sum_values(values):
    result = 0
    for value in values:
        result += value
    return result
def average(values):
    if len(values) == 0:
        return 0.0
    result = sum_values(values)
    return result / float(len(values))
def index_of(values, target):
    for i, value in enumerate(values):
        if value == target:
            return i
    return -1
def main():
    print("File3 demo starting.")
    numbers = [2, 4, 6, 8, 10]
    print_array(numbers)
    total = sum_values(numbers)
    avg = average(numbers)
    print("Total:", total)
    print("Average:", avg)
    target = 6
    index = index_of(numbers, target)
    print("Index of", target, ":", index)
    print("File3 demo finished.")
if __name__ == "__main__":
    main()
