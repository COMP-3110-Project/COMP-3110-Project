# File4 Digital Library Test (NEW version, refactored)
# Clean layout, helper print function, reduced repetition.

def print_list(title, items):
    print(f"\n{title}:")
    for item in items:
        print(item)

def main():
    library = DigitalLibrary()

    library.addMedia(Book("ADHD coach", "Self-Help", 2018, 5, "James Clear", "1234567890"))
    library.addMedia(Movie("fright", "Sci-Fi", 2010, 5, "Christopher Nolan", 148, "PG-13"))
    library.addMedia(Podcast("day", "News", 2022, 4, "Michael Barbaro", 1000))
    library.addMedia(MusicAlbum("25", "Pop", 2015, 5, "Adele", 11, "2015-11-20"))

    print_list("All Media Items", library.getAllItems())

    found = library.searchByTitle("fright")
    print("\nFound:" if found else "\nNot Found:", found)

    library.sortByTitle()
    print_list("Sorted by Title", library.getAllItems())

    print_list("Filtered by Genre 'Pop'", library.filterByGenre("Pop"))

    print_list("Recommended by Rating (≥ 5)", library.recommendByRating(5))

    removed = library.removeMedia(Book("atomic habits", "Self-Help", 2018, 5, "James Clear", "1234567890"))
    print("\nRemoving 'Atomic Habits':", removed)

if __name__ == "__main__":
    main()
