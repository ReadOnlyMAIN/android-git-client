package fr.readonlymain.gitclient.data.repository

/** The local mutation succeeded; callers must not imply that it was rolled back. */
class PartialBranchDeletionException(val localBranchDeleted: Boolean, cause: Exception) :
    Exception("Local branch deleted, but remote deletion failed: ${cause.localizedMessage}", cause)
