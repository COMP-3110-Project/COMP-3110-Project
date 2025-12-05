# File3 helper demo (refactored)
# Contains one split method and one merged method
def print_array(array):
    for index, value in enumerate(array):
        print(f"Value {index}: {value}")
def sum_loop(array):
    total = 0
    for value in array:
        total += value
    return total
def sum_values(array):
    return sum_loop(array)
def analyze_array(array, target):
    total = sum_values(array)
    average = total / float(len(array)) if len(array) > 0 else 0.0
    print("Average:", average)
    found_index = -1
    for i, value in enumerate(array):
        if value == target:
            found_index = i
            break
    print("Index of", target, ":", found_index)
def main():
    print("File3 demo starting.")
    numbers = [2, 4, 6, 8, 10]
    print_array(numbers)
    total = sum_values(numbers)
    print("Total:", total)
    target = 6
    analyze_array(numbers, target)
    print("File3 demo finished.")
if __name__ == "__main__":
    main()
