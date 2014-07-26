from subprocess import Popen, PIPE
import re

# TODO(sandy): this might not work for changes

def get_diff(b, a):
    pipe = Popen("diff -y %s %s" % (a, b), shell = True, stdout = PIPE)
    pipe.wait()
    return pipe.stdout

# get line numbers in second arg to diff, given a range in first arg
def apply_diff(diff, start, end):
    leftLine = 0
    rightLine = 0
    for line in diff:
        bits = line.split("\t", 1)

        left = bits[0]
        # strip out spacing
        right = bits[1][6:]

        isMod = right[0] != "\t"
        publish = False
        if not isMod:
            # if not a mod, move both pointers
            leftLine += 1
            rightLine += 1

            # output if in the correct range
            if start <= leftLine and leftLine <= end:
                print rightLine
        else:
            # otherwise only move one pointer
            right = right[6:]
            if right[0] == "<":
                leftLine += 1
            else:
                rightLine += 1


apply_diff(get_diff("head.txt", "buffer.txt"), 1, 8)

