package utils

object Todo {
    // for TODOs
    val unimplemented = "(unimplemented)"

    // identify places currently in progress
    def needsWork(why: String) = "(needs work)"

    // get warnings for HACKs
    def hack = "(hack)"
}
