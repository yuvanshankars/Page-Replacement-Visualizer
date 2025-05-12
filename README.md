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


