package fr.readonlymain.gitclient.data.preferences

/** Storage corruption or key loss is not an empty credential collection. */
class CredentialStorageException(message: String, cause: Throwable? = null) : Exception(message, cause)
