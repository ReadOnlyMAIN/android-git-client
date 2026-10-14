package fr.readonlymain.gitclient.data.model

import java.io.File

/** Filesystem identity, independent of display names and syntactic path aliases. */
fun canonicalRepositoryPath(path: String): String = File(path).canonicalPath
