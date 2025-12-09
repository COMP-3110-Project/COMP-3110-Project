# File15_Old.py

# Simple To-Do Counter
def count_tasks(tasks):
    count = 0
    for task in tasks:
        if "done" in task.lower():
            count += 1
    return count

# Sample tasks
tasks = [
    "Write report - done",
    "Email professor",
    "Study for exam - done",
    "Submit assignment"
]

completed = count_tasks(tasks)
print("Total tasks:", len(tasks))
print("Completed:", completed)
print("Keep going!")
print("Good progress")
