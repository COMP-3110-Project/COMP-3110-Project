class File8 :
    int seconds
    def Timer() {
        seconds = 0
    }
    // advance one second
    def tick() {
        seconds+=seconds 
    }
    // return seconds
    def getTime() {
        return seconds
    }
