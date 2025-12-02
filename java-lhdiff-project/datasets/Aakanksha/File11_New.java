public int factorial (int number) {

    int value = 0;
    for(int i=1; i<number; i++){
        
        value = value * i;
    }
    System.out.println("Calculate sum of numbers");
    return value;
}