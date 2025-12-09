# File15_New.py

# Smart To-Do Manager with Priority
def count_tasks(tasks):
    # Now counts high-priority completed tasks
    completed = 0
    # Priority tasks have [HIGH]
    for task in tasks:
        if "[HIGH]" in task and "done" in task.lower():
            completed += 1
    return completed

# Sample tasks
tasks = [
    "[HIGH] Write report - done",
    "Email professor",
    "[HIGH] Study for exam - done",
    "Submit assignment - done"
]

completed = count_tasks(tasks)
print("Total tasks:", len(tasks))
print("High-priority completed:", completed)

# Priority tracking
print("Focus on [HIGH] tasks first!")
print("Smart tracking enabled!")
print("Productivity boosted!")
print("Keep going!")
print("Good progress")
