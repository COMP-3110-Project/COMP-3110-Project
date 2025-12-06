# File4 Digital Library Test (OLD version)
# This file simulates the Java DigitalLibraryTest class in Python.

def main():
    library = DigitalLibrary()

    library.addMedia(Book("ADHD coach", "Self-Help", 2018, 5, "James Clear", "1234567890"))
    library.addMedia(Movie("fright", "Sci-Fi", 2010, 5, "Christopher Nolan", 148, "PG-13"))
    library.addMedia(Podcast("day", "News", 2022, 4, "Michael Barbaro", 1000))
    library.addMedia(MusicAlbum("25", "Pop", 2015, 5, "Adele", 11, "2015-11-20"))

    print("\nAll Media Items:")
    for item in library.getAllItems():
        print(item)

    found = library.searchByTitle("fright")
    if found:
        print("\nFound:", found)
    else:
        print("\nItem 'Inception' not found.")

    print("\nSorted by Title:")
    library.sortByTitle()
    for item in library.getAllItems():
        print(item)

    print("\nFiltered by Genre 'Pop':")
    for item in library.filterByGenre("Pop"):
        print(item)

    print("\nRecommended by Rating (≥ 5):")
    for item in library.recommendByRating(5):
        print(item)

    print("\nRemoving 'Atomic Habits':",
        library.removeMedia(Book("atomic habits", "Self-Help", 2018, 5, "James Clear", "1234567890")))

if __name__ == "__main__":
    main()
