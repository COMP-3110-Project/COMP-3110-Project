class File10:
    a = int(input("Enter a numerical value: "))
    b = int(input("Enter a numerical value: "))
    c = int(input("Enter a numerical value: "))
    d = int(input("Enter a numerical value: "))
    e = int(input("Enter a numerical value: "))

    @staticmethod
    def sum(num1, num2):
        numsum = num1 + num2
        return numsum

Sum = File10.sum(File10.a, File10.b)
print(Sum)