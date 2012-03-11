/notes/pathInfoMatcher

// should return true
startsSubfolder:${piStarts("/subfolder")?string("true","false")}

// shoud (always) return true
startsRoot:${piStarts("/")?string("true","false")}

// should return false
startsContactPage:${piStarts("/contactPage")?string("true","false")}

