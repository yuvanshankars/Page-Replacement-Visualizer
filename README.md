# Page-Replacement-Visualizer
def fifo(pages, frame_size):
    frame = []
    page_faults = 0
    for page in pages:
        if page not in frame:
            if len(frame) < frame_size:
                frame.append(page)
            else:
                frame.pop(0)
                frame.append(page)
            page_faults += 1
    return page_faults

def lru(pages, frame_size):
    frame = []
    page_faults = 0
    recent_use = []

    for page in pages:
        if page not in frame:
            if len(frame) < frame_size:
                frame.append(page)
            else:
                lru_index = recent_use.pop(0)
                frame.remove(lru_index)
                frame.append(page)
            page_faults += 1
        else:
            recent_use.remove(page)
        recent_use.append(page)
    return page_faults

def optimal(pages, frame_size):
    frame = []
    page_faults = 0

    for i in range(len(pages)):
        page = pages[i]
        if page not in frame:
            if len(frame) < frame_size:
                frame.append(page)
            else:
                future = pages[i+1:]
                indices = []
                for p in frame:
                    if p in future:
                        indices.append(future.index(p))
                    else:
                        indices.append(float('inf'))
                victim = frame[indices.index(max(indices))]
                frame.remove(victim)
                frame.append(page)
            page_faults += 1
    return page_faults

# --- Input Section ---
ref_string = input("Enter page reference string (space-separated): ")
pages = list(map(int, ref_string.split()))
frame_size = int(input("Enter number of frames: "))

# --- Output Section ---
print("\n--- Page Replacement Algorithms Comparison ---")
print(f"Page faults using FIFO:    {fifo(pages, frame_size)}")
print(f"Page faults using LRU:     {lru(pages, frame_size)}")
print(f"Page faults using Optimal: {optimal(pages, frame_size)}")
//OUTPUT:
Enter page reference string (space-separated): 7 0 1 2 0 3 0 4 2 3 0 3 2
Enter number of frames: 4
SOLUTION:
Page faults using FIFO:    10
Page faults using LRU:     8
Page faults using Optimal: 7
